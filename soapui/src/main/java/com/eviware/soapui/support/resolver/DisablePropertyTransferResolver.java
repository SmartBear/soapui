/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class DisablePropertyTransferResolver implements Resolver
{
	PropertyTransfer transfer = null;
	private boolean resolved;

	public DisablePropertyTransferResolver( PropertyTransfer transfer )
	{
		this.transfer = transfer;
	}

	public String toString()
	{
		return getDescription();
	}

	public String getDescription()
	{
		return "Disable Property Transfer";
	}

	public String getResolvedPath()
	{
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{
		if( UISupport.confirm( "Are you sure you want to disable property?", "Property Disable" ) && transfer != null )
		{
			transfer.setDisabled( true );
			resolved = true;
		}
		return resolved;
	}

}
