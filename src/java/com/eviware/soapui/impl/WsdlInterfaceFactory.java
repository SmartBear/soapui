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

package com.eviware.soapui.impl;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.config.WsdlInterfaceConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.SoapUIException;

public class WsdlInterfaceFactory implements InterfaceFactory<WsdlInterface>
{
	public final static String WSDL_TYPE = "wsdl";
	private final static Logger log = Logger.getLogger( WsdlInterfaceFactory.class );

	public WsdlInterface build( WsdlProject project, InterfaceConfig config )
	{
		return new WsdlInterface( project, ( WsdlInterfaceConfig )config.changeType( WsdlInterfaceConfig.type ) );
	}

	public WsdlInterface createNew( WsdlProject project, String name )
	{
		WsdlInterface iface = new WsdlInterface( project, ( WsdlInterfaceConfig )project.getConfig().addNewInterface()
				.changeType( WsdlInterfaceConfig.type ) );
		iface.setName( name );

		return iface;
	}

	public static WsdlInterface[] importWsdl( WsdlProject project, String url, boolean createRequests )
			throws SoapUIException
	{
		return importWsdl( project, url, createRequests, null, null );
	}

	public static WsdlInterface[] importWsdl( WsdlProject project, String url, boolean createRequests,
			WsdlLoader wsdlLoader ) throws SoapUIException
	{
		return importWsdl( project, url, createRequests, null, wsdlLoader );
	}

	public static WsdlInterface[] importWsdl( WsdlProject project, String url, boolean createRequests,
			QName bindingName, WsdlLoader wsdlLoader ) throws SoapUIException
	{
		WsdlInterface[] result;

		PropertyExpansionContext context = new DefaultPropertyExpansionContext( project.getModelItem() );
		url = PropertyExpander.expandProperties( context, url );
		try
		{
			result = WsdlImporter.importWsdl( project, url, bindingName, wsdlLoader );
		}
		catch( Exception e )
		{
			log.error( "Error importing wsdl: " + e );
			SoapUI.logError( e );
			throw new SoapUIException( "Error importing wsdl", e );
		}

		try
		{
			if( createRequests && result != null )
			{
				for( WsdlInterface iface : result )
				{
					for( int c = 0; c < iface.getOperationCount(); c++ )
					{
						WsdlOperation operation = iface.getOperationAt( c );
						WsdlRequest request = operation.addNewRequest( "Request 1" );
						try
						{
							String requestContent = operation.createRequest( true );
							request.setRequestContent( requestContent );
						}
						catch( Exception e )
						{
							SoapUI.logError( e );
						}
					}
				}
			}
		}
		catch( Exception e )
		{
			log.error( "Error creating requests: " + e.getMessage() );
			throw new SoapUIException( "Error creating requests", e );
		}

		return result;
	}
}
