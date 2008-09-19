/*
 *  soapUI, copyright (C) 2004-2008 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver.defaultaction;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class RemoveTestStepDefaultResolveAction extends AbstractSoapUIAction<WsdlTestStep>
{

	public RemoveTestStepDefaultResolveAction()
	{
		super("Default resolver", "Remove unresolved request");
	}

	public void perform(WsdlTestStep target, Object param)
	{
		target.getTestCase().removeTestStep(target);
	}
	
	@Override
	public String toString()
	{
		return getDescription();
	}

}
