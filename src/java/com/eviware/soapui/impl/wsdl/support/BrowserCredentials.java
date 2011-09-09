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
