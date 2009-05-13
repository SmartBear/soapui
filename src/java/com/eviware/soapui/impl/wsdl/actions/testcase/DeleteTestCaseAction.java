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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a WsdlTestCase from its WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

public class DeleteTestCaseAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public DeleteTestCaseAction()
	{
		super( "Remove", "Removes this TestCase from the TestSuite" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		if( SoapUI.getTestMonitor().hasRunningTest( testCase ) )
		{
			UISupport.showErrorMessage( "Cannot remove RestCase while tests are running" );
			return;
		}

		if( UISupport.confirm( "Remove TestCase [" + testCase.getName() + "] from TestSuite", "Remove TestCase" ) )
		{
			( ( WsdlTestSuite )testCase.getTestSuite() ).removeTestCase( testCase );
		}
	}

}
