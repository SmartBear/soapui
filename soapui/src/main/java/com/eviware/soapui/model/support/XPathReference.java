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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.Operation;

public interface XPathReference
{
	public String getXPath();

	public void setXPath( String xpath );

	public String getLabel();

	public Operation getOperation();

	public boolean isRequest();

	/**
	 * Gets the property this xpath selects from
	 */

	// public TestProperty getTargetProperty();

	/**
	 * apply updates
	 */

	public void update();
}
