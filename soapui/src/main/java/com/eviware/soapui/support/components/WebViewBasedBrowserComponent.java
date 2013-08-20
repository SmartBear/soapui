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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import javafx.application.Platform;
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WebViewBasedBrowserComponent
{
	private JPanel panel = new JPanel( new BorderLayout() );
	private String errorPage;
	private boolean showingErrorPage;
	public String url;
	private Boolean possibleError = false;
	private final boolean addStatusBar;
	private PropertyChangeSupport pcs = new PropertyChangeSupport( this );

	private WebView webView;

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
					Group jfxComponentGroup = new Group();
					Scene scene = new Scene( jfxComponentGroup );
					jfxComponentGroup.getChildren().add( webView );
					browserPanel.setScene( scene );
				}
			} );

		}
		return panel;
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
			if( history.getCurrentIndex () == 0 )
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				history.go(history.getCurrentIndex() - 1);
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
			if( history.getCurrentIndex () >= history.getEntries().size() -1 )
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
		pcs.firePropertyChange( "content", null, null );
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
}
