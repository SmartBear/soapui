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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import java.io.File;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class ImportTestCaseAction extends AbstractSoapUIAction<WsdlTestSuite>
{

	public ImportTestCaseAction()
	{
		super( "Import Test Case", "Import test case in this test suite" );
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{

		File file = UISupport.getFileDialogs().openXML( this, "Choose test case to import" );

		if( file == null )
			return;

		String fileName = file.getAbsolutePath();
		if( fileName == null )
			return;

		testSuite.importTestCase( file );

	}

}
