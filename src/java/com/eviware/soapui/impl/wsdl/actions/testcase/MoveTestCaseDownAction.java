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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Moves the specified WsdlTestCase down one step in the WsdlTestSuites list of
 * WsdlTestCases
 * 
 * @author ole.matzura
 */

public class MoveTestCaseDownAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public MoveTestCaseDownAction()
	{
		super( "Move TestCase Down", "Moves this TestCase down" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		WsdlTestSuite testSuite = testCase.getTestSuite();
		int ix = testSuite.getIndexOfTestCase( testCase );
		if( ix == -1 || ix >= testSuite.getTestCaseCount() - 1 )
			return;

		testSuite.moveTestCase( ix, 1 );
		UISupport.select( testCase );
	}
}
