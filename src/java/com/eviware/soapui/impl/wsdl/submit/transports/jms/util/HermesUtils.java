/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.submit.transports.jms.util;

import hermes.HermesInitialContextFactory;
import hermes.JAXBHermesLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.HermesJMSClasspathHacker;

public class HermesUtils
{
	private static boolean hermesJarsLoaded = false;
	private static Map<String,Context> contextMap = new HashMap<String,Context>();
	public static String HERMES_CONFIG_XML="hermes-config.xml";

	
	public static Context hermesContext(WsdlProject project) throws NamingException, MalformedURLException, IOException
	{
		String expandedHermesConfigPath=PropertyExpander.expandProperties(project,project.getHermesConfig());
		String key = project.getName()+expandedHermesConfigPath;
		return getHermes( key, expandedHermesConfigPath);
	}
	
	public static Context hermesContext(WsdlProject project, String hermesConfigPath) throws NamingException, MalformedURLException, IOException
	{
		String expandedHermesConfigPath=PropertyExpander.expandProperties(project,hermesConfigPath);
		String key = project.getName()+expandedHermesConfigPath;
		return getHermes( key, expandedHermesConfigPath);
	}

	private static Context getHermes( String key,String hermesConfigPath) throws IOException, MalformedURLException,
			NamingException
	{
		if (!hermesJarsLoaded)
		{
			addHermesJarsToClasspath();
			hermesJarsLoaded = true;
		}
		
		if(contextMap.containsKey(key)){
			return contextMap.get(key);
		}
		
	
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
		props.put(Context.PROVIDER_URL, hermesConfigPath + File.separator+HERMES_CONFIG_XML);
		props.put("hermes.loader", JAXBHermesLoader.class.getName());

		Context ctx = new InitialContext(props);
		contextMap.put(key, ctx);
		return ctx;
	}
	
	
	
	
// TODO: this could be called on souapui startup if hermes config path is set
	private static void addHermesJarsToClasspath() throws IOException, MalformedURLException
	{
		String hermesLib = SoapUI.getSettings().getString(ToolsSettings.HERMES_1_13, null) + File.separator + "lib";

		if (hermesLib == null || "".equals(hermesLib))
		{
			throw new FileNotFoundException("HermesJMS home not specified !!!");
		}

		File dir = new File(hermesLib);

		String[] children = dir.list();
		for (String filename : children)
		{
			HermesJMSClasspathHacker.addURL(new URL("file:" + File.separator + hermesLib + File.separator + filename));
		}

	}
}
