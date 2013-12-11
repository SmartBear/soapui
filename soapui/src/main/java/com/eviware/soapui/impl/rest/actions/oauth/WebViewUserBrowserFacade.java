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

import javax.swing.JFrame;
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
				browserComponent.executeJavaScript( javaScriptToFocusFirstInputElementInTheForm() );
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
		popupWindow.dispose();
	}

	private String javaScriptToFocusFirstInputElementInTheForm()
	{
		return "var bFound = false;\n" +
				"\n" +
				"  for (f=0; f < document.forms.length; f++)\n" +
				"  {\n" +
				"    for(i=0; i < document.forms[f].length; i++)\n" +
				"    {\n" +
				"      if (document.forms[f][i].type != \"hidden\" && document.forms[f][i].disabled != true)\n" +
				"      {" +
				"        document.forms[f][i].focus();\n" +
				"        var bFound = true;\n" +
				"      }\n" +
				"      if (bFound == true)\n" +
				"        break;\n" +
				"    }\n" +
				"    if (bFound == true)\n" +
				"      break;\n" +
				"  }";
	}
}
