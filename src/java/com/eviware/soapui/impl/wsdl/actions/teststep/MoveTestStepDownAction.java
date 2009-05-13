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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Moves the specified WsdlTestStep down one step in the WsdlTestCases list of
 * WsdlTestSteps
 * 
 * @author ole.matzura
 */

public class MoveTestStepDownAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public MoveTestStepDownAction()
	{
		super( "Move Step Down", "Moves this TestStep down" );
	}

	public void perform( WsdlTestStep testStep, Object param )
	{
		WsdlTestCase testCase = testStep.getTestCase();
		int ix = testCase.getIndexOfTestStep( testStep );
		if( ix == -1 || ix >= testCase.getTestStepCount() - 1 )
			return;

		testCase.moveTestStep( ix, 1 );
		UISupport.select( testStep );
	}
}
