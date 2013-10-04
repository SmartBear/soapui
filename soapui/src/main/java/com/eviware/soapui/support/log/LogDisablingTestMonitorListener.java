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

package com.eviware.soapui.support.log;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.monitor.support.TestMonitorListenerAdapter;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.settings.UISettings;

/**
 * Disables httpclient and groovy logs during loadtests and securitytests
 * 
 * @author ole
 */

public final class LogDisablingTestMonitorListener extends TestMonitorListenerAdapter
{
	private Set<LoadTestRunner> loadTestRunners = new HashSet<LoadTestRunner>();
	private Set<SecurityTestRunner> securityTestRunners = new HashSet<SecurityTestRunner>();

	public void loadTestStarted( LoadTestRunner runner )
	{
		if( loadTestRunners.isEmpty() )
		{
			Logger.getLogger( SoapUI.class ).info( "Disabling logs during loadtests" );
			Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.OFF );

			if( !SoapUI.getSettings().getBoolean( UISettings.DONT_DISABLE_GROOVY_LOG ) )
				Logger.getLogger( "groovy.log" ).setLevel( Level.OFF );
		}

		loadTestRunners.add( runner );
	}

	public void loadTestFinished( LoadTestRunner runner )
	{
		loadTestRunners.remove( runner );

		if( loadTestRunners.isEmpty() )
		{
			Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.DEBUG );
			Logger.getLogger( "groovy.log" ).setLevel( Level.DEBUG );
			Logger.getLogger( SoapUI.class ).info( "Enabled logs after loadtests" );
		}
	}

	public void securityTestStarted( SecurityTestRunner runner )
	{
		if( securityTestRunners.isEmpty() )
		{
			// Logger.getLogger( SoapUI.class ).info(
			// "Disabling logs during securitytests" );
			// Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.OFF );

			// if( !SoapUI.getSettings().getBoolean(
			// UISettings.DONT_DISABLE_GROOVY_LOG ) )
			// Logger.getLogger( "groovy.log" ).setLevel( Level.OFF );
		}

		securityTestRunners.add( runner );
	}

	public void securityTestFinished( SecurityTestRunner runner )
	{
		securityTestRunners.remove( runner );

		if( securityTestRunners.isEmpty() )
		{
			// Logger.getLogger( "org.apache.http.wire" ).setLevel( Level.DEBUG );
			// Logger.getLogger( "groovy.log" ).setLevel( Level.DEBUG );
			// Logger.getLogger( SoapUI.class ).info(
			// "Enabled logs after securitytests" );
		}
	}
}
