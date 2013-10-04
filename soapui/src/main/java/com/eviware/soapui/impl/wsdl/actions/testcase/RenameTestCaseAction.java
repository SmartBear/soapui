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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlTestCase
 * 
 * @author Ole.Matzura
 */

public class RenameTestCaseAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public RenameTestCaseAction()
	{
		super( "Rename", "Renames this TestCase" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		String name = UISupport.prompt( "Specify name of TestCase", "Rename TestCase", testCase.getName() );
		if( name == null || name.equals( testCase.getName() ) )
			return;
		while( testCase.getTestSuite().getTestCaseByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of TestCase", "Rename TestCase", testCase.getName() );
			if( name == null || name.equals( testCase.getName() ) )
				return;
		}

		testCase.setName( name );
	}
}
