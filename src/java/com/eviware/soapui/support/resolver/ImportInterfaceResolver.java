package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class ImportInterfaceResolver implements Resolver
{
	private boolean resolved = false;

	//place holder
	public ImportInterfaceResolver(AbstractWsdlModelItem<?> wsdlTestRequestStep)
	{
	}

	public boolean apply()
	{
//		WsdlTestCase wsdlTestCase = wtRequestStep.getTestCase();
//		if (wsdlTestCase != null)
//		{
//			wsdlTestCase.removeTestStep(wtRequestStep);
//			resolved = true;
//		}
//		else
//		{
//			resolved = false;
//		}
//		return resolved;
		return resolve();
	}

	public String getResolvedPath()
	{
		return "";
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{

		UISupport.showInfoMessage("Import inteface");
		return true;
	}

	public String getDescription()
	{
		return "Resolve: Import inteface";
	}

	@Override
	public String toString()
	{
		return getDescription();
	}
}
