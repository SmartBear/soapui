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

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.support.AbstractInterface;

public class InterfaceConfigDefinitionCache<T extends AbstractInterface<?>> extends AbstractDefinitionCache<T>
{
	public InterfaceConfigDefinitionCache( T iface )
	{
		super( iface.getConfig().getDefinitionCache(), iface );
	}

	protected DefinitionCacheConfig reinit( T iface )
	{
		InterfaceConfig config = iface.getConfig();
		if( config.isSetDefinitionCache() )
			config.unsetDefinitionCache();

		return config.addNewDefinitionCache();
	}
}
