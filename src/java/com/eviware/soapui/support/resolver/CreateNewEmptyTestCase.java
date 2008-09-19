package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class CreateNewEmptyTestCase implements Resolver
{

	private boolean resolved;
	private WsdlRunTestCaseTestStep testStep;

	public CreateNewEmptyTestCase(WsdlRunTestCaseTestStep wsdlRunTestCaseTestStep)
	{
		testStep = wsdlRunTestCaseTestStep;
	}

	@Override
	public String getDescription()
	{
		return "Create new empty test case";
	}
	
	@Override
	public String toString()
	{
		return getDescription();
	}

	@Override
	public String getResolvedPath()
	{
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return resolved;
	}

	@Override
	public boolean resolve()
	{
		WsdlTestCase tCase = testStep.getTestCase().getTestSuite().addNewTestCase("New Test Case");
		testStep.setTargetTestCase(tCase);
		resolved = true;
		return resolved;
	}

}
