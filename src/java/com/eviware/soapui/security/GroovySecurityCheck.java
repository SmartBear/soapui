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
package com.eviware.soapui.security;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityCheck extends AbstractSecurityCheck
{

	public static final String SCRIPT_PROPERTY = GroovySecurityCheck.class.getName() + "@script";
	private String script;

	public GroovySecurityCheck( SecurityCheckConfig config )
	{
		super( config );
		this.script = config.getScript().getStringValue();
	}

	@Override
	protected void execute( TestStep testStep )
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
		getSettings().setString( SCRIPT_PROPERTY, script );
		notifyPropertyChanged( SCRIPT_PROPERTY, old, script );
	}

	private String getScript()
	{
		return getSettings().getString( SCRIPT_PROPERTY, "" );
	}
	

}
