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

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class RunTestCaseRemoveResolver implements Resolver
{
	private WsdlTestStep testStep;
	private boolean resolved;

	public RunTestCaseRemoveResolver( WsdlTestStep testStep )
	{
		this.testStep = testStep;
	}

	public void perform( WsdlTestStep target, Object param )
	{
		target.setDisabled( true );
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	public String getDescription()
	{
		return "Disable Run Test step";
	}

	public String getResolvedPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{

		if( UISupport.confirm( "Are you sure to disable test step?", "Disable Test Step" ) && testStep != null )
		{
			testStep.setDisabled( true );
			resolved = true;
		}
		return resolved;
	}
}
