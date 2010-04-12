package com.eviware.soapui.support.jnlp;

import java.io.File;

public class WebstartUtilCore extends WebstartUtil
{

	public static void init()
	{
		if (isWebStart())
		{
			try
			{
				if( System.getProperty( "deployment.user.tmp" ) != null
						&& System.getProperty( "deployment.user.tmp" ).length() > 0 )
				{
					System.setProperty( "GRE_HOME", System.getProperty( "deployment.user.tmp" ) );
				}
				
				// wsi-test-tools
				System.setProperty("wsi.dir", createWebStartDirectory("wsi-test-tools", System
						.getProperty("wsitesttools.jar.url")
						)+ File.separator + "wsi-test-tools");
				System.out.println(System.getProperty("wsi.dir"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

	}
}
