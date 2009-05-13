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
 * Moves the specified WsdlTestStep up one step in the WsdlTestCases list of
 * WsdlTestSteps
 * 
 * @author ole.matzura
 */

public class MoveTestStepUpAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public MoveTestStepUpAction()
	{
		super( "Move Step Up", "Moves this TestStep up" );
	}

	public void perform( WsdlTestStep testStep, Object param )
	{
		WsdlTestCase testCase = testStep.getTestCase();
		int ix = testCase.getIndexOfTestStep( testStep );
		if( ix == -1 || ix == 0 )
			return;

		testCase.moveTestStep( ix, -1 );
		UISupport.select( testStep );
	}
}
