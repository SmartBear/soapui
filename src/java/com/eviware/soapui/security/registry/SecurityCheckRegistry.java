/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ui.SecurityConfigurationDialogBuilder;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Registry of SecurityCheck factories
 * 
 * @author soapUI team
 */

public class SecurityCheckRegistry
{
	protected static SecurityCheckRegistry instance;
	private Map<String, AbstractSecurityCheckFactory> availableSecurityChecks = new HashMap<String, AbstractSecurityCheckFactory>();
	private StringToStringMap securityCheckNames = new StringToStringMap();

	public SecurityCheckRegistry()
	{
		addFactory( new GroovySecurityCheckFactory() );
		addFactory( new ParameterExposureCheckFactory() );
		addFactory( new XmlBombSecurityCheckFactory() );
		addFactory( new MaliciousAttachmentSecurityCheckFactory() );
		addFactory( new LargeAttachmentSecurityCheckFactory() );
		// this is actually working
		addFactory( new XPathInjectionSecurityCheckFactory() );
		addFactory( new InvalidTypesSecurityCheckFactory() );
		addFactory( new BoundarySecurityCheckFactory() );
		addFactory( new SQLInjectionCheckFactory() );
	}

	/**
	 * Gets the right SecurityCheck Factory, depending on the type
	 * 
	 * @param type
	 *           The securityCheck to get the factory for
	 * @return
	 */
	public AbstractSecurityCheckFactory getFactory( String type )
	{
		for( String cc : availableSecurityChecks.keySet() )
		{
			AbstractSecurityCheckFactory scf = availableSecurityChecks.get( cc );
			if( scf.getSecurityCheckType().equals( type ) )
				return scf;

		}
		return null;
	}

	/**
	 * Gets the right SecurityCheck Factory using name
	 * 
	 * @param name
	 *           The securityCheck name to get the factory for
	 * @return
	 */
	public AbstractSecurityCheckFactory getFactoryByName( String name )
	{
		String type = getSecurityCheckTypeForName( name );

		if( type != null )
		{
			return getFactory( type );
		}

		return null;
	}

	/**
	 * Adding a new factory to the registry
	 * 
	 * @param factory
	 */
	public void addFactory( AbstractSecurityCheckFactory factory )
	{
		removeFactory( factory.getSecurityCheckType() );
		availableSecurityChecks.put( factory.getSecurityCheckName(), factory );
		securityCheckNames.put( factory.getSecurityCheckName(), factory.getSecurityCheckType() );
	}

	/**
	 * Removing a factory from the registry
	 * 
	 * @param type
	 */
	public void removeFactory( String type )
	{
		for( String scfName : availableSecurityChecks.keySet() )
		{
			AbstractSecurityCheckFactory csf = availableSecurityChecks.get( scfName );
			if( csf.getSecurityCheckType().equals( type ) )
			{
				availableSecurityChecks.remove( scfName );
				securityCheckNames.remove( scfName );
				break;
			}
		}
	}

	/**
	 * 
	 * @return The registry instance
	 */
	public static synchronized SecurityCheckRegistry getInstance()
	{
		if( instance == null )
			instance = new SecurityCheckRegistry();

		return instance;
	}

	/**
	 * Checking if the registry contains a factory.
	 * 
	 * @param config
	 *           A configuration to check the factory for
	 * @return
	 */
	public boolean hasFactory( SecurityCheckConfig config )
	{
		return getFactory( config.getType() ) != null;
	}

	/**
	 * Returns the list of available checks
	 * 
	 * @param monitorOnly
	 *           Set this to true to get only the list of checks which can be
	 *           used in the http monitor
	 * @return A String Array containing the names of all the checks
	 */
	public String[] getAvailableSecurityChecksNames( boolean monitorOnly )
	{
		List<String> result = new ArrayList<String>();

		for( AbstractSecurityCheckFactory securityCheck : availableSecurityChecks.values() )
		{
			if( monitorOnly && securityCheck.isHttpMonitor() )
				result.add( securityCheck.getSecurityCheckName() );
		}

		String[] sortedResult = result.toArray( new String[result.size()] );
		Arrays.sort( sortedResult );

		return sortedResult;
	}

	// TODO drso: test and implement properly
	public String[] getAvailableSecurityChecksNames( TestStep testStep )
	{
		List<String> result = new ArrayList<String>();

		for( AbstractSecurityCheckFactory securityCheck : availableSecurityChecks.values() )
		{
			if( securityCheck.canCreate( testStep ) )
				result.add( securityCheck.getSecurityCheckName() );
		}

		String[] sortedResult = result.toArray( new String[result.size()] );
		Arrays.sort( sortedResult );

		return sortedResult;
	}

	public SecurityConfigurationDialogBuilder getUIBuilder()
	{
		return new SecurityConfigurationDialogBuilder();
	}

	public String getSecurityCheckTypeForName( String name )
	{
		return securityCheckNames.get( name );
	}

}