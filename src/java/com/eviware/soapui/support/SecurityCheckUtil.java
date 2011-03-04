/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.security.assertion.SensitiveInfoExposureAssertion;
import com.eviware.soapui.security.panels.ProjectSensitiveInformationPanel;
import com.eviware.soapui.settings.GlobalPropertySettings;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;

public class SecurityCheckUtil
{

	public static List<String> globalEntriesList()
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

	public static boolean contains( SubmitContext context, String content, String token, boolean useRegEx )

	{
		if( token == null )
			token = "";
		String replToken = PropertyExpander.expandProperties( context, token );

		if( replToken.length() > 0 )
		{
			int ix = -1;

			if( useRegEx )
			{
				if( content.matches( replToken ) )
					ix = 0;
			}
			else
			{
				ix = content.toUpperCase().indexOf( replToken.toUpperCase() );
			}

			if( ix == -1 )
				return false;
		}

		return true;
	}

	public static RestParamsPropertyHolder getSoapRequestParams( AbstractHttpRequest<?> request )
	{
		XmlBeansRestParamsTestPropertyHolder holder = new XmlBeansRestParamsTestPropertyHolder( request,
				RestParametersConfig.Factory.newInstance() );
		try
		{
			XmlObject requestXml = XmlObject.Factory.parse( request.getRequestContent(), new XmlOptions()
					.setLoadStripWhitespace().setLoadStripComments() );
			Node[] nodes = XmlUtils.selectDomNodes( requestXml, "//text()" );

			for( Node node : nodes )
			{
				String xpath = XmlUtils.createXPath( node.getParentNode() );
				RestParamProperty property = holder.addProperty( node.getParentNode().getNodeName() );
				property.setValue( node.getNodeValue() );
				property.setPath( xpath );
			}
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
		}
		return holder;
	}

	public static List<String> projectEntriesList( SensitiveInfoExposureAssertion sensitiveInfoExposureAssertion )
	{
		Project project = ModelSupport.getModelItemProject( sensitiveInfoExposureAssertion );
		AbstractWsdlModelItem<ModelItemConfig> modelItem = ( AbstractWsdlModelItem<ModelItemConfig> )project
				.getModelItem();
		// ProjectConfig config =
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( ( ( ProjectConfig )modelItem.getConfig() )
				.getSensitiveInformation() );
		String[] strngArray = reader.readStrings( ProjectSensitiveInformationPanel.PROJECT_SPECIFIC_EXPOSURE_LIST );
		if( strngArray != null )
		{
			return StringUtils.toStringList( strngArray );
		}
		else
		{
			return new StringList();
		}

	}

}
