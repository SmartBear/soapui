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
package com.eviware.soapui.model.project;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.SoapUIException;

public interface ProjectFactory<T extends Project>
{
	public T createNew() throws XmlException, IOException, SoapUIException;

	public T createNew( String path ) throws XmlException, IOException, SoapUIException;

	public T createNew( String projectFile, String projectPassword );

	public T createNew( Workspace workspace );

	public T createNew( String path, Workspace workspace );

	public T createNew( String path, Workspace workspace, boolean create );

	public T createNew( String path, Workspace workspace, boolean create, boolean open, String tempName,
			String projectPassword );

}
