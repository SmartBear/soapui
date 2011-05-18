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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityCheck;
import com.eviware.soapui.security.scan.GroovySecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class GroovySecurityCheckFactory extends AbstractSecurityCheckFactory
{

	public GroovySecurityCheckFactory()
	{
		super( GroovySecurityCheck.TYPE, GroovySecurityCheck.NAME,
				"Executes the specified groovy script for security scan", "/groovy_security_check_script.gif" );
	}

	public boolean canCreate( TestStep testStep )
	{
		return true;
	}

	@Override
	public AbstractSecurityCheck buildSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent )
	{
		return new GroovySecurityCheck( testStep, config, parent, null );
	}

	@Override
	public SecurityCheckConfig createNewSecurityCheck( String name )
	{
		SecurityCheckConfig securityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		securityCheckConfig.setType( GroovySecurityCheck.TYPE );
		securityCheckConfig.setName( name );
		GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory.newInstance();
		groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
		// securityCheckConfig.changeType( GroovySecurityCheckConfig.type );
		securityCheckConfig.setConfig( groovyscc );
		return securityCheckConfig;
	}

}
