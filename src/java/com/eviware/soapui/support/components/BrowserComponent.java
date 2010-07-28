/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import com.eviware.soapui.impl.rest.panels.request.views.html.HttpHtmlResponseView;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jniwrapper.PlatformContext;
import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.NewWindowContainer;
import com.teamdev.jxbrowser.NewWindowManager;
import com.teamdev.jxbrowser.NewWindowParams;
import com.teamdev.jxbrowser.events.DisposeEvent;
import com.teamdev.jxbrowser.events.DisposeListener;
import com.teamdev.jxbrowser.events.NavigationEvent;
import com.teamdev.jxbrowser.events.NavigationFinishedEvent;
import com.teamdev.jxbrowser.events.NavigationListener;
import com.teamdev.jxbrowser.events.StatusChangedEvent;
import com.teamdev.jxbrowser.events.StatusListener;
import com.teamdev.xpcom.PoxyAuthenticationHandler;
import com.teamdev.xpcom.ProxyConfiguration;
import com.teamdev.xpcom.ProxyServerAuthInfo;
import com.teamdev.xpcom.ProxyServerType;
import com.teamdev.xpcom.Services;

public class BrowserComponent implements nsIWebProgressListener, nsIWeakReference, StatusListener
{
	private Browser browser;
	private JPanel panel = new JPanel( new BorderLayout() );
	private JPanel statusBar;
	private JLabel statusLabel;
	private String errorPage;
	private final boolean addToolbar;
	private boolean showingErrorPage;
	public String url;
	private Boolean possibleError = false;
	@SuppressWarnings( "unused" )
	private boolean disposed;
	private static boolean disabled;
	private NavigationListener internalNavigationListener;
	private DisposeListener internalDisposeListener;
	private HttpHtmlResponseView httpHtmlResponseView;
	private static SoapUINewWindowManager newWindowManager;

	public BrowserComponent( boolean addToolbar )
	{
		this.addToolbar = addToolbar;
	}

	public static void setDisabled( boolean disabled )
	{
		BrowserComponent.disabled = disabled;
	}

	public static boolean isJXBrowserDisabled()
	{
		if( disabled )
			return true;

		String disable = System.getProperty( "soapui.jxbrowser.disable", "nope" );
		if( disable.equals( "true" ) )
			return true;

		if( !disable.equals( "false" )
				&& ( !PlatformContext.isMacOS() && "64".equals( System.getProperty( "sun.arch.data.model" ) ) ) )
			return true;

		return false;
	}

	public Component getComponent()
	{
		if( isJXBrowserDisabled() )
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "browser component disabled" );
			panel.add( jxbrowserDisabledPanel );
		}
		else
		{
			if( browser == null )
			{

				statusBar = new JPanel();
				statusLabel = new JLabel();
				statusBar.add( statusLabel, BorderLayout.CENTER );

				if( addToolbar )
					panel.add( buildToolbar(), BorderLayout.NORTH );

				panel.add( statusBar, BorderLayout.SOUTH );

				initBrowser();

				browser.navigate( "about:blank" );
			}
		}
		return panel;
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new BackAction() ) );
		toolbar.addRelatedGap();
		toolbar.addFixed( UISupport.createToolbarButton( new ForwardAction() ) );

		toolbar.addGlue();

		return toolbar;
	}

	public void setHttpHtmlResponseView( HttpHtmlResponseView httpHtmlResponseView )
	{
		this.httpHtmlResponseView = httpHtmlResponseView;
	}

	private static final class SoapUINewWindowManager implements NewWindowManager
	{
		private String url;

		public NewWindowContainer evaluateWindow( final NewWindowParams params )
		{
			this.url = params.getUrl();

			return new NewWindowContainer()
			{
				public void insertBrowser( final Browser browser )
				{
					Tools.openURL( url );

					// // creates JFrame in which browser will be embedded
					// final JFrame popupFrame = new JFrame();
					// Container contentPane = popupFrame.getContentPane();
					// contentPane.add( browser.getComponent(), BorderLayout.CENTER
					// );
					// // sets window bounds using popup window params
					// popupFrame.setBounds( params.getBounds() );
					// popupFrame.setVisible( true );
					//
					// // registers window listener to dispose Browser instance on
					// // window close
					// popupFrame.addWindowListener( new WindowAdapter()
					// {
					// @Override
					// public void windowClosing( WindowEvent e )
					// {
					// browser.dispose();
					// }
					// } );
					//
					// // registers Browser dispose listener to dispose window when
					// // Browser
					// // is disposed (e.g. using the window.close JavaScript
					// // function)
					// internalDisposeListener = new InternalDisposeListener(
					// popupFrame );
					// browser.addDisposeListener( internalDisposeListener );
					// browser.addNavigationListener( internalNavigationListener );

				}
			};
		}
	}

	private final class InternalDisposeListener implements DisposeListener
	{
		private final JFrame popupFrame;

		private InternalDisposeListener( JFrame popupFrame )
		{
			this.popupFrame = popupFrame;
		}

		public void browserDisposed( DisposeEvent event )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					popupFrame.dispose();
				}
			} );
		}
	}

	private final class InternalBrowserNavigationListener implements NavigationListener
	{
		@Override
		public void navigationStarted( NavigationEvent arg0 )
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void navigationFinished( NavigationFinishedEvent arg0 )
		{
			if( httpHtmlResponseView != null && httpHtmlResponseView.isRecordHttpTrafic() )
			{
				String newEndpoint = arg0.getUrl();
				HttpTestRequest httpTestRequest = ( HttpTestRequest )( httpHtmlResponseView.getDocument().getRequest() );
				WsdlTestCase testCase = ( WsdlTestCase )httpTestRequest.getTestStep().getTestCase();
				int count = testCase.getTestStepList().size();
				testCase.addTestStep( HttpRequestStepFactory.HTTPREQUEST_TYPE, "Http Test Step " + ++count, newEndpoint );
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
		if( browser != null )
			return false;

		if( PlatformContext.isWindows() )
		{
			browser = BrowserFactory.createBrowser( BrowserType.Mozilla );
		}
		else
		{
			browser = BrowserFactory.createBrowser();
		}
		internalNavigationListener = new InternalBrowserNavigationListener();
		browser.addNavigationListener( internalNavigationListener );

		if( newWindowManager == null )
		{
			newWindowManager = new SoapUINewWindowManager();
			browser.getServices().setNewWindowManager( newWindowManager );
		}

		panel.add( browser.getComponent(), BorderLayout.CENTER );
		return true;
	}

	public void release()
	{
		if( browser != null )
		{
			disposed = true;
			//
			// if( !SwingUtilities.isEventDispatchThread() )
			// {
			// SwingUtilities.invokeLater( new Runnable()
			// {
			// public void run()
			// {
			// cleanup();
			// }
			// } );
			// }
			// else
			cleanup();
		}

		possibleError = false;
	}

	private synchronized void cleanup()
	{
		httpHtmlResponseView = null;

		if( browser != null )
		{
			browser.stop();
			browser.dispose();
			browser.removeDisposeListener( internalDisposeListener );
			browser.removeNavigationListener( internalNavigationListener );
			browser = null;

			panel.removeAll();
		}
	}

	public void setContent( String contentAsString, String contentType, String contextUri )
	{
		if( !SwingUtilities.isEventDispatchThread() )
		{
			SwingUtilities.invokeLater( new ContentSetter( contentAsString, contentType, contextUri ) );
		}
		else
		{
			if( browser == null )
			{
				initBrowser();
			}

			browser.setContent( contentAsString, contentType );
		}
	}

	public void setContent( String content, String contentType )
	{
		if( !SwingUtilities.isEventDispatchThread() )
		{
			SwingUtilities.invokeLater( new ContentSetter( content, contentType, null ) );
		}
		else
		{
			if( browser == null )
			{
				initBrowser();
			}

			browser.setContent( content, contentType );
		}
		pcs.firePropertyChange( "content", null, null );
	}

	public boolean navigate( String url, String errorPage )
	{
		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;

		if( browser == null )
		{
			initBrowser();
		}
		browser.navigate( getUrl() );

		if( showingErrorPage )
			showingErrorPage = false;
		return true;
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
			if( possibleError && !showingErrorPage )
				showErrorPage();
		}
	}

	public void statusChanged( final StatusChangedEvent event )
	{
		if( statusLabel != null )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					statusLabel.setText( event.getStatusText() );
				}
			} );
		}
	}

	public boolean isBrowserInitialised()
	{
		return browser != null;
	}

	public class ContentSetter implements Runnable
	{
		private final String contentAsString;
		private final String contentType;
		private final String contextUri;

		public ContentSetter( String contentAsString, String contentType, String contextUri )
		{
			this.contentAsString = contentAsString;
			this.contentType = contentType;
			this.contextUri = contextUri;
		}

		public void run()
		{
			browser.setContent( contentAsString, contentType );
		}
	}

	/**
	 * Setups proxy configuration
	 */

	void setUpProxy()
	{
		ProxyConfiguration proxyConf = Services.getProxyConfiguration();
		if( proxyConf == null )
			return;

		Settings settings = SoapUI.getSettings();
		PropertyExpansionContext context = null;

		// check system properties first
		String proxyHost = System.getProperty( "http.proxyHost" );
		String proxyPort = System.getProperty( "http.proxyPort" );

		if( proxyHost == null )
			proxyHost = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.HOST, "" ) );

		if( proxyPort == null )
			proxyPort = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.PORT, "" ) );

		if( !StringUtils.isNullOrEmpty( proxyHost ) && !StringUtils.isNullOrEmpty( proxyPort ) )
		{
			proxyConf.setHttpHost( proxyHost );
			proxyConf.setHttpPort( Integer.parseInt( proxyPort ) );
			// check excludes
			String[] excludes = PropertyExpander.expandProperties( context,
					settings.getString( ProxySettings.EXCLUDES, "" ) ).split( "," );
			for( String url : excludes )
			{
				proxyConf.setSkipProxyFor( url );
			}

			final String proxyUsername = PropertyExpander.expandProperties( context, settings.getString(
					ProxySettings.USERNAME, null ) );
			final String proxyPassword = PropertyExpander.expandProperties( context, settings.getString(
					ProxySettings.PASSWORD, null ) );

			if( proxyUsername != null )
			{
				proxyConf.setPoxyAuthenticationHandler( ProxyServerType.HTTP, new PoxyAuthenticationHandler()
				{
					/**
					 * manually sets user name and password for proxy server
					 */
					public ProxyServerAuthInfo authenticationRequired()
					{
						return new ProxyServerAuthInfo( proxyUsername, proxyPassword );
					}
				} );
			}
		}
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

}
