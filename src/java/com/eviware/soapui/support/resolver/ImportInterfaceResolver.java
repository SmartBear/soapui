package com.eviware.soapui.support.resolver;

import java.io.File;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class ImportInterfaceResolver implements Resolver
{
	private boolean resolved = false;
	private WsdlTestRequestStep item;
	
	public ImportInterfaceResolver(WsdlTestRequestStep item)
	{
		this.item = item;
	}

	public boolean apply()
	{
//		File iFile = UISupport.getFileDialogs().openXML(this, "Choose interface to import");
//		WsdlTestCase tCase = item.getTestCase();
//		item.getO
//		WsdlProject project = tCase.getTestSuite().getProject();
//		if (wsdlTestCase != null)
//		{
//			wsdlTestCase.removeTestStep(wtRequestStep);
//			resolved = true;
//		}
//		else
//		{
//			resolved = false;
//		}
		return resolved;
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
