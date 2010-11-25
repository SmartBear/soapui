/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.SecurityCheckConfig;

/**
 * Registry of SecurityCheck factories
 * 
 * @author soapUI team
 */

public class SecurityCheckRegistry
{
	private static SecurityCheckRegistry instance;
	private Map<String, SecurityCheckFactory> availableSecurityChecks = new HashMap<String, SecurityCheckFactory>();

	public SecurityCheckRegistry()
	{
		addFactory( new GroovySecurityCheckFactory() );
		addFactory( new ParameterExposureCheckFactory() );
		addFactory( new SQLInjectionCheckFactory() );
	}

	/**
	 * Gets the right SecurityCheck Factory, depending on the type
	 * @param type The securityCheck to get the factory for
	 * @return
	 */
	public SecurityCheckFactory getFactory( String type )
	{
		for( String cc : availableSecurityChecks.keySet() )
		{
			SecurityCheckFactory scf = availableSecurityChecks.get( cc );
			if( scf.getType().equals( type ) )
				return scf;

		}
		return null;
	}

	/**
	 * Adding a new factory to the registry
	 * @param factory
	 */
	public void addFactory( SecurityCheckFactory factory )
	{
		removeFactory( factory.getType() );
		availableSecurityChecks.put( factory.getSecurityCheckName(), factory );
	}

	/**
	 * Removing a factory from the registry
	 * @param type
	 */
	public void removeFactory( String type )
	{
		for( String scfName : availableSecurityChecks.keySet() )
		{
			SecurityCheckFactory csf = availableSecurityChecks.get( scfName );
			if( csf.getType().equals( type ) )
			{
				availableSecurityChecks.remove( scfName );
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
	 * @param config A configuraiton to check the factory for
	 * @return
	 */
	public boolean hasFactory( SecurityCheckConfig config )
	{
		return getFactory( config.getType() ) != null;
	}

	
	/**
	 * Returns the list of available checks
	 * 
	 * @param monitorOnly Set this to true to get only the list of checks which can be used in the http monitor
	 * @return A String Array containing the names of all the checks
	 */
	public String[] getAvailableSecurityChecksNames(boolean monitorOnly)
	{
		List<String> result = new ArrayList<String>();

		for( SecurityCheckFactory securityCheck : availableSecurityChecks.values() )
		{
			if (monitorOnly && securityCheck.isHttpMonitor())
				result.add( securityCheck.getSecurityCheckName() );
		}

		return result.toArray( new String[result.size()] );
	}

}
