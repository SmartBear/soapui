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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.StringUtils;

import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;

public class AMFTestRunListener implements TestRunListener
{
	private AMFCredentials amfCredentials;

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( amfCredentials != null && runContext.getProperty( AMFSubmit.AMF_CONNECTION ) != null
				&& runContext.getProperty( AMFSubmit.AMF_CONNECTION ) instanceof SoapUIAMFConnection )
		{
			if( amfCredentials.isLoggedIn() )
				amfCredentials.logout();
		}
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( testRunner.getTestCase() instanceof WsdlTestCase )
		{
			try
			{
				WsdlTestCase wsdlTestCase = ( WsdlTestCase )testRunner.getTestCase();

				if( wsdlTestCase.getConfig().getAmfAuthorisation() )
				{
					if( noAMFTestSteps( wsdlTestCase ) )
						return;

					String endpoint = runContext.expand( wsdlTestCase.getConfig().getAmfEndpoint() );
					String username = runContext.expand( wsdlTestCase.getConfig().getAmfLogin() );
					String password = runContext.expand( wsdlTestCase.getConfig().getAmfPassword() );

					SoapUIAMFConnection amfConnection = null;

					if( StringUtils.hasContent( endpoint ) )
					{
						if( StringUtils.hasContent( username ) )
						{
							amfCredentials = new AMFCredentials( endpoint, username, password, runContext );
							amfConnection = amfCredentials.login();
						}
						else
						{
							amfConnection = new SoapUIAMFConnection();
							amfConnection.connect( runContext.expand( endpoint ) );
						}

						runContext.setProperty( AMFSubmit.AMF_CONNECTION, amfConnection );
					}
				}
			}
			catch( ClientStatusException e )
			{
				SoapUI.logError( e );
			}
			catch( ServerStatusException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	/**
	 * check if there is no amf test steps in test case then disable amf
	 * authorisation and return true otherwise return false
	 * 
	 * @param wsdlTestCase
	 * @return boolean
	 */
	private static boolean noAMFTestSteps( WsdlTestCase wsdlTestCase )
	{
		if( wsdlTestCase.getTestStepsOfType( AMFRequestTestStep.class ).isEmpty() )
		{
			// wsdlTestCase.getConfig().setAmfAuthorisation( false );
			// SoapUI.log( wsdlTestCase.getName()
			// +
			// " does not contain any AMF Test Step therefore AMF Authorisation is disabled!"
			// );
			return true;
		}
		return false;
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
	{
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
	}
}
