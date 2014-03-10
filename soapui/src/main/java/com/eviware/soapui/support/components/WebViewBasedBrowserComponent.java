/*
 *  soapUI, copyright (C) 2004-2011 smartbear.com 
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.actions.oauth.BrowserListener;
import com.eviware.soapui.support.xml.XmlUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewBasedBrowserComponent
{
	public static final String CHARSET_PATTERN = "(.+)(;\\s*charset=)(.+)";
	private Pattern charsetFinderPattern = Pattern.compile( CHARSET_PATTERN );

	private JPanel panel = new JPanel( new BorderLayout() );
	private String errorPage;
	private boolean showingErrorPage;
	public String url;
	private PropertyChangeSupport pcs = new PropertyChangeSupport( this );

	private java.util.List<BrowserListener> listeners = new ArrayList<BrowserListener>();

	public WebView webView;
	private WebViewNavigationBar navigationBar;
	private String lastLocation;
	private Set<BrowserWindow> browserWindows = new HashSet<BrowserWindow>();

	private JFXPanel browserPanel;

	public WebViewBasedBrowserComponent( boolean addNavigationBar )
	{
		if( SoapUI.isBrowserDisabled() )
		{
			JEditorPane browserDisabledPanel = new JEditorPane();
			browserDisabledPanel.setText( "Browser Component disabled" );
			panel.add( browserDisabledPanel );
		}
		else
		{
			initializeWebView( addNavigationBar );
		}
	}

	public Component getComponent()
	{
		return panel;
	}

	private void initializeWebView( boolean addNavigationBar )
	{
		if( addNavigationBar )
		{
			navigationBar = new WebViewNavigationBar();
			panel.add( navigationBar.getComponent(), BorderLayout.NORTH );
		}

		final JFXPanel browserPanel = new JFXPanel();
		panel.add( browserPanel, BorderLayout.CENTER );

		WebViewInitialization webViewInitialization = new WebViewInitialization( browserPanel );
		if( Platform.isFxApplicationThread() )
		{
			webViewInitialization.run();
		}
		else
		{
			Platform.runLater( webViewInitialization );
		}
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(500);
				}
				catch( InterruptedException ignore )
				{

				}
				if( navigationBar != null )
				{
					navigationBar.focusUrlField();
				}
			}
		};
		SwingUtilities.invokeLater( runnable );
	}

	private String readDocumentAsString() throws TransformerException
	{
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
		transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
		transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
		transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );

		StringWriter stringWriter = new StringWriter();
		transformer.transform( new DOMSource( getWebEngine().getDocument() ),
				new StreamResult( stringWriter ) );

		return stringWriter.getBuffer().toString().replaceAll( "\n|\r", "" );
	}

	private void addKeyboardFocusManager( final JFXPanel browserPanel )
	{
		KeyboardFocusManager kfm = DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher( new KeyEventDispatcher()
		{
			@Override
			public boolean dispatchKeyEvent( KeyEvent e )
			{
				if( DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == browserPanel )
				{
					if( e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == 10 )
					{
						e.setKeyChar( ( char )13 );
					}
				}
				return false;
			}
		}
		);
	}

	public void executeJavaScript( final String script )
	{
		Platform.runLater( new Runnable()
		{
			public void run()
			{
				try
				{
					webView.getEngine().executeScript( script );
					for( BrowserListener listener : listeners )
					{
						listener.javaScriptExecuted( script, null, null );
					}
				}
				catch( Exception e )
				{
					SoapUI.log.warn( "Error executing JavaScript [" + script + "]", e );
					for( BrowserListener listener : listeners )
					{
						listener.javaScriptExecuted( script, lastLocation, e );
					}
				}
			}
		} );
	}

	public void handleClose( boolean cascade )
	{
		if( cascade )
		{
			for( Iterator<BrowserWindow> iterator = browserWindows.iterator(); iterator.hasNext(); )
			{
				iterator.next().close();
				iterator.remove();
			}
		}

		for( BrowserListener listener : listeners )
		{
			listener.browserClosed();
		}
	}

	public void release()
	{
		setContent( "" );
		browserPanel.setScene(null);
	}


	public void setContent( final String contentAsString, final String contentType )
	{
		if( SoapUI.isBrowserDisabled() )
		{
			return;
		}
		Platform.runLater( new Runnable()
		{
			public void run()
			{

				getWebEngine().loadContent( contentAsString, removeCharsetFrom( contentType ) );
			}
		} );
	}

	private String removeCharsetFrom( String contentType )
	{
		Matcher matcher = charsetFinderPattern.matcher( contentType );
		return matcher.matches() ? matcher.group( 1 ) : contentType;
	}

	public void setContent( final String contentAsString )
	{
		if( SoapUI.isBrowserDisabled() )
		{
			return;
		}
		Platform.runLater( new Runnable()
		{
			public void run()
			{
				getWebEngine().loadContent( contentAsString );
			}
		} );
		pcs.firePropertyChange( "content", null, contentAsString );
	}

	private WebEngine getWebEngine()
	{
		return webView.getEngine();
	}

	public void navigate( String url, String errorPage )
	{
		if( SoapUI.isBrowserDisabled() )
		{
			return;
		}
		navigate( url, null, errorPage );
	}

	public String getContent()
	{
		return webView == null ? null : XmlUtils.serialize( getWebEngine().getDocument() );
	}


	public String getUrl()
	{
		return url;
	}

	public String getErrorPage()
	{
		return errorPage;
	}


	public void setErrorPage( String errorPage )
	{
		this.errorPage = errorPage;
	}

	public void addPropertyChangeListener( PropertyChangeListener pcl )
	{
		pcs.addPropertyChangeListener( pcl );
	}


	public void removePropertyChangeListener( PropertyChangeListener pcl )
	{
		pcs.removePropertyChangeListener( pcl );
	}


	public void navigate( final String url, String postData, String errorPage )
	{

		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;

		Platform.runLater( new Runnable()
		{
			public void run()
			{
				getWebEngine().load( url );
			}
		} );

		if( showingErrorPage )
			showingErrorPage = false;
	}

	public void addBrowserStateListener( BrowserListener listener )
	{
		listeners.add( listener );
	}

	public void removeBrowserStateListener( BrowserListener listener )
	{
		listeners.remove( listener );
	}

	/*
	Class used to open a new browser window, used in the popup handler of the component.
	 */

	private class BrowserWindow extends JFrame
	{

		private final WebViewBasedBrowserComponent browser;

		private BrowserWindow( PopupFeatures popupFeatures ) throws HeadlessException
		{
			setIconImages( SoapUI.getFrameIcons() );
			browser = new WebViewBasedBrowserComponent( popupFeatures.hasToolbar() );
			getContentPane().setLayout( new BorderLayout() );
			getContentPane().add( browser.getComponent() );
			addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( WindowEvent e )
				{
					browser.handleClose( false );
				}
			} );
		}

		public void close()
		{
			setVisible( false );
			dispose();
			browser.handleClose( true );
			browser.release();
		}

	}

	/*
	The task to initialize the Java FX WebView component. Will be run synchronously if we're already on the
	Java FX event thread, asynchronously if we aren't.
	 */

	private class WebViewInitialization implements Runnable
	{

		public WebViewInitialization( JFXPanel browserPanel )
		{
			WebViewBasedBrowserComponent.this.browserPanel = browserPanel;
		}

		public void run()
		{
			webView = new WebView();

			createPopupHandler();
			listenForLocationChanges();
			listenForStateChanges();

			if( navigationBar != null )
			{
				navigationBar.initialize( getWebEngine(), WebViewBasedBrowserComponent.this );
			}
			browserPanel.setScene( createJfxScene() );
			addKeyboardFocusManager( browserPanel );
		}

		private void createPopupHandler()
		{
			webView.getEngine().setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>()
			{
				@Override
				public WebEngine call( PopupFeatures pf )
				{
					BrowserWindow popupWindow = new BrowserWindow( pf );
					browserWindows.add( popupWindow );
					popupWindow.setSize( 800, 600 );
					popupWindow.setVisible( true );
					final WebEngine webEngine = popupWindow.browser.getWebEngine();
					return webEngine;
				}
			} );
		}

		private void listenForLocationChanges()
		{
			webView.getEngine().locationProperty().addListener( new ChangeListener<String>()
			{
				@Override
				public void changed( ObservableValue<? extends String> observableValue, String oldLocation,
											String newLocation )
				{
					lastLocation = newLocation;
					for( BrowserListener listener : listeners )
					{
						listener.locationChanged( newLocation );
					}
				}
			} );
		}

		private void listenForStateChanges()
		{
			webView.getEngine().getLoadWorker().stateProperty().addListener(
					new ChangeListener<Worker.State>()
					{
						@Override
						public void changed( ObservableValue value, Worker.State oldState, Worker.State newState )
						{
							if( newState == Worker.State.SUCCEEDED )
							{
								try
								{
									if( getWebEngine().getDocument() != null )
									{
										String output = readDocumentAsString();
										for( BrowserListener listener : listeners )
										{
											listener.contentChanged( output );
										}
									}
								}
								catch( Exception ex )
								{
									SoapUI.logError( ex, "Error processing state change to " + newState );
								}
							}
						}
					} );
		}

		private Scene createJfxScene()
		{
			Group jfxComponentGroup = new Group();
			Scene scene = new Scene( jfxComponentGroup );
			webView.prefWidthProperty().bind( scene.widthProperty() );
			webView.prefHeightProperty().bind( scene.heightProperty() );
			jfxComponentGroup.getChildren().add( webView );
			return scene;
		}
	}
}
