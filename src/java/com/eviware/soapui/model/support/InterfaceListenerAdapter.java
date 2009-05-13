/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;

/**
 * Adapter for InterfaceListener implementations
 * 
 * @author Ole.Matzura
 */

public class InterfaceListenerAdapter implements InterfaceListener
{
	public void operationAdded( Operation operation )
	{
	}

	public void operationRemoved( Operation operation )
	{
	}

	public void requestAdded( Request request )
	{
	}

	public void requestRemoved( Request request )
	{
	}

	public void operationUpdated( Operation operation )
	{
	}
}
