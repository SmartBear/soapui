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

package com.eviware.soapui.impl.wsdl.loadtest.assertions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;

/**
 * Registry for available LoadTestAssertions
 * 
 * @author Ole.Matzura
 */

public class LoadTestAssertionRegistry
{
	private static LoadTestAssertionRegistry instance;
	private Map<String, Class<? extends AbstractLoadTestAssertion>> availableAssertions = new HashMap<String, Class<? extends AbstractLoadTestAssertion>>();
	@SuppressWarnings( "unused" )
	private final static Logger logger = Logger.getLogger( LoadTestAssertionRegistry.class );

	public LoadTestAssertionRegistry()
	{
		availableAssertions.put( TestStepAverageAssertion.STEP_AVERAGE_TYPE, TestStepAverageAssertion.class );
		availableAssertions.put( TestStepTpsAssertion.STEP_TPS_TYPE, TestStepTpsAssertion.class );
		availableAssertions.put( TestStepMaxAssertion.STEP_MAXIMUM_TYPE, TestStepMaxAssertion.class );
		availableAssertions.put( TestStepStatusAssertion.STEP_STATUS_TYPE, TestStepStatusAssertion.class );
		availableAssertions.put( MaxErrorsAssertion.MAX_ERRORS_TYPE, MaxErrorsAssertion.class );
	}

	public static synchronized LoadTestAssertionRegistry getInstance()
	{
		if( instance == null )
			instance = new LoadTestAssertionRegistry();

		return instance;
	}

	public static AbstractLoadTestAssertion buildAssertion( LoadTestAssertionConfig config, WsdlLoadTest loadTest )
	{
		try
		{
			Class<? extends AbstractLoadTestAssertion> clazz = getInstance().availableAssertions.get( config.getType() );
			Constructor<? extends AbstractLoadTestAssertion> ctor = clazz.getConstructor( new Class[] {
					LoadTestAssertionConfig.class, WsdlLoadTest.class } );

			return ( AbstractLoadTestAssertion )ctor.newInstance( config, loadTest );
		}
		catch( SecurityException e )
		{
			SoapUI.logError( e );
		}
		catch( NoSuchMethodException e )
		{
			SoapUI.logError( e );
		}
		catch( IllegalArgumentException e )
		{
			SoapUI.logError( e );
		}
		catch( InstantiationException e )
		{
			SoapUI.logError( e );
		}
		catch( IllegalAccessException e )
		{
			SoapUI.logError( e );
		}
		catch( InvocationTargetException e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

	public static LoadTestAssertionConfig createAssertionConfig( String type )
	{
		LoadTestAssertionConfig config = LoadTestAssertionConfig.Factory.newInstance();
		config.setType( type );
		return config;
	}

	public static String[] getAvailableAssertions()
	{
		return getInstance().availableAssertions.keySet().toArray( new String[getInstance().availableAssertions.size()] );
	}

	public static LoadTestAssertion createAssertion( String type, WsdlLoadTest loadTest )
	{
		LoadTestAssertionConfig config = createAssertionConfig( type );
		return buildAssertion( config, loadTest );
	}
}
