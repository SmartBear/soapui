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

	public String getDescription()
	{
		return "Create new empty test case";
	}
	
	@Override
	public String toString()
	{
		return getDescription();
	}

	public String getResolvedPath()
	{
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		WsdlTestCase tCase = testStep.getTestCase().getTestSuite().addNewTestCase("New Test Case");
		testStep.setTargetTestCase(tCase);
		resolved = true;
		return resolved;
	}

}
