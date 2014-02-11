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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.net.URL;

/**
 * Implementation based on the JavaFX WebView component.
 */
public class WebViewUserBrowserFacade implements UserBrowserFacade
{

	private WebViewBasedBrowserComponent browserComponent = new WebViewBasedBrowserComponent( false );
	private JFrame popupWindow;

	@Override
	public void open( URL url )
	{
		popupWindow = new JFrame( "Browser" );
        popupWindow.setIconImages(SoapUI.getFrameIcons());

		popupWindow.getContentPane().add( browserComponent.getComponent() );
		popupWindow.setBounds( 100, 100, 800, 600 );
		popupWindow.setVisible( true );

		addBrowserStateListener( new BrowserStateChangeListener()
		{
			@Override
			public void locationChanged( String newLocation )
			{
			}

			@Override
			public void contentChanged( String newContent )
			{

			}
		} );

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

	@Override
	public void close()
	{

		try
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					popupWindow.setVisible( false );
					popupWindow.dispose();
				}
			} );
		}
		catch( Exception e )
		{
			SoapUI.log.debug( "Could not close window due to unexpected error: " + e.getMessage() + "!" );
		}

	}

}
