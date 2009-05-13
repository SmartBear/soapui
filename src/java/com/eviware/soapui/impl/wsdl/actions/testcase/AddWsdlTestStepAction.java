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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a WsdlTestStep specified by the supplied WsdlTestStepFactory to a
 * WsdlTestCase
 * 
 * @author ole.matzura
 */

public class AddWsdlTestStepAction extends AbstractSoapUIAction<WsdlTestCase>
{
	public final static String SOAPUI_ACTION_ID = "AddWsdlTestStepAction";

	public AddWsdlTestStepAction()
	{
		super( "Add Step", "Adds a TestStep to this TestCase" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		WsdlTestStepFactory factory = ( WsdlTestStepFactory )param;

		String name = UISupport.prompt( "Specify name for new step", "Add Step", factory.getTestStepName() );
		if( name != null )
		{
			TestStepConfig newTestStepConfig = factory.createNewTestStep( testCase, name );
			if( newTestStepConfig != null )
			{
				WsdlTestStep testStep = testCase.addTestStep( newTestStepConfig );
				UISupport.selectAndShow( testStep );
			}
		}
	}
}