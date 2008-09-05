package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class TestRequestDefaultResolveAction extends AbstractSoapUIAction<AbstractWsdlModelItem<?>>
{

	public TestRequestDefaultResolveAction()
	{
		super("Default resolver", "Remove unresolved request");
	}

	public void perform(AbstractWsdlModelItem<?> target, Object param)
	{
		WsdlTestCase tc = ((WsdlTestRequestStep)target).getTestCase();
		tc.removeTestStep((WsdlTestRequestStep)target);
	}
	
	@Override
	public String toString()
	{
		return getDescription();
	}

}
