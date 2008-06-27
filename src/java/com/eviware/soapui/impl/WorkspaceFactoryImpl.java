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

package com.eviware.soapui.impl;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;

/**
 * Default WorkspaceFactory implementation
 * 
 * @author Ole.Matzura
 */

public class WorkspaceFactoryImpl extends WorkspaceFactory
{
   public Workspace openWorkspace(String[] args) throws SoapUIException
   {
      try
		{
			return new WorkspaceImpl( args[0] );
		}
		catch (Exception e)
		{
			throw new SoapUIException( e );
		}
   }
}
