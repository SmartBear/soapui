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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.mozilla.interfaces.nsIBinaryInputStream;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsIHttpChannel;
import org.mozilla.interfaces.nsIHttpHeaderVisitor;
import org.mozilla.interfaces.nsIInputStream;
import org.mozilla.interfaces.nsIInterfaceRequestor;
import org.mozilla.interfaces.nsIObserver;
import org.mozilla.interfaces.nsIObserverService;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISeekableStream;
import org.mozilla.interfaces.nsIServiceManager;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIUploadChannel;
import org.mozilla.interfaces.nsIWeakReference;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.request.views.html.HttpHtmlResponseView;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.settings.WebRecordingSettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jniwrapper.PlatformContext;
import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.NewWindowContainer;
import com.teamdev.jxbrowser.NewWindowManager;
import com.teamdev.jxbrowser.NewWindowParams;
import com.teamdev.jxbrowser.events.NavigationEvent;
import com.teamdev.jxbrowser.events.NavigationFinishedEvent;
import com.teamdev.jxbrowser.events.NavigationListener;
import com.teamdev.jxbrowser.events.StatusChangedEvent;
import com.teamdev.jxbrowser.events.StatusListener;
import com.teamdev.jxbrowser.mozilla.MozillaBrowser;
import com.teamdev.jxbrowser.prompt.DefaultPromptService;
import com.teamdev.jxbrowser1.mozilla.MozillaWebBrowser;
import com.teamdev.xpcom.PoxyAuthenticationHandler;
import com.teamdev.xpcom.ProxyConfiguration;
import com.teamdev.xpcom.ProxyServerAuthInfo;
import com.teamdev.xpcom.ProxyServerType;
import com.teamdev.xpcom.Services;
import com.teamdev.xpcom.Xpcom;
import com.teamdev.xpcom.util.XPCOMManager;

public class BrowserComponent implements nsIWebProgressListener, nsIWeakReference, StatusListener
{
	private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private MozillaBrowser browser;
	private JPanel panel = new JPanel( new BorderLayout() );
	private JPanel statusBar;
	private JLabel statusLabel;
	private String errorPage;
	private boolean showingErrorPage;
	public String url;
	private Boolean possibleError = false;
	@SuppressWarnings( "unused" )
	private boolean disposed;
	private static boolean disabled;
	private NavigationListener internalNavigationListener;
	private static BrowserComponent recordingBrowser;
	private static HttpHtmlResponseView httpHtmlResponseView;
	private static Boolean initialized = false;
	private static SoapUINewWindowManager newWindowManager;
	private static Map<nsIDOMWindow, BrowserComponent> browserMap = new HashMap<nsIDOMWindow, BrowserComponent>();
	private static Map<BrowserComponent, Map<String, RecordedRequest>> browserRecordingMap = new HashMap<BrowserComponent, Map<String, RecordedRequest>>();
	private final boolean addStatusBar;

	static
	{
		initialize();
	}

	public BrowserComponent( boolean addToolbar, boolean addStatusBar )
	{
		this.addStatusBar = addStatusBar;
	}

	public synchronized static void initialize()
	{
		if( initialized )
			return;

		try
		{
			if( !isJXBrowserDisabled() )
			{
				Xpcom.initialize();
			}

			initialized = true;
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
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
				if( addStatusBar )
				{
					statusBar = new JPanel( new BorderLayout() );
					statusLabel = new JLabel();
					statusBar.add( statusLabel, BorderLayout.WEST );
					panel.add( statusBar, BorderLayout.SOUTH );
				}

				// if( addToolbar )
				// panel.add( buildToolbar(), BorderLayout.NORTH );

				initBrowser();

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

	public void setRecordingHttpHtmlResponseView( HttpHtmlResponseView httpHtmlResponseView )
	{
		BrowserComponent.httpHtmlResponseView = httpHtmlResponseView;
		recordingBrowser = httpHtmlResponseView == null ? null : this;
	}

	private static final class RecordingHttpListener implements Runnable
	{
		public void run()
		{
			final Mozilla mozilla = Mozilla.getInstance();
			nsIServiceManager serviceManager = mozilla.getServiceManager();
			nsIObserverService observerService = ( nsIObserverService )serviceManager.getServiceByContractID(
					"@mozilla.org/observer-service;1", nsIObserverService.NS_IOBSERVERSERVICE_IID );

			final nsIBinaryInputStream in = ( nsIBinaryInputStream )XPCOMManager.getInstance().newComponent(
					"@mozilla.org/binaryinputstream;1", nsIBinaryInputStream.class );

			nsIObserver httpObserver = new nsIObserver()
			{
				protected long _lRequestCounter = 0;

				public void observe( nsISupports subject, String sTopic, String sData )
				{
					try
					{
						if( EVENT_HTTP_ON_MODIFY_REQUEST.equals( sTopic ) )
						{
							_lRequestCounter++ ;

							nsIHttpChannel httpChannel = ( nsIHttpChannel )subject
									.queryInterface( nsIHttpChannel.NS_IHTTPCHANNEL_IID );

							if( httpChannel.getNotificationCallbacks() == null )
								return;

							nsIInterfaceRequestor interfaceRequestor = ( nsIInterfaceRequestor )httpChannel
									.getNotificationCallbacks()
									.queryInterface( nsIInterfaceRequestor.NS_IINTERFACEREQUESTOR_IID );

							nsIDOMWindow window = ( nsIDOMWindow )interfaceRequestor
									.getInterface( nsIDOMWindow.NS_IDOMWINDOW_IID );

							BrowserComponent browserComponent = browserMap.get( window );
							if( browserComponent != null && browserComponent == BrowserComponent.recordingBrowser )
							{
								RecordedRequest rr = new RecordedRequest( dumpUri( httpChannel.getURI() ), httpChannel
										.getRequestMethod() );

								nsIUploadChannel upload = ( nsIUploadChannel )httpChannel
										.queryInterface( nsIUploadChannel.NS_IUPLOADCHANNEL_IID );

								byte[] requestData = null;
								if( upload != null )
								{
									nsIInputStream uploadStream = ( nsIInputStream )upload.getUploadStream();

									if( uploadStream != null && uploadStream.available() > 0 )
									{
										nsISeekableStream seekable = ( nsISeekableStream )uploadStream
												.queryInterface( nsISeekableStream.NS_ISEEKABLESTREAM_IID );

										long pos = seekable.tell();
										long available = uploadStream.available();

										if( available > 0 )
										{
											try
											{
												synchronized( mozilla )
												{
													in.setInputStream( uploadStream );
													requestData = in.readByteArray( available );
													rr.setContent( getRequestBody( requestData ) );
													rr.setContentType( getContentType( requestData ) );
												}
											}
											catch( Throwable e )
											{
												e.printStackTrace();
											}
											finally
											{
												seekable.seek( nsISeekableStream.NS_SEEK_SET, pos );
											}
										}
									}
								}

								final StringToStringsMap headersMap = new StringToStringsMap();
								httpChannel.visitRequestHeaders( new nsIHttpHeaderVisitor()
								{

									public void visitHeader( String header, String value )
									{
										if( !isHeaderExcluded( header ) )
										{
											headersMap.put( header, value );
										}
									}

									public nsISupports queryInterface( String sIID )
									{
										return Mozilla.queryInterface( this, sIID );
									}
								} );

								rr.setHeaders( headersMap );

								if( !browserRecordingMap.containsKey( browserComponent ) )
								{
									browserRecordingMap.put( browserComponent, new HashMap<String, RecordedRequest>() );
								}

								browserRecordingMap.get( browserComponent ).put( rr.getUrl(), rr );
							}
						}
						else
						{
							System.out.println( "HTTPObserver: Unknown event '" + sTopic + "'" );
						}
					}
					catch( Throwable e )
					{
						// ignore errors related to the querying of unsupported
						// interfaces
						if( e.getMessage().indexOf( "0x80004002" ) == -1 )
							SoapUI.logError( e );
					}
				}

				public nsISupports queryInterface( String sIID )
				{
					return Mozilla.queryInterface( this, sIID );
				}
			};

			boolean blnObserverIsWeakReference = false;
			observerService.addObserver( httpObserver, EVENT_HTTP_ON_MODIFY_REQUEST, blnObserverIsWeakReference );
		}

	}

	public static boolean isHeaderExcluded( String header )
	{
		String excluded = SoapUI.getSettings().getString( WebRecordingSettings.EXCLUDED_HEADERS, null );
		List<String> result = new ArrayList<String>();
		if( excluded != null && excluded.trim().length() > 0 )
		{
			try
			{
				StringList names = StringList.fromXml( excluded );
				for( String name : names )
				{
					result.add( name );
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
		return result.contains( header );
	}
	private static String getContentType( byte[] requestData )
	{
		String request = new String( requestData );
		String contentType = request.substring( request.indexOf( "Content-Type" ) + 14 );
		contentType = contentType.substring( 0, contentType.indexOf( "\n" ) - 1 );
		return contentType.trim();
	}

	private static String getRequestBody( byte[] requestData )
	{
		String request = new String( requestData );
		int ix = request.indexOf( "\r\n\r\n" );
		return ix == -1 ? "" : request.substring( ix + 4 );
	}

	private static final class SoapUINewWindowManager implements NewWindowManager
	{
		public NewWindowContainer evaluateWindow( NewWindowParams params )
		{
			return new NewWindowContainer()
			{
				public void insertBrowser( Browser browser )
				{
					browser.addNavigationListener( new NavigationListener()
					{
						@Override
						public void navigationStarted( final NavigationEvent arg0 )
						{
							// this is the only event we wanted
							arg0.getBrowser().removeNavigationListener( this );

							// since there is no way to detect the source browser for
							// the new window we just assume it is the recording one.
							if( BrowserComponent.recordingBrowser != null )
							{
								BrowserComponent.recordingBrowser.replaceBrowser( arg0.getBrowser() );
							}
							else
							{
								SwingUtilities.invokeLater( new Runnable()
								{

									@Override
									public void run()
									{
										if( UISupport.confirm( "Open [" + arg0.getUrl() + "] with system Browser?", "Open URL" ) )
											Tools.openURL( arg0.getUrl() );

									}
								} );

								arg0.getBrowser().dispose();
							}
						}

						@Override
						public void navigationFinished( NavigationFinishedEvent arg0 )
						{
						}
					} );
				}
			};
		}
	}

	private final class InternalBrowserNavigationListener implements NavigationListener
	{
		@Override
		public void navigationStarted( NavigationEvent arg0 )
		{
		}

		@Override
		public void navigationFinished( NavigationFinishedEvent arg0 )
		{
			if( BrowserComponent.recordingBrowser == BrowserComponent.this
					&& browserRecordingMap.containsKey( BrowserComponent.this ) )
			{
				Map<String, RecordedRequest> map = browserRecordingMap.get( BrowserComponent.this );
				RecordedRequest recordedRequest = map.get( arg0.getUrl() );
				if( recordedRequest != null )
				{
					if( httpHtmlResponseView != null && httpHtmlResponseView.isRecordHttpTrafic() )
					{
						HttpTestRequest httpTestRequest = ( HttpTestRequest )( httpHtmlResponseView.getDocument()
								.getRequest() );
						WsdlTestCase testCase = ( WsdlTestCase )httpTestRequest.getTestStep().getTestCase();
						int count = testCase.getTestStepList().size();

						String url2 = recordedRequest.getUrl();
						try
						{
							url2 = new URL( recordedRequest.getUrl() ).getPath();
						}
						catch( MalformedURLException e )
						{

						}

						HttpTestRequestStep newHttpStep = ( HttpTestRequestStep )testCase.addTestStep(
								HttpRequestStepFactory.HTTPREQUEST_TYPE, "Http Test Step " + ++count + " [" + url2 + "]",
								recordedRequest.getUrl(), recordedRequest.getMethod() );

						newHttpStep.getTestRequest().setRequestHeaders( recordedRequest.getHeaders() );

						if( recordedRequest.getContent() != null )
						{
							newHttpStep.getTestRequest().setMediaType( recordedRequest.getContentType() );
							if( newHttpStep.getTestRequest().getMediaType().equals( CONTENT_TYPE_FORM_URLENCODED ) )
							{
								newHttpStep.getTestRequest().setPostQueryString( true );
								RestUtils.extractParamsFromQueryString( newHttpStep.getTestRequest().getParams(),
										recordedRequest.getContent() );
							}
							else
							{
								newHttpStep.getTestRequest().setRequestContent( recordedRequest.getContent() );
							}
						}
					}
				}
			}

			browserRecordingMap.remove( BrowserComponent.this );
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

		browser = ( MozillaBrowser )BrowserFactory.createBrowser( BrowserType.Mozilla );
		browserMap.put( ( ( MozillaWebBrowser )browser.getPeer() ).getWebBrowser().getContentDOMWindow(), this );

		if( newWindowManager == null )
		{
			registerHttpListener();

			newWindowManager = new SoapUINewWindowManager();
			browser.getServices().setNewWindowManager( newWindowManager );
			browser.getServices().setPromptService( new DefaultPromptService() );
		}

		internalNavigationListener = new InternalBrowserNavigationListener();
		browser.addNavigationListener( internalNavigationListener );
		browser.addStatusListener( this );

		panel.add( browser.getComponent(), BorderLayout.CENTER );
		return true;
	}

	protected void replaceBrowser( Browser browser2 )
	{
		// remove old
		browserMap.remove( ( ( MozillaWebBrowser )browser.getPeer() ).getWebBrowser().getContentDOMWindow() );

		browser.stop();
		browser.removeNavigationListener( internalNavigationListener );
		browser.removeStatusListener( this );
		panel.remove( browser.getComponent() );
		browser.dispose();

		// replace
		browser = ( MozillaBrowser )browser2;
		browserMap.put( ( ( MozillaWebBrowser )browser.getPeer() ).getWebBrowser().getContentDOMWindow(), this );
		browser.addNavigationListener( internalNavigationListener );
		browser.addStatusListener( this );
		panel.add( browser.getComponent(), BorderLayout.CENTER );
	}

	public static boolean isRecording()
	{
		return httpHtmlResponseView != null && httpHtmlResponseView.isRecordHttpTrafic();
	}

	public void release()
	{
		if( browser != null )
		{
			disposed = true;
			cleanup();
		}

		possibleError = false;
	}

	private synchronized void cleanup()
	{
		if( recordingBrowser == this )
		{
			httpHtmlResponseView = null;
			recordingBrowser = null;
		}

		if( browser != null )
		{
			browserMap.remove( ( ( MozillaWebBrowser )browser.getPeer() ).getWebBrowser().getContentDOMWindow() );
			browserRecordingMap.remove( this );

			browser.stop();
			browser.dispose();
			browser.removeNavigationListener( internalNavigationListener );
			browser.removeStatusListener( this );

			panel.removeAll();
			browser = null;
		}
	}

	public void setContent( String contentAsString, String contextUri )
	{
		if( browser == null )
		{
			initBrowser();
		}

		browser.setContent( contentAsString, contextUri );
		pcs.firePropertyChange( "content", null, null );
	}

	public void setContent( String content )
	{
		if( browser == null )
		{
			initBrowser();
		}

		browser.setContent( content );
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

	/**
	 * Setups proxy configuration
	 */

	void setUpProxy()
	{
		if( isJXBrowserDisabled() )
			return;

		initialize();

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

	/**
	 * Called after a HTTP response from the server is received.
	 * 
	 * @see http://developer.mozilla.org/en/Observer_Notifications
	 */
	public static final String EVENT_HTTP_ON_MODIFY_REQUEST = "http-on-modify-request";

	public void registerHttpListener()
	{
		Xpcom.invokeLater( new RecordingHttpListener() );
	}

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

	private static class RecordedRequest
	{
		private String url;
		private String contentType;
		private StringToStringsMap headers;
		private String method;
		private String content;

		public RecordedRequest( String url, String method )
		{
			this.url = url;
			this.method = method;
		}

		public void setContentType( String contentType )
		{
			this.contentType = contentType;
		}

		public void setHeaders( StringToStringsMap headersMap )
		{
			headers = headersMap;
		}

		public void setContent( String requestBody )
		{
			content = requestBody;
		}

		public String getUrl()
		{
			return url;
		}

		public String getContentType()
		{
			return contentType;
		}

		public StringToStringsMap getHeaders()
		{
			return headers;
		}

		public String getMethod()
		{
			return method;
		}

		public String getContent()
		{
			return content;
		}
	}

}
