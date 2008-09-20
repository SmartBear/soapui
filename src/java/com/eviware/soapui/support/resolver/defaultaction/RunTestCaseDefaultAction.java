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
