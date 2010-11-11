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
package com.eviware.soapui.security.check;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestContext;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityCheck extends AbstractSecurityCheck
{

	public static final String SCRIPT_PROPERTY = GroovySecurityCheck.class.getName() + "@script";
	private String script;
	private GroovySecurityCheckConfig groovySecurityCheckConfig;

	public GroovySecurityCheck( SecurityCheckConfig config )
	{
		super( config );
		if( config.getConfig() == null )
		{
			groovySecurityCheckConfig = ( GroovySecurityCheckConfig )config.addNewConfig().changeType(
					GroovySecurityCheckConfig.type );
			groovySecurityCheckConfig.addNewScript();
		}
		else
		{
			groovySecurityCheckConfig = ( GroovySecurityCheckConfig )config.getConfig().changeType(
					GroovySecurityCheckConfig.type );
		
		}
	
		this.script = groovySecurityCheckConfig.getScript().getStringValue();
	}

	@Override
	protected void execute( TestStep testStep, SecurityTestContext context )
	{
		scriptEngine.setScript( script );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	public void setScript( String script )
	{
		String old = getScript();
		if( groovySecurityCheckConfig.getScript() == null )
		{
			groovySecurityCheckConfig.addNewScript();
		}
		
		groovySecurityCheckConfig.getScript().setStringValue( script );
		notifyPropertyChanged( SCRIPT_PROPERTY, old, script );
	}

	private String getScript()
	{
		return groovySecurityCheckConfig.getScript() != null ? groovySecurityCheckConfig.getScript().getStringValue()
				: "";
	}

	@Override
	public void analyze( TestStep testStep, SecurityTestContext context )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return true;
	}
}
