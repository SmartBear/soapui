/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support;

import com.teamdev.jxbrowser.BrowserServices;
import com.teamdev.jxbrowser.prompt.CloseStatus;
import com.teamdev.jxbrowser.prompt.DefaultPromptService;
import com.teamdev.jxbrowser.prompt.LoginParams;

public class BrowserCredentials
{

	public static void initBrowserCredentials( String username, String password )
	{
		BrowserServices browserServices = BrowserServices.getInstance();
		SoapUIBrowserPromptService promptService = new SoapUIBrowserPromptService( username, password );
		browserServices.setPromptService( promptService );
	}

	public static class SoapUIBrowserPromptService extends DefaultPromptService
	{
		private String username;
		private String password;

		public SoapUIBrowserPromptService( String username, String password )
		{
			this.username = username;
			this.password = password;
		}

		@Override
		public CloseStatus loginRequested( LoginParams params )
		{
			params.setUserName( username );
			params.setPassword( password );
			return CloseStatus.OK;
		}
	}

}
