package com.eviware.soapui.security.check;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

public class InvalidTypesSecurityCheck extends AbstractSecurityCheck
{

	public final static String TYPE = "InvalidTypesSecurityCheck";
	
	private SchemeTypeExtractor extractor;

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		extractor = new SchemeTypeExtractor( testStep );
	}

	/**
	 * 
	 * 
	 */
	//XXX: add rest and http.
	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckRequestResult analyze( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SecurityCheckRequestResult execute( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

}
