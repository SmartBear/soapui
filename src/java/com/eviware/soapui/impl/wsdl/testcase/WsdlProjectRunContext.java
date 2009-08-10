/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

public class WsdlProjectRunContext extends AbstractSubmitContext<WsdlProject> implements
		ProjectRunContext
{
	private final WsdlProjectRunner testScenarioRunner;

	public WsdlProjectRunContext( WsdlProjectRunner testScenarioRunner, StringToObjectMap properties )
	{
		super( testScenarioRunner.getTestRunnable(), properties );
		this.testScenarioRunner = testScenarioRunner;
	}

	public WsdlProject getProject()
	{
		return getModelItem();
	}

	public ProjectRunner getProjectRunner()
	{
		return testScenarioRunner;
	}

	public TestRunner getTestRunner()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object getProperty( String name )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
