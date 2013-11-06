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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.support.StringUtils;

/**
 * Registry of LoadFactorys
 * 
 * @author Ole.Matzura
 */

public class LoadStrategyRegistry
{
	private static LoadStrategyRegistry instance;
	private Map<String, LoadStrategyFactory> factories = new HashMap<String, LoadStrategyFactory>();

	public LoadStrategyRegistry()
	{
		addFactory( new SimpleLoadStrategy.Factory() );
		addFactory( new BurstLoadStrategy.Factory() );
		addFactory( new VarianceLoadStrategy.Factory() );
		addFactory( new ThreadCountChangeLoadStrategy.Factory() );
	}

	public void addFactory( LoadStrategyFactory factory )
	{
		factories.put( factory.getType(), factory );
	}

	public String[] getStrategies()
	{
		return StringUtils.sortNames( factories.keySet().toArray( new String[factories.size()] ) );
	}

	public static LoadStrategyRegistry getInstance()
	{
		if( instance == null )
			instance = new LoadStrategyRegistry();

		return instance;
	}

	public LoadStrategyFactory getFactory( String type )
	{
		return factories.get( type );
	}
}
