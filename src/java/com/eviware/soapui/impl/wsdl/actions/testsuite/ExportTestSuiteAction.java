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
package com.eviware.soapui.impl.wsdl.actions.testsuite;

import java.io.File;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ExportTestSuiteAction extends AbstractSoapUIAction<WsdlTestSuite>
{

	public ExportTestSuiteAction()
	{
		super( "Export", "Export this test suite" );
	}

	public void perform( WsdlTestSuite tSuite, Object param )
	{
		tSuite.beforeSave();
		String defaultFileName = System.getProperty( "user.home" ) + File.separator + tSuite.getName() + ".xml";
		File file = UISupport.getFileDialogs().saveAs( this, "Select test case file", "xml", "XML",
				new File( defaultFileName ) );

		if( file == null )
			return;

		String fileName = file.getAbsolutePath();
		if( fileName == null )
			return;

		tSuite.export( file );
	}

}
