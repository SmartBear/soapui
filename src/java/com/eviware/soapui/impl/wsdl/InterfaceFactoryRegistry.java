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

package com.eviware.soapui.impl.wsdl;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.InterfaceFactory;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.support.StringUtils;

public class InterfaceFactoryRegistry
{
	private static Map<String, InterfaceFactory<?>> factories = new HashMap<String, InterfaceFactory<?>>();

	static
	{
		factories.put( WsdlInterfaceFactory.WSDL_TYPE, new WsdlInterfaceFactory() );
		factories.put( RestServiceFactory.REST_TYPE, new RestServiceFactory() );
	}

	public static AbstractInterface<?> createNew( WsdlProject project, String type, String name )
	{
		if( !factories.containsKey( type ) )
			throw new RuntimeException( "Unknown interface type [" + type + "]" );

		return factories.get( type ).createNew( project, name );
	}

	public static AbstractInterface<?> build( WsdlProject project, InterfaceConfig config )
	{
		String type = config.getType();
		if( StringUtils.isNullOrEmpty( type ) )
			type = WsdlInterfaceFactory.WSDL_TYPE;

		return factories.get( type ).build( project, config );
	}
}
