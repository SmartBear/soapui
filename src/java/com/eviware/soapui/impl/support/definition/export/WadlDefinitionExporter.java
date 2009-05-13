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

package com.eviware.soapui.impl.support.definition.export;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.wsdl.support.Constants;

public class WadlDefinitionExporter extends AbstractDefinitionExporter<RestService>
{
	public WadlDefinitionExporter( InterfaceDefinition<RestService> definition )
	{
		super( definition );
	}

	public WadlDefinitionExporter( RestService restService ) throws Exception
	{
		this( restService.getDefinitionContext().getInterfaceDefinition() );
	}

	public String export( String folderName ) throws Exception
	{
		if( getDefinition().getInterface().isGenerated() )
			setDefinition( getDefinition().getInterface().getWadlContext().regenerateWadl() );

		return super.export( folderName );
	}

	protected String[] getLocationXPathsToReplace()
	{
		return new String[] { "declare namespace s='" + Constants.WADL10_NS + "' .//s:grammars/s:include/@href",
				"declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation",
				"declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation" };
	}
}