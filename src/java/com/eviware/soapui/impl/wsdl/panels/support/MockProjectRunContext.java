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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestRunner;

public class MockProjectRunContext extends AbstractSubmitContext<WsdlProject> implements ProjectRunContext
{
	private final MockProjectRunner mockProjectRunner;

	public MockProjectRunContext( MockProjectRunner mockProjectRunner )
	{
		super( mockProjectRunner.getProject() );
		this.mockProjectRunner = mockProjectRunner;
	}

	public WsdlProject getProject()
	{
		return getModelItem();
	}

	public ProjectRunner getProjectRunner()
	{
		return mockProjectRunner;
	}

	public TestRunner getTestRunner()
	{
		return mockProjectRunner;
	}

	public Object getProperty( String name )
	{
		return getProperties().get( name );
	}
}
