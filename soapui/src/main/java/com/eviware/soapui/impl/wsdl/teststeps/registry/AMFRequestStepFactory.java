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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.AMFRequestTestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.monitor.WsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

/**
 * Factory for creation TransferValue steps
 * 
 * @author Ole.Matzura
 */

public class AMFRequestStepFactory extends WsdlTestStepFactory
{
	public static final String AMF_REQUEST_TYPE = "amfrequest";

	public AMFRequestStepFactory()
	{
		super( AMF_REQUEST_TYPE, "AMF Request", "Submits a AMF Request and validates its response", "/amf_request.gif" );
	}

	public WsdlTestStep buildTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		return new AMFRequestTestStep( testCase, config, forLoadTest );
	}

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name )
	{
		TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
		testStepConfig.setType( AMF_REQUEST_TYPE );
		testStepConfig.setName( name );
		return testStepConfig;
	}

	public boolean canCreate()
	{
		return true;
	}

	public TestStepConfig createConfig( WsdlMonitorMessageExchange me, String stepName )
	{
		AMFRequestTestStepConfig testRequestConfig = AMFRequestTestStepConfig.Factory.newInstance();

		testRequestConfig.setEndpoint( me.getEndpoint() );

		TestStepConfig testStep = TestStepConfig.Factory.newInstance();
		testStep.setType( AMF_REQUEST_TYPE );
		testStep.setConfig( testRequestConfig );
		testStep.setName( stepName );
		return testStep;
	}
}
