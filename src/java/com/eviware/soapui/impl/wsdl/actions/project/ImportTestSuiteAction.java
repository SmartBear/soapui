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
package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ImportTestSuiteAction extends AbstractSoapUIAction<WsdlProject>
{
	public ImportTestSuiteAction()
	{
		super( "Import Test Suite", "Import test suite for this interface" );
	}

	public void perform( WsdlProject project, Object param )
	{
		File file = UISupport.getFileDialogs().openXML( this, "Choose test suite to import" );

		if( file == null )
			return;

		String fileName = file.getAbsolutePath();
		if( fileName == null )
			return;

		project.importTestSuite( file );

	}
}
