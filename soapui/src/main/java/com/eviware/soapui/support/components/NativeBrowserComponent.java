/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mozilla.interfaces.nsIHttpChannel;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWeakReference;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.Configurable;
import com.teamdev.jxbrowser.Feature;
import com.teamdev.jxbrowser.events.NavigationAdapter;
import com.teamdev.jxbrowser.events.NavigationFinishedEvent;
import com.teamdev.jxbrowser.events.NavigationStatusCode;
import com.teamdev.jxbrowser.events.StatusChangedEvent;
import com.teamdev.jxbrowser.events.StatusListener;
import com.teamdev.jxbrowser.prompt.DefaultPromptService;
import com.teamdev.jxbrowser.security.HttpSecurityAction;
import com.teamdev.jxbrowser.security.HttpSecurityHandler;
import com.teamdev.jxbrowser.security.SecurityProblem;

public class NativeBrowserComponent implements nsIWebProgressListener, nsIWeakReference, StatusListener
{
	private Browser browser;
	private JPanel panel = new JPanel( new BorderLayout() );
	private JPanel statusBar;
	private JLabel statusLabel;
	private String errorPage;
	private boolean showingErrorPage;
	public String url;
	private Boolean possibleError = false;
	private final boolean addStatusBar;
	private com.eviware.soapui.support.components.NativeBrowserComponent.InternalNavigationAdapter internalNavigationAdapter;

	public NativeBrowserComponent( boolean addToolbar, boolean addStatusBar )
	{
		this.addStatusBar = addStatusBar;
	}

	public Component getComponent()
	{
		if( SoapUI.isJXBrowserDisabled( true ) )
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "browser component disabled or not available on this platform" );
			panel.add( jxbrowserDisabledPanel );
		}
		else
		{
			if( browser == null )
			{
				if( addStatusBar )
				{
					statusBar = new JPanel( new BorderLayout() );
					statusLabel = new JLabel();
					UISupport.setFixedSize( statusBar, new Dimension( 20, 20 ) );
					statusBar.add( statusLabel, BorderLayout.WEST );
					panel.add( statusBar, BorderLayout.SOUTH );
				}

				// if( addToolbar )
				// panel.add( buildToolbar(), BorderLayout.NORTH );

				initBrowser();

				configureBrowser();

				browser.navigate( "about:blank" );
			}
		}
		return panel;
	}

	@SuppressWarnings( "unused" )
	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new BackAction() ) );
		toolbar.addRelatedGap();
		toolbar.addFixed( UISupport.createToolbarButton( new ForwardAction() ) );

		toolbar.addGlue();

		return toolbar;
	}

	private final static class InternalHttpSecurityHandler implements HttpSecurityHandler
	{
		@Override
		public HttpSecurityAction onSecurityProblem( Set<SecurityProblem> arg0 )
		{
			return HttpSecurityAction.CONTINUE;
		}
	}

	private final class InternalNavigationAdapter extends NavigationAdapter
	{
		@Override
		public void navigationFinished( NavigationFinishedEvent evt )
		{
			if( evt.getUrl().equals( SoapUI.PUSH_PAGE_URL ) && !( evt.getStatusCode().equals( NavigationStatusCode.OK ) ) )
			{
				browser.navigate( SoapUI.PUSH_PAGE_ERROR_URL );
			}
		}
	}

	private class BackAction extends AbstractAction
	{
		public BackAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_left.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to previous selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( !browser.canGoBack() )
				Toolkit.getDefaultToolkit().beep();
			else
				browser.goBack();
		}
	}

	private class ForwardAction extends AbstractAction
	{
		public ForwardAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_right.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to next selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( !browser.canGoForward() )
				Toolkit.getDefaultToolkit().beep();
			else
				browser.goForward();
		}
	}

	public synchronized boolean initBrowser()
	{
		if( browser != null || SoapUI.isJXBrowserDisabled( true ) )
			return false;

		browser = BrowserFactory.createBrowser();
		browser.getServices().setPromptService( new DefaultPromptService() );

		BrowserComponent.initNewWindowManager( browser, false );

		internalHttpSecurityHandler = new InternalHttpSecurityHandler();
		browser.setHttpSecurityHandler( internalHttpSecurityHandler );

		internalNavigationAdapter = new InternalNavigationAdapter();
		browser.addNavigationListener( internalNavigationAdapter );

		browser.addStatusListener( this );

		panel.add( browser.getComponent(), BorderLayout.CENTER );

		return true;
	}

	public void release()
	{
		if( browser != null )
		{
			cleanup();
		}

		possibleError = false;
	}

	private synchronized void cleanup()
	{
		if( browser != null )
		{
			browser.stop();
			browser.dispose();
			browser.setHttpSecurityHandler( null );
			browser.removeStatusListener( this );

			panel.removeAll();
			browser = null;
		}
	}

	private void configureBrowser()
	{
		if( browser != null )
		{
			Configurable contentSettings = browser.getConfigurable();

			if( SoapUI.isJXBrowserPluginsDisabled() )
			{
				contentSettings.disableFeature( Feature.PLUGINS );
			}
			else
			{
				contentSettings.enableFeature( Feature.PLUGINS );
			}
		}

	}

	public void setContent( String contentAsString, String contextUri )
	{
		if( SoapUI.isJXBrowserDisabled( true ) )
			return;

		if( browser == null )
		{
			initBrowser();
		}

		configureBrowser();

		try
		{
			// browser.navigate( contextUri );

			browser.setContent( contentAsString, contextUri );
			pcs.firePropertyChange( "content", null, null );
		}
		catch( Throwable e )
		{
			e.printStackTrace();
		}

	}

	public void setContent( String content )
	{
		if( SoapUI.isJXBrowserDisabled( true ) )
			return;

		if( browser == null )
		{
			initBrowser();
		}

		configureBrowser();

		browser.setContent( content );
		pcs.firePropertyChange( "content", null, null );
	}

	public void navigate( String url, String errorPage )
	{
		navigate( url, null, errorPage );
	}

	public String getContent()
	{
		return browser == null ? null : XmlUtils.serialize( browser.getDocument() );
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url ) throws InterruptedException, InvocationTargetException
	{
		navigate( url, null );
	}

	public nsISupports queryInterface( String uuid )
	{
		return Mozilla.queryInterface( this, uuid );
	}

	public nsISupports queryReferent( String uuid )
	{
		return Mozilla.queryInterface( this, uuid );
	}

	public void onLocationChange( nsIWebProgress arg0, nsIRequest arg1, nsIURI arg2 )
	{
		if( getUrl() != null && !getUrl().equals( "about:blank" ) )
		{
			if( !possibleError )
				possibleError = true;
			else
			{
				if( !showingErrorPage )
				{
					showErrorPage();
				}
			}
		}
	}

	public void onProgressChange( nsIWebProgress arg0, nsIRequest arg1, int arg2, int arg3, int arg4, int arg5 )
	{

	}

	public void onSecurityChange( nsIWebProgress arg0, nsIRequest arg1, long arg2 )
	{
	}

	public void onStateChange( nsIWebProgress arg0, nsIRequest request, long arg2, long arg3 )
	{
		try
		{
			if( getUrl() != null && !getUrl().equals( "about:blank" ) )
			{
				nsIHttpChannel ch = null;

				ch = ( nsIHttpChannel )request.queryInterface( nsIHttpChannel.NS_IHTTPCHANNEL_IID );

				if( ch != null )
				{
					possibleError = false;
					showingErrorPage = false;
				}
			}
		}
		catch( XPCOMException e )
		{
			SoapUI.logError( e );
			if( possibleError && !showingErrorPage )
				showErrorPage();
		}

	}

	private void showErrorPage()
	{
		if( errorPage != null && !errorPage.equals( getUrl() ) )
		{
			try
			{
				showingErrorPage = true;
				setUrl( errorPage );
			}
			catch( Throwable e )
			{
				e.printStackTrace();
			}
		}
	}

	public String getErrorPage()
	{
		return errorPage;
	}

	public void setErrorPage( String errorPage )
	{
		this.errorPage = errorPage;
	}

	public void onStatusChange( nsIWebProgress arg0, nsIRequest arg1, long arg2, String arg3 )
	{
		try
		{
			if( getUrl() != null && !getUrl().equals( "about:blank" ) )
			{
				nsIHttpChannel ch = null;

				ch = ( nsIHttpChannel )arg1.queryInterface( nsIHttpChannel.NS_IHTTPCHANNEL_IID );

				if( ch != null )
				{
					possibleError = false;
					showingErrorPage = false;
				}
			}
		}
		catch( XPCOMException e )
		{
			SoapUI.logError( e );
			if( possibleError && !showingErrorPage )
				showErrorPage();
		}
	}

	public void statusChanged( StatusChangedEvent event )
	{
		if( statusLabel != null )
		{
			statusLabel.setText( event.getStatusText() );
		}
	}

	public boolean isBrowserInitialised()
	{
		return browser != null;
	}

	private PropertyChangeSupport pcs = new PropertyChangeSupport( this );

	public void addPropertyChangeListener( PropertyChangeListener pcl )
	{
		pcs.addPropertyChangeListener( pcl );
	}

	public void rempvePropertyChangeListener( PropertyChangeListener pcl )
	{
		pcs.removePropertyChangeListener( pcl );
	}

	/**
	 * Called after a HTTP response from the server is received.
	 * 
	 * @see http://developer.mozilla.org/en/Observer_Notifications
	 */
	public static final String EVENT_HTTP_ON_MODIFY_REQUEST = "http-on-modify-request";
	private InternalHttpSecurityHandler internalHttpSecurityHandler;

	/**
	 * Converts an object implementing the nsIURI interface into a human readable
	 * URI.
	 * 
	 * @param uri
	 *           nsIURI object to convert
	 * @return String URI result string
	 */
	public static String dumpUri( nsIURI uri )
	{
		if( uri == null )
		{
			return "";
		}

		return ( ( uri.getUsername() == null || "".equals( uri.getUsername() ) ) ? "" : uri.getUsername() + ":"
				+ uri.getUserPass() )
				+ uri.getScheme()
				+ "://"
				+ uri.getHost()
				+ ( ( uri.getPort() == -1 ) ? "" : ":" + uri.getPort() )
				+ uri.getPath();
	}

	public void navigate( String url, String postData, String errorPage )
	{
		if( SoapUI.isJXBrowserDisabled( true ) )
			return;

		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;

		if( browser == null )
		{
			initBrowser();
		}

		configureBrowser();
		BrowserComponent.updateJXBrowserProxy();

		if( postData != null && postData.length() > 0 )
		{
			browser.navigate( url, postData );
		}
		else
		{
			browser.navigate( url );
		}

		if( showingErrorPage )
			showingErrorPage = false;
	}
}
