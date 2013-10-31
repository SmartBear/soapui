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

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.testsuite.TestProperty;

import java.util.Map;
import java.util.Properties;

public interface RestParamsPropertyHolder extends MutableTestPropertyHolder, Map<String, TestProperty>
{

	public RestParamProperty getProperty( String name );

	public void resetValues();

	public int getPropertyIndex( String name );

	public void saveTo( Properties props );

	public RestParamProperty getPropertyAt( int index );

	public PropertyExpansion[] getPropertyExpansions();

	public void setPropertiesLabel( String propertiesLabel );

	public RestParamProperty addProperty( String name );

	public RestParamProperty removeProperty( String propertyName );

	public RestParamProperty get( Object key );

	public void addParameter( RestParamProperty prop );

	void setParameterLocation( RestParamProperty parameter, NewRestResourceActionBase.ParamLocation newLocation );

	/**
	 * Internal property class
	 * 
	 * @author ole
	 */

	public enum ParameterStyle
	{
		MATRIX, HEADER, QUERY, TEMPLATE, PLAIN
	}

}
