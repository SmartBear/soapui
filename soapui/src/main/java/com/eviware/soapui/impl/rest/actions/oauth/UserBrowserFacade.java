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

import java.net.URL;

/**
 * Defines interactions with a web browser in the context of an OAuth2
 */
interface UserBrowserFacade
{
	
	void open(URL url);

	void addBrowserStateListener(BrowserStateChangeListener listener);

	void removeBrowserStateListener(BrowserStateChangeListener listener);

	void close();

	void executeJavaScript( String script );
}
