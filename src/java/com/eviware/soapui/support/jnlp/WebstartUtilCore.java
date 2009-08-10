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
