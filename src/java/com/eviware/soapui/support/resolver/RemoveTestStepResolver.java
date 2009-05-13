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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class RemoveTestStepResolver implements Resolver
{
	private WsdlTestStep testStep;
	private boolean resolve;

	public RemoveTestStepResolver( WsdlTestStep testStep )
	{
		this.testStep = testStep;
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	public String getDescription()
	{
		return "Remove Test Step";
	}

	public String getResolvedPath()
	{
		return null;
	}

	public boolean isResolved()
	{
		return resolve;
	}

	public boolean resolve()
	{
		if( UISupport.confirm( "Are you sure to remove this test step?", "Remove Test Step" ) )
		{
			if( testStep != null )
			{
				testStep.getTestCase().removeTestStep( testStep );
				resolve = true;
			}
		}
		return resolve;
	}

}
