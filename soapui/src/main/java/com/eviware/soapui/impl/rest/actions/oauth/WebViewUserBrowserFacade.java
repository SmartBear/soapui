/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;

import javax.swing.*;
import java.net.URL;

/**
 *
 */
public class WebViewUserBrowserFacade implements UserBrowserFacade
{

	private WebViewBasedBrowserComponent browserComponent = new WebViewBasedBrowserComponent( false );

	@Override
	public void open( URL url )
	{
		JFrame popupWindow = new JFrame( "Browser" );

		popupWindow.getContentPane().add( browserComponent.getComponent() );
		popupWindow.setBounds(100, 100, 800, 600);
		popupWindow.setVisible( true );

		browserComponent.navigate( url.toString(), null );

	}

	@Override
	public void addBrowserStateListener( BrowserStateChangeListener listener )
	{
		browserComponent.addBrowserStateListener( listener );
	}

	@Override
	public void removeBrowserStateListener( BrowserStateChangeListener listener )
	{
		browserComponent.removeBrowserStateListener( listener );
	}

}
