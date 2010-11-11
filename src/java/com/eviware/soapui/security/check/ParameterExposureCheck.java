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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.HttpResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestContext;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck
{

	public static final String MINIMUM_LENGTH_PROPERTY = ParameterExposureCheck.class.getName() + "@minimumLength";

	public ParameterExposureCheck( SecurityCheckConfig config )
	{
		super( config );
		monitorApplicable = true;
	}

	@Override
	protected void execute( TestStep testStep, SecurityTestContext context )
	{
		if (testStep instanceof HttpTestRequestStep) {
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( (WsdlTestCase)testStep.getTestCase(), new StringToObjectMap() );
			testCaseRunner.runTestStepByName( testStep.getName() );
		}
		analyze(testStep, context);		
	}

	@Override
	public void analyze(TestStep testStep, SecurityTestContext context) {
		if (testStep instanceof HttpTestRequestStep) {
			//This is just to make things a bit easier to read going forward
			HttpTestRequestStep httpTestStep = (HttpTestRequestStep)testStep;
			HttpTestRequest request = httpTestStep.getTestRequest();
			MessageExchange messageExchange = new HttpResponseMessageExchange( request );
			
			Map<String, TestProperty> params = httpTestStep.getProperties();
			
			for (TestProperty param : params.values() ) {
				if (param.getValue().length() >= getMinimumLength()) {
					TestAssertionConfig assertionConfig = TestAssertionConfig.Factory.newInstance();
					assertionConfig.setType(SimpleContainsAssertion.ID);
		
					SimpleContainsAssertion containsAssertion = (SimpleContainsAssertion) TestAssertionRegistry.getInstance().buildAssertion(assertionConfig, httpTestStep);
					containsAssertion.setName(param.getName());
					containsAssertion.setToken(param.getValue());
					
					containsAssertion.assertResponse(messageExchange, context);
				}
			}		
		}
		// TODO Auto-generated method stub
		
	}
	
	public void setMinimumLength( long minimumLength )
	{
		long old = getMinimumLength();
		getSettings().setLong( MINIMUM_LENGTH_PROPERTY, minimumLength );
		notifyPropertyChanged( MINIMUM_LENGTH_PROPERTY, old, minimumLength );
	}

	private long getMinimumLength()
	{
		return getSettings().getLong(MINIMUM_LENGTH_PROPERTY, 5);
	}
	
}
