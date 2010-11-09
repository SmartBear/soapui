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

package com.eviware.soapui.security.registry;

import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.security.GroovySecurityCheck;
import com.eviware.soapui.security.SecurityCheck;

/**
 * Factory for creation GroovyScript steps
 * 
 * @author soapUI team
 */

public class GroovySecurityCheckFactory extends SecurityCheckFactory
{
	public static final String GROOVY_SECURITY_CHECK_TYPE = "groovySecurityCheck";

	public GroovySecurityCheckFactory()
	{
		super( GROOVY_SECURITY_CHECK_TYPE, "Groovy Security Check", "Executes the specified groovy script for security check", "/groovy_security_check_script.gif" );
	}

	public boolean canCreate()
	{
		return true;
	}

	@Override
	public SecurityCheck buildSecurityCheck(  SecurityCheckConfig config )
	{
		return new GroovySecurityCheck(  config );
	}

	// @Override
	// public SecurityCheckConfig createNewSecurityCheck( WsdlTestCase testCase,
	// String name )
	// {
	// SecurityCheckConfig securityCheckConfig =
	// SecurityCheckConfig.Factory.newInstance();
	// securityCheckConfig.setType( GROOVY_SECURITY_CHECK_TYPE );
	// securityCheckConfig.setName( name );
	// return securityCheckConfig;
	// }
}
