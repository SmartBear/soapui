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

package com.eviware.soapui.impl.wadl;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.DefinitionLoader;
import com.eviware.soapui.impl.support.definition.export.WadlDefinitionExporter;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.support.definition.support.InterfaceCacheDefinitionLoader;
import com.eviware.soapui.impl.wadl.support.GeneratedWadlDefinitionLoader;
import com.eviware.soapui.impl.wadl.support.WadlInterfaceDefinition;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.support.StringUtils;

public class WadlDefinitionContext extends
		AbstractDefinitionContext<RestService, DefinitionLoader, WadlInterfaceDefinition>
{
	public WadlDefinitionContext( String url, RestService iface )
	{
		super( url, iface );
	}

	public WadlDefinitionContext( String wadlUrl )
	{
		super( wadlUrl );
	}

	protected DefinitionLoader createDefinitionLoader( DefinitionCache restServiceDefinitionCache )
	{
		if( getInterface().isGenerated() || StringUtils.isNullOrEmpty( getInterface().getWadlUrl() ) )
			return new GeneratedWadlDefinitionLoader( getInterface() );
		else
			return new InterfaceCacheDefinitionLoader( restServiceDefinitionCache );
	}

	protected DefinitionLoader createDefinitionLoader( String url )
	{
		if( getInterface().isGenerated() || StringUtils.isNullOrEmpty( url ) )
			return new GeneratedWadlDefinitionLoader( getInterface() );
		else
			return new UrlWsdlLoader( url, getInterface() );
	}

	protected WadlInterfaceDefinition loadDefinition( DefinitionLoader loader ) throws Exception
	{
		return new WadlInterfaceDefinition( getInterface() ).load( loader );
	}

	public String export( String path ) throws Exception
	{
		return new WadlDefinitionExporter( getInterface() ).export( path );
	}

	public WadlInterfaceDefinition regenerateWadl()
	{
		try
		{
			if( getInterface().isGenerated() )
				reload();

			return getInterfaceDefinition();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return null;
	}
}
