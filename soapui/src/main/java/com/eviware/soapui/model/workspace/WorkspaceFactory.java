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

package com.eviware.soapui.model.workspace;

import com.eviware.soapui.impl.WorkspaceFactoryImpl;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Factory class for creating Workspaces
 * 
 * @author Ole.Matzura
 */

public abstract class WorkspaceFactory
{
	private static WorkspaceFactory instance;

	public static WorkspaceFactory getInstance()
	{
		if( instance == null )
		{
			instance = new WorkspaceFactoryImpl();
		}

		return instance;
	}

	public abstract Workspace openWorkspace( String workspaceName, StringToStringMap projectOptions )
			throws SoapUIException;
}
