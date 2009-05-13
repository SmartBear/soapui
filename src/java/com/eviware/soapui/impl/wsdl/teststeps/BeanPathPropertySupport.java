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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;

public class BeanPathPropertySupport extends AbstractPathPropertySupport
{
	private Object config;

	public BeanPathPropertySupport( AbstractWsdlModelItem<?> modelItem, String propertyName )
	{
		this( modelItem, modelItem.getConfig(), propertyName );
	}

	public BeanPathPropertySupport( AbstractWsdlModelItem<?> modelItem, Object config, String propertyName )
	{
		super( modelItem, propertyName );
		this.config = config;
	}

	public void setPropertyValue( String value ) throws IllegalAccessException, InvocationTargetException
	{
		BeanUtils.setProperty( config, getPropertyName(), value );
	}

	public String getPropertyValue() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		return BeanUtils.getProperty( config, getPropertyName() );
	}

	public void setConfig( Object config )
	{
		this.config = config;
	}
}