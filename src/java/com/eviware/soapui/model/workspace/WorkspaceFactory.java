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

package com.eviware.soapui.model.workspace;

import java.util.TreeMap;

import com.eviware.soapui.impl.WorkspaceFactoryImpl;
import com.eviware.soapui.support.SoapUIException;

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
   
   public abstract Workspace openWorkspace( String workspaceName, TreeMap<String,String> projectOptions ) throws SoapUIException;
}
