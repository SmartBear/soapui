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

package com.eviware.soapui.impl.wsdl.support.soap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.soap12.SOAP12Binding;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.settings.WsdlSettings;

/**
 * BindingImporter that can import a WsdlInterface from an SOAP 1.2/HTTP binding
 * 
 * @author Ole.Matzura
 */

public class Soap12HttpBindingImporter extends AbstractSoapBindingImporter
{
	private final static Logger log = Logger.getLogger( Soap12HttpBindingImporter.class );

	public boolean canImport( Binding binding )
	{
		List<?> list = binding.getExtensibilityElements();
		SOAP12Binding soapBinding = WsdlUtils.getExtensiblityElement( list, SOAP12Binding.class );
		return soapBinding == null ? false : soapBinding.getTransportURI().startsWith( Constants.SOAP_HTTP_TRANSPORT )
				|| soapBinding.getTransportURI().startsWith( Constants.SOAP12_HTTP_BINDING_NS );
	}

	@SuppressWarnings( "unchecked" )
	public WsdlInterface importBinding( WsdlProject project, WsdlContext wsdlContext, Binding binding ) throws Exception
	{
		String name = project.getSettings().getBoolean( WsdlSettings.NAME_WITH_BINDING ) ? binding.getQName()
				.getLocalPart() : binding.getPortType().getQName().getLocalPart();

		WsdlInterface iface = ( WsdlInterface )project.addNewInterface( name, WsdlInterfaceFactory.WSDL_TYPE );
		iface.setBindingName( binding.getQName() );
		iface.setSoapVersion( SoapVersion.Soap12 );

		String[] endpoints = WsdlUtils.getEndpointsForBinding( wsdlContext.getDefinition(), binding );
		for( int i = 0; i < endpoints.length; i++ )
		{
			log.info( "importing endpoint " + endpoints[i] );
			iface.addEndpoint( endpoints[i] );
		}

		List<BindingOperation> list = binding.getBindingOperations();
		Collections.sort( list, new BindingOperationComparator() );

		for( Iterator<BindingOperation> iter = list.iterator(); iter.hasNext(); )
		{
			BindingOperation operation = ( BindingOperation )iter.next();

			// sanity check
			if( operation.getOperation() == null || operation.getOperation().isUndefined() )
			{
				log
						.error( "BindingOperation [" + operation.getName()
								+ "] is missing or referring to an invalid operation" );
			}
			else
			{
				log.info( "importing operation " + operation.getName() );
				iface.addNewOperation( operation );
			}
		}

		initWsAddressing( binding, iface, wsdlContext.getDefinition() );

		return iface;
	}

}
