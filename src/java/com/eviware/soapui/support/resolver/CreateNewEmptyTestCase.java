/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class CreateNewEmptyTestCase implements Resolver
{

	private boolean resolved;
	private WsdlRunTestCaseTestStep testStep;

	public CreateNewEmptyTestCase( WsdlRunTestCaseTestStep wsdlRunTestCaseTestStep )
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
		WsdlTestCase tCase = testStep.getTestCase().getTestSuite().addNewTestCase( "New Test Case" );
		testStep.setTargetTestCase( tCase );
		resolved = true;
		return resolved;
	}

}
