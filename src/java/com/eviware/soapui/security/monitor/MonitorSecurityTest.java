package com.eviware.soapui.security.monitor;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.security.check.SecurityCheck;

public class MonitorSecurityTest
{
	List<SecurityCheck> monitorSecurityChecksList;

	public MonitorSecurityTest()
	{
		monitorSecurityChecksList = new ArrayList<SecurityCheck>();
	}

	public SecurityCheck addSecurityTest( String name )
	{
		// TODO implement adding names test
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
