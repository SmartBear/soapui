package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class ChooseAnotherPropertyTargetResolver implements Resolver
{

	private boolean resolved;
	
	public ChooseAnotherPropertyTargetResolver(PropertyTransfer propertyTransfer, PropertyTransfersTestStep parent)
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getDescription()
	{
		return "Add new target property";
	}

	@Override
	public String getResolvedPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return resolved;
	}

	@Override
	public boolean resolve()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
