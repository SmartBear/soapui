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

import javax.swing.JTextField;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.check.BoundarySecurityCheck.RestrictionLabel;
import com.eviware.soapui.security.ui.ParameterExposureCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 * 
 * @author nebojsa.tasic
 */

public class ParameterExposureCheck extends AbstractSecurityCheckWithProperties
{
	public static final String TYPE = "ParameterExposureCheck";
	public static final String NAME = "Parameter Exposure";
	private static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";

	StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

	public ParameterExposureCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		testStep.run( ( TestCaseRunner )securityTestRunner, ( TestCaseRunContext )securityTestRunner.getRunContext() );
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
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		return false;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures parameter exposure security check";
	}

	@Override
	public String getConfigName()
	{
		return "Parameter Exposure Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
