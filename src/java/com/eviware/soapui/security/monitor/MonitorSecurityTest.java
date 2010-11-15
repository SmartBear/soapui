package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
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

	public SecurityCheck addSecurityCheck( String name, String type )
	{
		SecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( type );
		SecurityCheckConfig scc = factory.createNewSecurityCheck( name );
		monitorSecurityChecksList.add( factory.buildSecurityCheck( scc ) );
		return null;
	}

	public SecurityCheck getSecurityCheckAt( int index )
	{
		// TODO implement
		return null;
	}

	public SecurityCheck removeSecurityCheckAt( int index )
	{
		// TODO implement
		return null;
	}

}
