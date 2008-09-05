package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class ChangeOperationResolver implements Resolver
{

	private boolean resolved = false;

	public ChangeOperationResolver(AbstractWsdlModelItem<?> abstractWsdlModelItem)
	{
	}

	public boolean apply()
	{
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

		UISupport.showInfoMessage("Import operation");
		return true;
	}

	public String getDescription()
	{
		return "Resolve: Import operation";
	}

	 @Override
	public String toString()
	{
		return getDescription();
	}
}
