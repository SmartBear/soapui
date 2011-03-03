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

package com.eviware.soapui.security.check;

import com.eviware.soapui.config.SQLInjectionCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.fuzzer.Fuzzer;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

/**
 * This will test whether a targeted web page is vulnerable to reflected XSS
 * attacks
 * 
 * @author soapui team
 */

public class SQLInjectionCheck extends AbstractSecurityCheck
{

	public static final String TYPE = "SQLInjectionCheck";

	private Fuzzer sqlFuzzer = Fuzzer.getSQLFuzzer();

	public SQLInjectionCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			SQLInjectionCheckConfig pescc = SQLInjectionCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
		if( config.getConfig() == null )
		{
			SQLInjectionCheckConfig pescc = SQLInjectionCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		// sqlFuzzer.getNextFuzzedTestStep( testStep, getParameters() );

		testStep.run( (TestCaseRunner)securityTestRunner, context );
	}

	@Override
	protected boolean hasNext(TestStep testStep,SecurityTestRunContext context)
	{
		return sqlFuzzer.hasNext();
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures SQL injection security check";
	}

	@Override
	public String getConfigName()
	{
		return "SQL Injection Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
