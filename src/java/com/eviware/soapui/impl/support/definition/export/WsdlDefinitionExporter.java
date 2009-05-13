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

import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.wsdl.WsdlInterface;

public class WsdlDefinitionExporter extends AbstractDefinitionExporter
{
	public WsdlDefinitionExporter( WsdlInterface iface ) throws Exception
	{
		this( iface.getWsdlContext().getInterfaceDefinition() );
	}

	public WsdlDefinitionExporter( InterfaceDefinition<WsdlInterface> definition )
	{
		super( definition );
	}

	protected String[] getLocationXPathsToReplace()
	{
		return new String[] { "declare namespace s='http://schemas.xmlsoap.org/wsdl/' .//s:import/@location",
				"declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation",
				"declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation" };
	}
}
