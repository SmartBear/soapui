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

package com.eviware.soapui.model.project;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;

public class ProjectFactoryRegistry
{
	private static Map<String, ProjectFactory<?>> factories = new HashMap<String, ProjectFactory<?>>();

	static
	{
		factories.put( WsdlProjectFactory.WSDL_TYPE, new WsdlProjectFactory() );
	}

	public static ProjectFactory<?> getProjectFactory( String projectType )
	{
		return factories.get( projectType );
	}

	public static void registrerProjectFactory( String projectType, ProjectFactory<?> projectFactory )
	{
		factories.put( projectType, projectFactory );
	}
}
