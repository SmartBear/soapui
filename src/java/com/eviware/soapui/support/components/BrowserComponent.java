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

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

public class BrowserComponent
{
	private JEditorPane editorPane;
	private JScrollPane scrollPane;

	// private WebBrowser browser;
	// private static WebBrowserFactory webBrowserFactory;
	//
	// static
	// {
	// Xpcom.initialize( Xpcom.AWT );
	// }

	public BrowserComponent()
	{
		editorPane = new JEditorPane();
		editorPane.setEditorKit( new HTMLEditorKit() );
		editorPane.setEditable( false );
		editorPane.addHyperlinkListener( new DefaultHyperlinkListener( editorPane ) );
	}

	public Component getComponent()
	{
		// if( browser == null )
		// {
		// initBrowser();
		// }
		//
		// return browser.getComponent();
		if( scrollPane == null )
		{
			scrollPane = new JScrollPane( editorPane );
			UISupport.addPreviewCorner( scrollPane, false );
		}
		return scrollPane;
	}

	private void initBrowser()
	{
		// if( webBrowserFactory == null )
		// webBrowserFactory = WebBrowserFactory.getInstance();
		//
		// browser = webBrowserFactory.createBrowser();
		// browser.addStatusChangeListener( new StatusChangeListener() {
		// public void statusChanged( StatusChangeEvent event )
		// {
		// // System.out.println( "Status change: " + event );
		// }
		// } );
		//
		// browser.deactivate();
	}

	public void release()
	{
		// if( browser != null )
		// browser.dispose();
		//      
		// browser = null;
	}

	public void setContent( String contentAsString, String contentType, String contextUri )
	{
		editorPane.setContentType( contentType );
		try
		{
			editorPane.read( new ByteArrayInputStream( contentAsString.getBytes() ), editorPane.getEditorKit()
					.createDefaultDocument() );
		}
		catch( IOException e )
		{
			e.printStackTrace(); // To change body of catch statement use File |
										// Settings | File Templates.
		}

		// if( browser == null )
		// {
		// initBrowser();
		// }
		// browser.setContentWithContext( contentAsString, contentType, contextUri
		// );
	}

	public void setContent( String content, String contentType )
	{
		setContent( content, contentType, null );

		// if( browser == null )
		// {
		// initBrowser();
		// }
		// browser.setContent( content, contentType );
	}

	public boolean navigate( String url, String errorPage )
	{
		try
		{
			String proxyHost = SoapUI.getSettings().getString( ProxySettings.HOST,
					System.getProperty( "http.proxyHost", null ) );
			if( StringUtils.hasContent( proxyHost ) )
			{
				System.setProperty( "http.proxyHost", proxyHost );
			}

			String proxyPort = SoapUI.getSettings().getString( ProxySettings.PORT,
					System.getProperty( "http.proxyPort", null ) );
			if( StringUtils.hasContent( proxyPort ) )
			{
				System.setProperty( "http.proxyPort", proxyPort );
			}

			editorPane.setPage( new URL( url ) );
			return true;
		}
		catch( Exception e )
		{
			SoapUI.logError( new Exception( "Failed to access [" + url + "]", e ) );
		}

		if( errorPage != null )
		{
			navigate( errorPage, null );
		}

		return false;

		// if( browser == null )
		// {
		// initBrowser();
		// }
		//
		// browser.navigate( url );
	}

	public String getContent()
	{
		// return browser == null ? null : XmlUtils.serialize(
		// browser.getDocument() );
		return editorPane.getText();
	}
}
