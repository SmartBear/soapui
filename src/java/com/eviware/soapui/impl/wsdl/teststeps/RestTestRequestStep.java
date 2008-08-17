package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;

public class RestTestRequestStep extends WsdlTestStepWithProperties
{

	public RestTestRequestStep(WsdlTestCase testCase, TestStepConfig config, boolean hasEditor, boolean forLoadTest)
	{
		super(testCase, config, hasEditor, forLoadTest);
	}

	public TestStepResult run(TestRunner testRunner, TestRunContext testRunContext)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public RestTestRequest getRestRequest()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
