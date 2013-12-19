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
import com.eviware.soapui.impl.rest.actions.oauth.BrowserStateChangeListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWebProgress;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringWriter;
import java.util.ArrayList;

public class WebViewBasedBrowserComponent
{
	private JPanel panel = new JPanel( new BorderLayout() );
	private String errorPage;
	private boolean showingErrorPage;
	public String url;
	private Boolean possibleError = false;
	private final boolean addStatusBar;
	private PropertyChangeSupport pcs = new PropertyChangeSupport( this );

	private java.util.List<BrowserStateChangeListener> listeners = new ArrayList<BrowserStateChangeListener>();

	public WebView webView;

	public WebViewBasedBrowserComponent( boolean addStatusBar )
	{
		this.addStatusBar = addStatusBar;
	}

	public Component getComponent()
	{
		if( webView == null )
		{
			if( addStatusBar )
			{
				JPanel statusBar = new JPanel( new BorderLayout() );
				JLabel statusLabel = new JLabel();
				UISupport.setFixedSize( statusBar, new Dimension( 20, 20 ) );
				statusBar.add( statusLabel, BorderLayout.WEST );
				panel.add( statusBar, BorderLayout.SOUTH );
			}

			final JFXPanel browserPanel = new JFXPanel();
			panel.add( browserPanel, BorderLayout.CENTER );

			Platform.runLater( new Runnable()
			{
				public void run()
				{
					webView = new WebView();
					webView.getEngine().getLoadWorker().stateProperty().addListener(
							new ChangeListener<Worker.State>()
							{
								@Override
								public void changed( ObservableValue ov, Worker.State oldState, Worker.State newState )
								{
									if( newState == Worker.State.SUCCEEDED )
									{
										try
										{
											String location = getWebEngine().getLocation();
											for( BrowserStateChangeListener listener : listeners )
											{
												listener.locationChanged( location );
											}

											if( getWebEngine().getDocument() != null )
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

												String output = stringWriter.getBuffer().toString().replaceAll( "\n|\r", "" );

												for( BrowserStateChangeListener listener : listeners )
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
					Group jfxComponentGroup = new Group();
					Scene scene = new Scene( jfxComponentGroup );
					webView.prefWidthProperty().bind( scene.widthProperty() );
					webView.prefHeightProperty().bind( scene.heightProperty() );
					jfxComponentGroup.getChildren().add( webView );
					browserPanel.setScene( scene );
					addKeybaordFocusManager( browserPanel );
				}
			} );

		}

		return panel;
	}

	private void addKeybaordFocusManager( final JFXPanel browserPanel )
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

	public void executeJavaScript( String script )
	{
		getWebEngine().executeScript( script );
	}

	// TODO: Evaluate whether these should be used
	private class BackAction extends AbstractAction
	{
		public BackAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_left.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to previous selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			WebHistory history = getWebEngine().getHistory();
			if( history.getCurrentIndex() == 0 )
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				history.go( history.getCurrentIndex() - 1 );
			}
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
			WebHistory history = getWebEngine().getHistory();
			if( history.getCurrentIndex() >= history.getEntries().size() - 1 )
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				history.go( history.getCurrentIndex() + 1 );
			}
		}
	}

	public void release()
	{
		// TODO: Check whether we need to do anything here
		possibleError = false;
	}


	public void setContent( String contentAsString, String contextUri )
	{
		getWebEngine().loadContent( contentAsString, contextUri );
	}

	public void setContent( String content )
	{
		getWebEngine().loadContent( content );
		pcs.firePropertyChange( "content", null, content );
	}

	private WebEngine getWebEngine()
	{
		return webView.getEngine();
	}

	public void navigate( String url, String errorPage )
	{
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

	// TODO: Check whether we need to do anything here

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


	private void showErrorPage()
	{
		if( errorPage != null && !errorPage.equals( getUrl() ) )
		{
			try
			{
				showingErrorPage = true;
				navigate( errorPage, null );
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


	public void addBrowserStateListener( BrowserStateChangeListener listener )
	{
		listeners.add( listener );
	}

	public void removeBrowserStateListener( BrowserStateChangeListener listener )
	{
		listeners.remove( listener );
	}
}
