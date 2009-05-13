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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.support.definition.support.XmlSchemaBasedInterfaceDefinition;
import com.eviware.soapui.impl.wsdl.WsdlInterface;

public class WsdlInterfaceDefinition extends XmlSchemaBasedInterfaceDefinition<WsdlInterface>
{
	private Definition definition;

	private static WSDLFactory factory;
	private static WSDLReader wsdlReader;
	private Logger log = Logger.getLogger( WsdlInterfaceDefinition.class );

	public WsdlInterfaceDefinition( WsdlInterface iface )
	{
		super( iface );
	}

	public WsdlInterfaceDefinition load( WsdlDefinitionLoader loader ) throws Exception
	{
		if( factory == null )
		{
			factory = WSDLFactory.newInstance();
			wsdlReader = factory.newWSDLReader();
			wsdlReader.setFeature( "javax.wsdl.verbose", true );
			wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
		}

		definition = wsdlReader.readWSDL( loader );
		log.debug( "Loaded WSDL: " + ( definition != null ? "ok" : "null" ) );

		if( !loader.isAborted() )
		{
			super.loadSchemaTypes( loader );
		}
		else
			throw new Exception( "Loading of WSDL from [" + loader.getBaseURI() + "] was aborted" );

		return this;
	}

	public String getTargetNamespace()
	{
		return definition.getTargetNamespace();
	}

	public Definition getWsdlDefinition()
	{
		return definition;
	}
}
