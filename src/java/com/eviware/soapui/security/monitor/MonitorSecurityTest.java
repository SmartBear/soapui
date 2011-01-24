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

package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
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

	private List<AbstractSecurityCheck> monitorSecurityChecksList;
	private JSecurityTestRunLog securityTestLog;

	public MonitorSecurityTest( JSecurityTestRunLog securityTestRunLog )
	{
		monitorSecurityChecksList = new ArrayList<AbstractSecurityCheck>();
		this.securityTestLog = securityTestRunLog;
	}

	/**
	 * The list of checks attached to the monitor
	 * 
	 * @return List of checks
	 */
	public List<AbstractSecurityCheck> getMonitorSecurityChecksList()
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
	public AbstractSecurityCheck addSecurityCheck( String name, String type )
	{
		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( type );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		AbstractSecurityCheck newSc = factory.buildSecurityCheck( scc, null );
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
	public AbstractSecurityCheck addSecurityCheck( String name, AbstractSecurityCheck sc )
	{
		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( sc.getType() );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		AbstractSecurityCheck newSc = factory.buildSecurityCheck( scc, null );
		newSc.getConfig().setConfig( sc.getConfig().getConfig() );
		newSc.getConfig().setSetupScript( sc.getConfig().getSetupScript() );
		newSc.getConfig().setTearDownScript( sc.getConfig().getTearDownScript() );
		// set assertions
		// newSc.getConfig().setAssertionArray( sc.getConfig().getAssertionList()
		// );
		monitorSecurityChecksList.add( newSc );
		return newSc;
	}

	public AbstractSecurityCheck getSecurityCheckAt( int index )
	{
		return monitorSecurityChecksList.get( index );
	}

	public AbstractSecurityCheck removeSecurityCheckAt( int index )
	{
		return monitorSecurityChecksList.remove( index );
	}

	public AbstractSecurityCheck getSecurityCheckByName( String name )
	{
		for( int c = 0; c < monitorSecurityChecksList.size(); c++ )
		{
			AbstractSecurityCheck securityCheck = getSecurityCheckAt( c );
			if( securityCheck.getName().equals( name ) )
				return securityCheck;
		}

		return null;
	}

	public AbstractSecurityCheck renameSecurityCheckAt( int index, String newName )
	{
		AbstractSecurityCheck sc = removeSecurityCheckAt( index );
		sc.setName( newName );
		monitorSecurityChecksList.add( sc );
		return sc;
	}

	public List<String> getExistingChecksNames()
	{
		List<String> names = new ArrayList<String>();
		for( AbstractSecurityCheck scc : monitorSecurityChecksList )
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
		for( AbstractSecurityCheck check : getMonitorSecurityChecksList() )
		{
			if( ( ( HttpSecurityAnalyser )check ).canRun() && !check.isDisabled() )
				( ( HttpSecurityAnalyser )check ).analyzeHttpConnection( messageExchange, getSecurityTestLog() );
		}

	}

}
