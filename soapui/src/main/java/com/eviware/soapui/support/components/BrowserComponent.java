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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.request.views.html.HttpHtmlResponseView;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.settings.WebRecordingSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.teamdev.jxbrowser.*;
import com.teamdev.jxbrowser.cookie.HttpCookieStorage;
import com.teamdev.jxbrowser.events.*;
import com.teamdev.jxbrowser.gecko.xpcom.XPCOM;
import com.teamdev.jxbrowser.gecko.xpcom.XPCOMManager;
import com.teamdev.jxbrowser.mozilla.MozillaBrowser;
import com.teamdev.jxbrowser.mozilla.MozillaCookieStorage;
import com.teamdev.jxbrowser.prompt.DefaultPromptService;
import com.teamdev.jxbrowser.proxy.*;
import com.teamdev.jxbrowser.security.HttpSecurityAction;
import com.teamdev.jxbrowser.security.HttpSecurityHandler;
import com.teamdev.jxbrowser.security.SecurityProblem;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.mozilla.interfaces.*;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

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
	@SuppressWarnings("unused")
	private boolean disposed;
	// private static boolean disabled;
	private NavigationListener internalNavigationListener;
	private HttpHtmlResponseView httpHtmlResponseView;
	private static SoapUINewWindowManager newWindowManager;
	private static Map<nsIDOMWindow, BrowserComponent> browserMap = new HashMap<nsIDOMWindow, BrowserComponent>();
	private static Map<BrowserComponent, Map<String, RecordedRequest>> browserRecordingMap = new HashMap<BrowserComponent, Map<String, RecordedRequest>>();
	private static Object recordingHttpListener;
	private final boolean addStatusBar;

	public BrowserComponent( boolean addToolbar, boolean addStatusBar )
	{
		this.addStatusBar = addStatusBar;
	}

	public Component getComponent()
	{
		if( SoapUI.isJXBrowserDisabled() )
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "Browser Component disabled or not available on this platform" );
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

				if( !initBrowser() )
					return panel;

				configureBrowser();

				browser.navigate( "about:blank" );
			}
		}
		return panel;
	}

	@SuppressWarnings("unused")
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
		this.httpHtmlResponseView = httpHtmlResponseView;
		if( httpHtmlResponseView != null )
		{
			if( !browserRecordingMap.containsKey( BrowserComponent.this ) )
			{
				browserRecordingMap.put( BrowserComponent.this, new HashMap<String, RecordedRequest>() );
			}

			// clear cookies when we start recording
			HttpCookieStorage cookies = MozillaCookieStorage.getInstance( BrowserType.Mozilla );
			cookies.deleteCookie( cookies.getCookies() );
		}
		else
		{
			browserRecordingMap.remove( BrowserComponent.this );
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

	private final class InternalHttpSecurityHandler implements HttpSecurityHandler
	{
		@Override
		public HttpSecurityAction onSecurityProblem( Set<SecurityProblem> arg0 )
		{
			return HttpSecurityAction.CONTINUE;
		}
	}

	private static final class RecordingHttpListener implements Runnable
	{
		public void run()
		{
			recordingHttpListener = this;

			final Mozilla mozilla = Mozilla.getInstance();
			nsIServiceManager serviceManager = mozilla.getServiceManager();
			nsIObserverService observerService = ( nsIObserverService )serviceManager.getServiceByContractID(
					"@mozilla.org/observer-service;1", nsIObserverService.NS_IOBSERVERSERVICE_IID );

			final nsIBinaryInputStream in = XPCOMManager.getInstance().newComponent( "@mozilla.org/binaryinputstream;1",
					nsIBinaryInputStream.class );

			nsIObserver httpObserver = new nsIObserver()
			{
				public void observe( nsISupports subject, String sTopic, String sData )
				{
					try
					{
						if( EVENT_HTTP_ON_MODIFY_REQUEST.equals( sTopic ) )
						{
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
							if( browserComponent != null && browserRecordingMap.containsKey( browserComponent ) )
							{
								RecordedRequest rr = new RecordedRequest( dumpUri( httpChannel.getURI() ),
										httpChannel.getRequestMethod() );

								nsIUploadChannel upload = ( nsIUploadChannel )httpChannel
										.queryInterface( nsIUploadChannel.NS_IUPLOADCHANNEL_IID );

								byte[] requestData = null;
								if( upload != null )
								{
									nsIInputStream uploadStream = upload.getUploadStream();

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
													String requestBody = getRequestBody( requestData );
													if( requestBody != null && requestBody.length() > 0 )
													{
														rr.setContent( requestBody );
														String contentType = getContentType( requestData );
														if( StringUtils.hasContent( contentType ) )
															rr.setContentType( contentType );
													}
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
		int ix = request.indexOf( "Content-Type" );
		if( ix >= 0 && ix < request.length() - 14 )
		{
			String contentType = request.substring( ix + 14 );
			contentType = contentType.substring( 0, contentType.indexOf( "\n" ) - 1 );
			return contentType.trim();
		}
		else
			return null;
	}

	private static String getRequestBody( byte[] requestData )
	{
		String request = new String( requestData );
		int ix = request.indexOf( "\r\n\r\n" );
		return ix == -1 ? "" : request.substring( ix + 4 );
	}

	private static final class SoapUINewWindowManager implements NewWindowManager
	{
		public NewWindowContainer evaluateWindow( final NewWindowParams params )
		{
			return new NewWindowContainer()
			{
				public void insertBrowser( final Browser browser )
				{
					browser.addNavigationListener( new NavigationListener()
					{
						public void navigationStarted( final NavigationEvent arg0 )
						{
							// this is the only event we wanted
							arg0.getBrowser().removeNavigationListener( this );

							// since there is no way to detect the source browser for
							// the new window we just assume it is the recording one.
							BrowserComponent browserComponent = null;
							if( params.getParent() instanceof MozillaBrowser )
							{
								browserComponent = browserMap.get( ( ( MozillaBrowser )params.getParent() ).getPeer()
										.getNsIWebBrowser().getContentDOMWindow() );
								if( browserRecordingMap.containsKey( browserComponent ) )
								{
									browserComponent.replaceBrowser( arg0.getBrowser() );
								}
								else
									browserComponent = null;
							}

							if( browserComponent == null )
							{
								Tools.openURL( arg0.getUrl() );
							}
						}

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
		public void navigationStarted( NavigationEvent arg0 )
		{
		}

		public void navigationFinished( NavigationFinishedEvent arg0 )
		{
			if( browserRecordingMap.containsKey( BrowserComponent.this ) )
			{
				Map<String, RecordedRequest> map = browserRecordingMap.get( BrowserComponent.this );
				RecordedRequest recordedRequest = map.get( arg0.getUrl() );
				if( recordedRequest != null )
				{
					if( httpHtmlResponseView != null && httpHtmlResponseView.isRecordHttpTrafic() )
					{
						HttpTestRequest httpTestRequest = ( HttpTestRequest )( httpHtmlResponseView.getDocument()
								.getRequest() );
						WsdlTestCase testCase = httpTestRequest.getTestStep().getTestCase();
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
								newHttpStep.getTestRequest().setMediaType( CONTENT_TYPE_FORM_URLENCODED );
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
			// TODO ask Ole why was this removing necessary

			// browserRecordingMap.remove( BrowserComponent.this );
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
		if( browser != null || SoapUI.isJXBrowserDisabled() )
			return false;

		try
		{
			browser = ( MozillaBrowser )BrowserFactory.createBrowser( BrowserType.Mozilla );
			browserMap.put( browser.getPeer().getNsIWebBrowser().getContentDOMWindow(), this );

			initNewWindowManager( browser, true );

			internalHttpSecurityHandler = new InternalHttpSecurityHandler();
			browser.setHttpSecurityHandler( internalHttpSecurityHandler );

			internalNavigationListener = new InternalBrowserNavigationListener();
			browser.addNavigationListener( internalNavigationListener );
			browser.addStatusListener( this );

			internalNavigationAdapter = new InternalNavigationAdapter();
			browser.addNavigationListener( internalNavigationAdapter );

			panel.add( browser.getComponent(), BorderLayout.CENTER );

			return true;
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
			return false;
		}
	}

	public static void initNewWindowManager( Browser browser, boolean forRecording )
	{
		if( newWindowManager == null )
		{
			newWindowManager = new SoapUINewWindowManager();
			browser.getServices().setNewWindowManager( newWindowManager );
			browser.getServices().setPromptService( new DefaultPromptService() );
		}

		if( forRecording && recordingHttpListener == null )
			registerHttpListener();
	}

	protected void replaceBrowser( Browser browser2 )
	{
		// remove old
		browserMap.remove( browser.getPeer().getNsIWebBrowser().getContentDOMWindow() );

		browser.stop();
		browser.removeNavigationListener( internalNavigationListener );
		browser.removeStatusListener( this );
		panel.remove( browser.getComponent() );
		browser.dispose();

		// replace
		browser = ( MozillaBrowser )browser2;
		browserMap.put( browser.getPeer().getNsIWebBrowser().getContentDOMWindow(), this );
		browser.addNavigationListener( internalNavigationListener );
		browser.addStatusListener( this );
		panel.add( browser.getComponent(), BorderLayout.CENTER );
	}

	// public static boolean isRecording()
	// {
	// return httpHtmlResponseView != null &&
	// httpHtmlResponseView.isRecordHttpTrafic();
	// }

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
		if( browser != null )
		{
			browserMap.remove( browser.getPeer().getNsIWebBrowser().getContentDOMWindow() );
			browserRecordingMap.remove( this );
			httpHtmlResponseView = null;

			browser.stop();
			browser.dispose();
			browser.removeNavigationListener( internalNavigationListener );
			browser.removeNavigationListener( internalNavigationAdapter );
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
		if( SoapUI.isJXBrowserDisabled() )
			return;

		if( browser == null )
		{
			if( !initBrowser() )
				return;
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
		if( SoapUI.isJXBrowserDisabled() )
			return;

		if( browser == null )
		{
			if( !initBrowser() )
				return;
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

	private static boolean proxyAuthenticationInitialized = false;

	public static void updateJXBrowserProxy()
	{
		ProxyConfig proxyConf = BrowserServices.getInstance().getProxyConfig();
		if( proxyConf == null )
			return;

		try
		{
			proxyConf.setAutoDetectForNetwork( false );

			if( !proxyAuthenticationInitialized )
			{
				proxyConf.setAuthenticationHandler( ServerType.HTTP, new AuthenticationHandler()
				{
					@Override
					public ProxyServerLogin authenticationRequired( ServerType arg0 )
					{
						Settings settings = SoapUI.getSettings();
						PropertyExpansionContext context = null;

						String proxyUsername = PropertyExpander.expandProperties( context,
								settings.getString( ProxySettings.USERNAME, null ) );
						String proxyPassword = PropertyExpander.expandProperties( context,
								settings.getString( ProxySettings.PASSWORD, null ) );

						return new ProxyServerLogin( proxyUsername, proxyPassword );
					}
				} );

				proxyAuthenticationInitialized = true;
			}

			if( ProxyUtils.isProxyEnabled() )
			{
				if( ProxyUtils.isAutoProxy() )
				{
					HttpRoutePlanner routePlanner = HttpClientSupport.getHttpClient().getRoutePlanner();
					HttpRoute httpRoute = routePlanner.determineRoute( new HttpHost( "soapui.org" ), new HttpGet( "http://soapui.org" ), null );

					HttpHost proxyHost = httpRoute.getProxyHost();

					if( proxyHost != null )
					{
						proxyConf.setProxy( ServerType.HTTP, new ProxyServer( proxyHost.getHostName(), proxyHost.getPort() ) );
					}
					else
					{
						proxyConf.setDirectConnection();
					}
				}
				else
				{
					Settings settings = SoapUI.getSettings();
					PropertyExpansionContext context = null;

					// check system properties first
					String proxyHost = System.getProperty( "http.proxyHost" );
					String proxyPort = System.getProperty( "http.proxyPort" );

					if( proxyHost == null )
						proxyHost = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.HOST, "" ) );

					if( proxyPort == null )
						proxyPort = PropertyExpander.expandProperties( context, settings.getString( ProxySettings.PORT, "" ) );

					proxyConf.setProxy( ServerType.HTTP, new ProxyServer( proxyHost, Integer.parseInt( proxyPort ) ) );
					// check excludes
					proxyConf.setExceptions( PropertyExpander.expandProperties( context,
							settings.getString( ProxySettings.EXCLUDES, "" ) ) );
				}
			}
			else
			{
				proxyConf.setDirectConnection();
			}
		}
		catch( Exception e )
		{
			//ignore
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
	private InternalHttpSecurityHandler internalHttpSecurityHandler;
	private InternalNavigationAdapter internalNavigationAdapter;

	public static void registerHttpListener()
	{
		XPCOM.invokeLater( new RecordingHttpListener() );
	}

	/**
	 * Converts an object implementing the nsIURI interface into a human readable
	 * URI.
	 *
	 * @param uri nsIURI object to convert
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

	public void navigate( String url, String postData, String errorPage )
	{
		if( SoapUI.isJXBrowserDisabled() )
			return;

		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;

		if( browser == null )
		{
			if( !initBrowser() )
				return;
		}

		configureBrowser();
		updateJXBrowserProxy();

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
