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

package com.eviware.soapui.impl.support.definition.support;

import com.eviware.soapui.config.DefinitionCacheConfig;
import com.eviware.soapui.impl.support.AbstractInterface;

public class StandaloneDefinitionCache<T extends AbstractInterface<?>> extends AbstractDefinitionCache<T>
{
	public StandaloneDefinitionCache()
	{
		super( DefinitionCacheConfig.Factory.newInstance(), null );
	}

	protected DefinitionCacheConfig reinit( T owner )
	{
		return DefinitionCacheConfig.Factory.newInstance();
	}
}
