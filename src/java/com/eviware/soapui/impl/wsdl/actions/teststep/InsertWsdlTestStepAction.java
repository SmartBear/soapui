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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Inserts a WsdlTestStep specified by the supplied WsdlTestStepFactory at the
 * position to the specified WsdlTestStep
 * 
 * @author ole.matzura
 */

public class InsertWsdlTestStepAction extends AbstractSoapUIAction<WsdlTestStep>
{
	public static final String SOAPUI_ACTION_ID = "InsertWsdlTestStepAction";

	public InsertWsdlTestStepAction()
	{
		super( "Insert Step", "Inserts a TestStep at the position of this TestStep" );
	}

	public void perform( WsdlTestStep testStep, Object param )
	{
		WsdlTestStepFactory factory = ( WsdlTestStepFactory )param;
		WsdlTestCase testCase = testStep.getTestCase();

		String name = UISupport.prompt( "Specify name for new step", "Insert Step", factory.getTestStepName() );
		if( name != null )
		{
			TestStepConfig newTestStepConfig = factory.createNewTestStep( testCase, name );
			if( newTestStepConfig != null )
			{
				int ix = testCase.getIndexOfTestStep( testStep );
				testStep = testCase.insertTestStep( newTestStepConfig, ix );
				UISupport.selectAndShow( testStep );
			}
		}
	}
}