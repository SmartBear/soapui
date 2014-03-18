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

import com.eviware.soapui.SoapUI;

/**
 * @author joel.jonsson
 */
public class WebViewBasedBrowserComponentFactory
{
	/*
	This factory  resides in a separate class to avoid ClassNotFoundException when jfxrt.jar in unavailable
	and browser component is disabled.
	 */
	public static WebViewBasedBrowserComponent createBrowserComponent( boolean addNavigationBar )
	{
		if( SoapUI.isBrowserDisabled() )
		{
			return new DisabledWebViewBasedBrowserComponent();
		}
		else
		{
			return new EnabledWebViewBasedBrowserComponent( addNavigationBar );
		}

	}
}
