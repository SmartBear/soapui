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

import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.support.definition.support.InterfaceCacheDefinitionLoader;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

/**
 * Holder for WSDL4J Definitions and related SchemaTypeLoader types
 * 
 * @author Ole.Matzura
 */

public class WsdlContext extends
		AbstractDefinitionContext<WsdlInterface, WsdlDefinitionLoader, WsdlInterfaceDefinition>
{
	private SoapVersion soapVersion = SoapVersion.Soap11;

	public WsdlContext( String url, WsdlInterface iface )
	{
		super( url, iface );
	}

	public WsdlContext( String wsdlUrl )
	{
		this( wsdlUrl, ( WsdlInterface )null );
	}

	public WsdlContext( String wsdlUrl, SoapVersion soapVersion )
	{
		this( wsdlUrl );
		if( soapVersion != null )
		{
			this.soapVersion = soapVersion;
		}
	}

	protected WsdlDefinitionLoader createDefinitionLoader( DefinitionCache wsdlInterfaceDefinitionCache )
	{
		return new InterfaceCacheDefinitionLoader( wsdlInterfaceDefinitionCache );
	}

	protected WsdlDefinitionLoader createDefinitionLoader( String url )
	{
		return new UrlWsdlLoader( url, getInterface() );
	}

	protected WsdlInterfaceDefinition loadDefinition( WsdlDefinitionLoader loader ) throws Exception
	{
		return new WsdlInterfaceDefinition( getInterface() ).load( loader );
	}

	public Definition getDefinition() throws Exception
	{
		return getInterfaceDefinition().getWsdlDefinition();
	}

	public SoapVersion getSoapVersion()
	{
		return getInterface() == null ? soapVersion : getInterface().getSoapVersion();
	}

	public String export( String path ) throws Exception
	{
		return new WsdlDefinitionExporter( getInterface() ).export( path );
	}
}
