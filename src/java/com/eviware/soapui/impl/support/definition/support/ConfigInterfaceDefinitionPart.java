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

package com.eviware.soapui.impl.support.definition.support;

import org.w3c.dom.Node;

import com.eviware.soapui.config.DefinitionCacheTypeConfig;
import com.eviware.soapui.config.DefintionPartConfig;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.support.xml.XmlUtils;

public class ConfigInterfaceDefinitionPart implements InterfaceDefinitionPart
{
	private DefintionPartConfig config;
	private boolean isRoot;
	private DefinitionCacheTypeConfig.Enum type;

	public ConfigInterfaceDefinitionPart( DefintionPartConfig config, boolean isRoot, DefinitionCacheTypeConfig.Enum type )
	{
		this.config = config;
		this.isRoot = isRoot;
		this.type = type;
	}

	public String getUrl()
	{
		return config.getUrl();
	}

	public String getType()
	{
		return config.getType();
	}

	public String getContent()
	{
		if( type == DefinitionCacheTypeConfig.TEXT )
		{
			Node domNode = config.getContent().getDomNode();
			return XmlUtils.getNodeValue( domNode );
		}
		else
		{
			return config.getContent().toString();
		}
	}

	public boolean isRootPart()
	{
		return isRoot;
	}
}
