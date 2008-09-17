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
