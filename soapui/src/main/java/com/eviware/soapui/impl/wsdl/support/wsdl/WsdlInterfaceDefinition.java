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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.support.definition.support.XmlSchemaBasedInterfaceDefinition;
import com.eviware.soapui.impl.wsdl.WsdlInterface;

public class WsdlInterfaceDefinition extends XmlSchemaBasedInterfaceDefinition<WsdlInterface>
{
	private Definition definition;

	private static WSDLFactory factory;
	private static WSDLReader wsdlReader;
	private Logger log = Logger.getLogger( WsdlInterfaceDefinition.class );

	private static final Object loadLock = new Object();

	public WsdlInterfaceDefinition( WsdlInterface iface )
	{
		super( iface );
	}

	public WsdlInterfaceDefinition load( WsdlDefinitionLoader loader ) throws Exception
	{
		synchronized ( loadLock )
		{
			if( factory == null )
			{
				factory = WSDLFactory.newInstance();
				wsdlReader = factory.newWSDLReader();
				wsdlReader.setFeature( "javax.wsdl.verbose", true );
				wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
			}
	
			log.debug( "Loading WSDL: " + loader.getBaseURI() );
			try
			{
					definition = wsdlReader.readWSDL( loader );
			}
			catch( WSDLException e )
			{
				throw new InvalidDefinitionException( e );
			}
	
			if( !loader.isAborted() )
			{
				super.loadSchemaTypes( loader );
			}
			else
				throw new Exception( "Loading of WSDL from [" + loader.getBaseURI() + "] was aborted" );
	
			return this;
		}
	}

	public String getTargetNamespace()
	{
		return WsdlUtils.getTargetNamespace( definition );
	}

	public Definition getWsdlDefinition()
	{
		return definition;
	}
}
