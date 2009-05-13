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

package com.eviware.soapui.model.support;

import org.apache.commons.beanutils.PropertyUtils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.TestProperty;

public class XPathReferenceImpl implements XPathReference
{
	private String label;
	private TestProperty targetProperty;
	private Object target;
	private String xpathPropertyName;
	private String xpath;

	public XPathReferenceImpl( String label, TestProperty targetProperty, Object target, String xpathPropertyName )
	{
		this.label = label;
		this.targetProperty = targetProperty;
		this.target = target;
		this.xpathPropertyName = xpathPropertyName;

		try
		{
			this.xpath = ( String )PropertyUtils.getProperty( target, xpathPropertyName );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public String getLabel()
	{
		return label;
	}

	public TestProperty getTargetProperty()
	{
		return targetProperty;
	}

	public String getXPath()
	{
		return xpath;
	}

	public void setXPath( String xpath )
	{
		this.xpath = xpath;
	}

	public void update()
	{
		try
		{
			PropertyUtils.setProperty( target, xpathPropertyName, xpath );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}
}
