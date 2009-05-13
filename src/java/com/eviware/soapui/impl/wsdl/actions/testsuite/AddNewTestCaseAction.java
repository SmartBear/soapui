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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new WsdlTestCase to a WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

public class AddNewTestCaseAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public static final String SOAPUI_ACTION_ID = "AddNewTestCaseAction";

	public AddNewTestCaseAction()
	{
		super( "New TestCase", "Creates a new TestCase in this test suite" );
		// putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu N" ));
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{
		String name = UISupport.prompt( "Specify name of TestCase", "New TestCase", "TestCase "
				+ ( testSuite.getTestCaseCount() + 1 ) );
		if( name == null )
			return;

		WsdlTestCase testCase = testSuite.addNewTestCase( name );
		UISupport.showDesktopPanel( testCase );
	}
}
