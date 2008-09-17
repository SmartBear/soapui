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

	public String getDescription()
	{
		return "Add new target property";
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

	@Override
	public boolean resolve()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
