/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
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

import com.eviware.soapui.impl.rest.actions.oauth.BrowserListener;

import javax.swing.*;
import java.awt.*;

/**
 * @author joel.jonsson
 */
class DisabledWebViewBasedBrowserComponent implements WebViewBasedBrowserComponent
{
	private JPanel panel = new JPanel( new BorderLayout() );

	DisabledWebViewBasedBrowserComponent()
	{
		JEditorPane browserDisabledPanel = new JEditorPane();
		browserDisabledPanel.setText( "Browser Component disabled" );
		panel.add( browserDisabledPanel );
		panel.setPreferredSize( new Dimension( 300, 200 ) );
	}

	@Override
	public Component getComponent()
	{
		return panel;
	}

	@Override
	public void navigate( String url )
	{
	}

	@Override
	public void setContent( String contentAsString )
	{
	}

	@Override
	public void setContent( String contentAsString, String contentType )
	{
	}

	@Override
	public void close( boolean cascade )
	{
	}

	@Override
	public void addBrowserStateListener( BrowserListener listener )
	{
	}

	@Override
	public void removeBrowserStateListener( BrowserListener listener )
	{
	}

	@Override
	public void executeJavaScript( String script )
	{
	}
}
