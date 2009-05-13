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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.InterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public class RestServiceFactory implements InterfaceFactory<RestService>
{
	public final static String REST_TYPE = "rest";

	public RestService build( WsdlProject project, InterfaceConfig config )
	{
		return new RestService( project, ( RestServiceConfig )config.changeType( RestServiceConfig.type ) );
	}

	public RestService createNew( WsdlProject project, String name )
	{
		RestServiceConfig config = ( RestServiceConfig )project.getConfig().addNewInterface().changeType(
				RestServiceConfig.type );
		RestService iface = new RestService( project, config );
		iface.setName( name );

		return iface;
	}
}
