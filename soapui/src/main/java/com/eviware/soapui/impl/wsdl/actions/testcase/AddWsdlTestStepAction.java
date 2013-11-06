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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.support.StringUtils;
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

		if( !factory.canAddTestStepToTestCase( testCase ) )
			return;

		String name = UISupport.prompt( "Specify name for new step", "Add Step", factory.getTestStepName() );

		if( name == null )
			return;
		while( testCase.getTestStepByName( name.trim() ) != null )
		{
			name = UISupport.prompt( "Specify unique name of TestStep", "Rename TestStep", name );
			if( StringUtils.isNullOrEmpty( name ) )
				return;
		}
		TestStepConfig newTestStepConfig = factory.createNewTestStep( testCase, name );
		if( newTestStepConfig != null )
		{
			WsdlTestStep testStep = testCase.addTestStep( newTestStepConfig );
			if( testStep != null )
				UISupport.selectAndShow( testStep );
		}
	}
}
