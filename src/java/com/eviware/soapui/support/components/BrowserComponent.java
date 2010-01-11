/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mozilla.interfaces.nsIHttpChannel;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWeakReference;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.teamdev.jxbrowser.BrowserAttributes;
import com.teamdev.jxbrowser.ClipboardSupport;
import com.teamdev.jxbrowser.ContentHandler;
import com.teamdev.jxbrowser.SearchParams;
import com.teamdev.jxbrowser.WebBrowser;
import com.teamdev.jxbrowser.WebBrowserFactory;
import com.teamdev.jxbrowser.WebBrowserHistory;
import com.teamdev.jxbrowser.WebBrowserWindow;
import com.teamdev.jxbrowser.WindowCreator;
import com.teamdev.jxbrowser.event.ContextMenuListener;
import com.teamdev.jxbrowser.event.HistoryChangeListener;
import com.teamdev.jxbrowser.event.LocationEvent;
import com.teamdev.jxbrowser.event.RequestAdapter;
import com.teamdev.jxbrowser.event.RequestListener;
import com.teamdev.jxbrowser.event.StatusChangeEvent;
import com.teamdev.jxbrowser.event.StatusChangeListener;
import com.teamdev.jxbrowser.event.TitleChangeListener;
import com.teamdev.jxbrowser.mozilla.MozillaWebBrowser;
import com.teamdev.jxbrowser.printing.WebBrowserPrinting;
import com.teamdev.jxbrowser.ssl.BadCertificateHandler;
import com.teamdev.xpcom.AsyncHandlerFactory;
import com.teamdev.xpcom.PoxyAuthenticationHandler;
import com.teamdev.xpcom.ProxyConfiguration;
import com.teamdev.xpcom.ProxyServerAuthInfo;
import com.teamdev.xpcom.ProxyServerType;
import com.teamdev.xpcom.Services;
import com.teamdev.xpcom.Xpcom;

public class BrowserComponent implements nsIWebProgressListener, nsIWeakReference, StatusChangeListener
{
	private static String disabledReason;

	public class DummyBrowser implements WebBrowser
	{
		public void activate()
		{
		}

		public void addContentHandler( ContentHandler arg0 )
		{
		}

		public void addContextMenuListener( ContextMenuListener arg0 )
		{
		}

		public void addHistoryChangeListener( HistoryChangeListener arg0 )
		{
		}

		public void addRequestListener( RequestListener arg0 )
		{
		}

		public void addStatusChangeListener( StatusChangeListener arg0 )
		{
		}

		public void addTitleChangeListener( TitleChangeListener arg0 )
		{
		}

		public void deactivate()
		{
		}

		public void displayAsSource( boolean arg0 )
		{
		}

		public void dispose()
		{
		}

		public String evaluateScript( String arg0 )
		{
			return null;
		}

		public boolean findString( String arg0 )
		{
			return false;
		}

		public AsyncHandlerFactory getAsyncHandler()
		{
			return null;
		}

		public BrowserAttributes getAttributes()
		{
			return null;
		}

		public boolean getCanGoBack()
		{
			return false;
		}

		public boolean getCanGoForward()
		{
			return false;
		}

		public ClipboardSupport getClipboardSupport()
		{
			return null;
		}

		public Component getComponent()
		{
			return new JLabel( "Browser Component Disabled: [" + disabledReason + "]" );
		}

		public Document getDocument()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getDocumentContent( String arg0 )
		{
			// TODO Auto-generated method stub
			return null;
		}

		public WebBrowserHistory getHistory()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getLocationURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public WebBrowserPrinting getPrinting()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getTitle()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public void goBack()
		{
			// TODO Auto-generated method stub

		}

		public void goForward()
		{
			// TODO Auto-generated method stub

		}

		public boolean isDisposed()
		{
			// TODO Auto-generated method stub
			return false;
		}

		public void navigate( String arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void navigate( String arg0, String arg1 )
		{
			// TODO Auto-generated method stub

		}

		public void pageDown()
		{
			// TODO Auto-generated method stub

		}

		public void pageUp()
		{
			// TODO Auto-generated method stub

		}

		public void refresh()
		{
			// TODO Auto-generated method stub

		}

		public void removeContextMenuListener( ContextMenuListener arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void removeHistoryChangeListener( HistoryChangeListener arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void removeRequestListener( RequestListener arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void removeStatusChangeListener( StatusChangeListener arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void removeTitleChangeListener( TitleChangeListener arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void resetBadCertificateHandler( BadCertificateHandler arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void saveCurrentDocument( File arg0, RequestListener arg1 )
		{
			// TODO Auto-generated method stub

		}

		public void saveURLtoFile( URL arg0, File arg1, RequestListener arg2 )
		{
			// TODO Auto-generated method stub

		}

		public void setAsyncHandler( AsyncHandlerFactory arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void setAttributes( BrowserAttributes arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void setContent( String arg0, String arg1 )
		{
			// TODO Auto-generated method stub

		}

		public void setContent( String arg0, String arg1, String arg2 )
		{
			// TODO Auto-generated method stub

		}

		public void setContentWithContext( String arg0, String arg1, String arg2 )
		{
			// TODO Auto-generated method stub

		}

		public void setFocusAtFirstElement()
		{
			// TODO Auto-generated method stub

		}

		public void setWindowCreator( WindowCreator arg0 )
		{
			// TODO Auto-generated method stub

		}

		public void showOpenFileDialog()
		{
			// TODO Auto-generated method stub

		}

		public void showSaveFileDialog()
		{
			// TODO Auto-generated method stub

		}

		public void stop()
		{
			// TODO Auto-generated method stub

		}

		public BufferedImage toImage()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean findNext( String arg0, SearchParams arg1 )
		{
			// TODO Auto-generated method stub
			return false;
		}

		public void setContent( String arg0 )
		{
			// TODO Auto-generated method stub
			
		}

	}

	private WebBrowser browser;
	private static WebBrowserFactory webBrowserFactory;
	private JPanel panel = new JPanel( new BorderLayout() );
	private JPanel statusBar;
	private JLabel statusLabel;
	private String errorPage;
	private WebBrowserWindow browserWindowAdapter = new BrowserWindowAdapter();
	private final boolean addToolbar;
	private boolean showingErrorPage;
	public String url;
	private static Boolean initialized = false;
	private Boolean possibleError = false;
	@SuppressWarnings( "unused" )
	private boolean disposed;
	private static boolean disabled;

	public BrowserComponent( boolean addToolbar )
	{
		this.addToolbar = addToolbar;
		initialize();
	}

	public static void setDisabled( boolean disabled )
	{
		BrowserComponent.disabled = disabled;
	}

	public synchronized static void initialize()
	{
		if( initialized )
			return;

		try
		{
			if( !isJXBrowserDisabled() )
			{
				if( Xpcom.isMacOSX() )
				{
					final String currentCP = System.getProperty( "java.class.path" );
					final String appleJavaExtentions = ":/System/Library/Java";
					System.setProperty( "java.class.path", currentCP + appleJavaExtentions );
				}

				Xpcom.initialize();
				webBrowserFactory = WebBrowserFactory.getInstance();
			}

			initialized = true;
		}
		catch( Throwable t )
		{
			disabledReason = t.getMessage();
			t.printStackTrace();
		}
	}

	public static boolean isJXBrowserDisabled()
	{
		if( disabled )
			return true;

		String disable = System.getProperty( "soapui.jxbrowser.disable", "nope" );
		if( disable.equals( "true" ) )
			return true;

		if( !disable.equals( "false" )
				&& ( !Xpcom.isMacOSX() && "64".equals( System.getProperty( "sun.arch.data.model" ) ) ) )
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

	private class BackAction extends AbstractAction
	{
		public BackAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_left.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to previous selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( browser.getHistory().getCurrentPosition() == 0 )
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
			browser.goForward();
		}
	}

	public synchronized boolean initBrowser()
	{
		if( browser != null )
			return false;

		browser = webBrowserFactory == null ? new DummyBrowser() : webBrowserFactory.createBrowser();
		panel.add( browser.getComponent(), BorderLayout.CENTER );
		browser.addContentHandler( new ContentHandler()
		{

			public boolean canHandleContent( String arg0 )
			{
				return true;
			}

			public void handleContent( URL arg0 )
			{
				SoapUI.log.info( "Ignoring content for [" + arg0 + "]" );
			}

			public boolean isPreferred( String arg0 )
			{
				return true;
			}
		} );

		if( webBrowserFactory != null )
		{
			nsIWebBrowser nsWebBrowser = ( ( MozillaWebBrowser )browser ).getWebBrowser();
			nsWebBrowser.addWebBrowserListener( this, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID );
			browser.addStatusChangeListener( this );

			browser.setWindowCreator( new WindowCreator()
			{
				public WebBrowserWindow createChildWindow( Component parentComponent, long flags )
				{
					return browserWindowAdapter;
				}
			} );

			setUpProxy();
		}
		return true;
	}

	public void release()
	{
		if( browser != null )
		{
			disposed = true;

			if( !SwingUtilities.isEventDispatchThread() )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						cleanup();
					}
				} );
			}
			else
				cleanup();
		}

		possibleError = false;
	}

	private void cleanup()
	{
		browser.stop();

		if( webBrowserFactory != null )
		{
			nsIWebBrowser nsWebBrowser = ( ( MozillaWebBrowser )browser ).getWebBrowser();
			nsWebBrowser.removeWebBrowserListener( BrowserComponent.this,
					nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID );
			browser.removeStatusChangeListener( BrowserComponent.this );
			browser.dispose();
		}
		browser = null;
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

			browser.activate();
			browser.setContentWithContext( contentAsString, contentType, contextUri );
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

			browser.activate();
			browser.setContent( content, contentType );
		}
	}

	public boolean navigate( String url, String errorPage )
	{
		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;
		Xpcom.invokeLater( new Navigator() );
		return true;
	}

	public String getContent()
	{
		return browser == null ? null : XmlUtils.serialize( browser.getDocument() );
	}

	private final class Navigator implements Runnable
	{
		public void run()
		{
			try
			{
				if( browser == null )
				{
					initBrowser();
				}

				browser.activate();
				browser.navigate( getUrl() );

				if( showingErrorPage )
					showingErrorPage = false;
			}
			catch( Throwable e )
			{
				SoapUI.log( e.toString() );
			}
		}
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

	private class BrowserWindowAdapter implements WebBrowserWindow
	{
		private boolean resizable;

		public void close()
		{
		}

		public boolean isClosed()
		{
			return true;
		}

		public void setModal( boolean arg0 )
		{
		}

		public void setSize( int arg0, int arg1 )
		{
		}

		public void setVisible( boolean arg0 )
		{
		}

		public void setWebBrowser( WebBrowser arg0 )
		{
			if( arg0 != null )
			{
				arg0.addRequestListener( new PopupRequestAdapter() );
			}
		}

		public boolean isResizable()
		{
			return resizable;
		}

		public void setResizable( boolean resizable )
		{
			this.resizable = resizable;
		}
	}

	public void statusChanged( final StatusChangeEvent event )
	{
		if( statusLabel != null )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					statusLabel.setText( event.getStatus() );
				}
			} );
		}
	}

	public boolean isBrowserInitialised()
	{
		return browser != null;
	}

	private static class PopupRequestAdapter extends RequestAdapter
	{
		private LocationEvent event;

		@Override
		public void locationChanged( LocationEvent arg0 )
		{
			if( !arg0.getLocation().equals( "about:blank" ) )
			{
				event = arg0;
				SwingUtilities.invokeLater( new Runnable()
				{

					public void run()
					{
						boolean opened = false;
						if( UISupport.confirm( "Open url [" + event.getLocation() + "] in external browser?", "Open URL" ) )
						{
							opened = true;
							SwingUtilities.invokeLater( new Runnable()
							{

								public void run()
								{
									Tools.openURL( event.getLocation() );
									event = null;
								}
							} );
						}

						event.getWebBrowser().stop();
						event.getWebBrowser().deactivate();
						event.getWebBrowser().dispose();
						event.getWebBrowser().removeRequestListener( PopupRequestAdapter.this );
						if( !opened )
							event = null;
					}
				} );
			}
		}
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
			if( StringUtils.hasContent( contextUri ) )
			{
				browser.setContentWithContext( contentAsString, contentType, contextUri );
			}
			else
			{
				browser.setContent( contentAsString, contentType );
			}
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
}
