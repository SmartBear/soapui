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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of WsdlTestStep factories
 *
 * @author Ole.Matzura
 */

public class WsdlTestStepRegistry
{
	private static WsdlTestStepRegistry instance;
	private List<WsdlTestStepFactory> factories = new ArrayList<WsdlTestStepFactory>();

	public WsdlTestStepRegistry()
	{
		addFactory( new WsdlTestRequestStepFactory() );
		addFactory( new RestRequestStepFactory() );
		addFactory( new HttpRequestStepFactory() );
		addFactory( new AMFRequestStepFactory() );
		addFactory( new JdbcRequestTestStepFactory() );

		addFactory( new PropertiesStepFactory() );
		addFactory( new PropertyTransfersStepFactory() );

		addFactory( new ProPlaceholderStepFactory( "datasource", "SoapUI Pro DataSource", "/datasource.gif" ) );
		addFactory( new ProPlaceholderStepFactory( "datasink", "SoapUI Pro DataSink", "/datasink.gif" ) );
		addFactory( new ProPlaceholderStepFactory( "datagen", "SoapUI Pro DataGen", "/datagen.gif" ) );
		addFactory( new ProPlaceholderStepFactory( "datasourceloop", "SoapUI Pro DataSourceLoop", "/datasource_loop.gif" ) );

		addFactory( new GotoStepFactory() );
		addFactory( new RunTestCaseStepFactory() );

		addFactory( new GroovyScriptStepFactory() );
		addFactory( new ProPlaceholderStepFactory( "assertionteststep", "SoapUI Pro Assertion TestStep", "/unknown_assertion_step.gif" ) );
		addFactory( new DelayStepFactory() );
		addFactory( new WsdlMockResponseStepFactory() );
		addFactory( new ManualTestStepFactory() );

		for( WsdlTestStepFactory factory : SoapUI.getFactoryRegistry().getFactories( WsdlTestStepFactory.class ) )
		{
			addFactory( factory );
		}
	}

	public WsdlTestStepFactory getFactory( String type )
	{
		for( WsdlTestStepFactory factory : factories )
			if( factory.getType().equals( type ) )
				return factory;

		return null;
	}

	public void addFactory( WsdlTestStepFactory factory )
	{
		int replaceIndex = removeFactory( factory.getType() );
		if( replaceIndex == -1 )
		{
			factories.add( factory );
		}
		else
		{
			factories.add( replaceIndex, factory );
		}
	}

	public int removeFactory( String type )
	{
		int index = 0;
		for( WsdlTestStepFactory factory : factories )
		{
			if( factory.getType().equals( type ) )
			{
				factories.remove( factory );
				return index;
			}
			index++;
		}
		return -1;
	}

	public static synchronized WsdlTestStepRegistry getInstance()
	{
		if( instance == null )
			instance = new WsdlTestStepRegistry();

		return instance;
	}

	public WsdlTestStepFactory[] getFactories()
	{
		return factories.toArray( new WsdlTestStepFactory[factories.size()] );
	}

	public boolean hasFactory( TestStepConfig config )
	{
		return getFactory( config.getType() ) != null;
	}
}
