/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver.defaultaction;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class RunTestCaseDefaultAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public RunTestCaseDefaultAction()
	{
		super("Default RunTestStep Action", "Remove test case");
	}

	public void perform(WsdlTestStep target, Object param)
	{
		target.setDisabled(true);
	}

	@Override
	public String toString()
	{
		return getDescription();
	}
}
