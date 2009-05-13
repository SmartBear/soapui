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

import java.util.List;

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;

public abstract class AbstractInterfaceDefinition<T extends AbstractInterface<?>> implements InterfaceDefinition<T>
{
	private DefinitionCache definitionCache;
	private T iface;

	protected AbstractInterfaceDefinition( T iface )
	{
		this.iface = iface;
	}

	public DefinitionCache getDefinitionCache()
	{
		return definitionCache;
	}

	public void setDefinitionCache( DefinitionCache definitionCache )
	{
		this.definitionCache = definitionCache;
	}

	public InterfaceDefinitionPart getRootPart()
	{
		return definitionCache == null ? null : definitionCache.getRootPart();
	}

	public List<InterfaceDefinitionPart> getDefinitionParts() throws Exception
	{
		return definitionCache == null ? null : definitionCache.getDefinitionParts();
	}

	public T getInterface()
	{
		return iface;
	}

	public void setIface( T iface )
	{
		this.iface = iface;
	}
}
