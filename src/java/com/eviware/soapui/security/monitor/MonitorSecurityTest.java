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
package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.registry.AbstractSecurityCheckFactory;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

/**
 * MonitorSecurityTest Contains SecurityChecks to be applied to all underlying
 * traffic in SoapUI Monitor For initial version this is not saved to
 * configuration file
 * 
 * @author dragica.soldo
 * 
 */
public class MonitorSecurityTest
{

	private List<SecurityCheck> monitorSecurityChecksList;
	private JSecurityTestRunLog securityTestLog;

	public MonitorSecurityTest( JSecurityTestRunLog securityTestRunLog )
	{
		monitorSecurityChecksList = new ArrayList<SecurityCheck>();
		this.securityTestLog = securityTestRunLog;
	}

	/**
	 * The list of checks attached to the monitor
	 * 
	 * @return List of checks
	 */
	public List<SecurityCheck> getMonitorSecurityChecksList()
	{
		return monitorSecurityChecksList;
	}

	/**
	 * Adds new SecurityCheck of specified type
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public SecurityCheck addSecurityCheck( String name, String type )
	{
		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( type );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		SecurityCheck newSc = factory.buildSecurityCheck( scc );
		monitorSecurityChecksList.add( newSc );
		return newSc;
	}

	/**
	 * Adds new SecurityCheck with configuration copied from existing one
	 * 
	 * @param name
	 * @param sc
	 * @return
	 */
	public SecurityCheck addSecurityCheck( String name, SecurityCheck sc )
	{
		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( sc.getType() );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		SecurityCheck newSc = factory.buildSecurityCheck( scc );
		newSc.getConfig().setConfig( sc.getConfig().getConfig() );
		newSc.getConfig().setSetupScript( sc.getConfig().getSetupScript() );
		newSc.getConfig().setTearDownScript( sc.getConfig().getTearDownScript() );
		// set assertions
		// newSc.getConfig().setAssertionArray( sc.getConfig().getAssertionList()
		// );
		monitorSecurityChecksList.add( newSc );
		return newSc;
	}

	public SecurityCheck getSecurityCheckAt( int index )
	{
		return monitorSecurityChecksList.get( index );
	}

	public SecurityCheck removeSecurityCheckAt( int index )
	{
		return monitorSecurityChecksList.remove( index );
	}

	public SecurityCheck getSecurityCheckByName( String name )
	{
		for( int c = 0; c < monitorSecurityChecksList.size(); c++ )
		{
			SecurityCheck securityCheck = getSecurityCheckAt( c );
			if( securityCheck.getName().equals( name ) )
				return securityCheck;
		}

		return null;
	}

	public SecurityCheck renameSecurityCheckAt( int index, String newName )
	{
		SecurityCheck sc = removeSecurityCheckAt( index );
		sc.setName( newName );
		monitorSecurityChecksList.add( sc );
		return sc;
	}

	public List<String> getExistingChecksNames()
	{
		List<String> names = new ArrayList<String>();
		for( SecurityCheck scc : monitorSecurityChecksList )
		{
			names.add( scc.getName() );
		}
		return names;
	}

	public JSecurityTestRunLog getSecurityTestLog()
	{
		return securityTestLog;
	}

	public void logSecurityMessage( MessageExchange messageExchange )
	{
		for( SecurityCheck check : getMonitorSecurityChecksList() )
		{
			if( ( ( HttpSecurityAnalyser )check ).canRun() && !check.isDisabled() )
				( ( HttpSecurityAnalyser )check ).analyzeHttpConnection( messageExchange, getSecurityTestLog() );
		}

	}

}
