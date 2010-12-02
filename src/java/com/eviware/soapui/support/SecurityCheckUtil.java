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
package com.eviware.soapui.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.GlobalPropertySettings;

public class SecurityCheckUtil
{

	public static List<String> entriesList()
	{
		Settings settings = SoapUI.getSettings();
		String temp = settings.getString( GlobalPropertySettings.SECURITY_CHECKS_PROPERTIES, null );
		PropertiesTypeConfig config;
		try
		{
			config = PropertiesTypeConfig.Factory.parse( temp );
			List<String> contentList = new ArrayList<String>();
			for( PropertyConfig pc : config.getPropertyList() )
			{
				contentList.add( pc.getValue() );
			}
			return contentList;
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
			return null;
		}

	}

}
