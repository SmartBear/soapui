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

package com.eviware.soapui.impl.wsdl.panels.support;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class MockProjectRunner extends AbstractMockTestRunner<WsdlProject> implements ProjectRunner
{
	public MockProjectRunner( WsdlProject project )
	{
		super( project, null );
		setRunContext( new MockProjectRunContext( this ));
	}

	public WsdlProject getProject()
	{
		return getTestRunnable();
	}

	public List<TestSuiteRunner> getResults()
	{
		return new ArrayList<TestSuiteRunner>();
	}
}
