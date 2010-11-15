package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.registry.SecurityCheckFactory;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

public class MonitorSecurityTest
{
	List<SecurityCheck> monitorSecurityChecksList;

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
		monitorSecurityChecksList.add( factory.buildSecurityCheck( scc ) );
		return null;
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
}
