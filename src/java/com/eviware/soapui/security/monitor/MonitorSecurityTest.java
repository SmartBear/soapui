package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLog;
import com.eviware.soapui.security.registry.SecurityCheckFactory;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

public class MonitorSecurityTest
{
	private List<SecurityCheck> monitorSecurityChecksList;
	private SecurityTestLog securityTestLog;

	public SecurityTestLog getSecurityTestLog() {
		return securityTestLog;
	}

	public void setSecurityTestLog(SecurityTestLog securityTestLog) {
		this.securityTestLog = securityTestLog;
	}

	public MonitorSecurityTest()
	{
		monitorSecurityChecksList = new ArrayList<SecurityCheck>();
	}

	public List<SecurityCheck> getMonitorSecurityChecksList()
	{
		return monitorSecurityChecksList;
	}

	public SecurityCheck addSecurityCheck( String name, String type )
	{
		SecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( type );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		SecurityCheck newSc = factory.buildSecurityCheck( scc );
		monitorSecurityChecksList.add( newSc );
		return newSc;
	}

	public SecurityCheck addSecurityCheck( SecurityCheck sc )
	{
		monitorSecurityChecksList.add( sc );
		return sc;
	}

	public SecurityCheck addSecurityCheck( String name, SecurityCheck sc )
	{
		SecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( sc.getType() );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		SecurityCheck newSc = factory.buildSecurityCheck( scc );
		newSc.getConfig().setConfig( sc.getConfig().getConfig() );
		newSc.getConfig().setStartupScript( sc.getConfig().getStartupScript() );
		newSc.getConfig().setTearDownScript( sc.getConfig().getTearDownScript());
		//set assertions
//		newSc.getConfig().setAssertionArray( sc.getConfig().getAssertionList() );
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
		addSecurityCheck( sc );
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
}
