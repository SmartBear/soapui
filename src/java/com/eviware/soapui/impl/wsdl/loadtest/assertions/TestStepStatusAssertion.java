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

package com.eviware.soapui.impl.wsdl.loadtest.assertions;

import java.util.Arrays;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics;
import com.eviware.soapui.impl.wsdl.loadtest.data.LoadTestStatistics.Statistic;
import com.eviware.soapui.impl.wsdl.support.Configurable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XForm.FieldType;

/**
 * LoadTestAssertion for asserting the status of a teststep
 * 
 * @author Ole.Matzura
 */

public class TestStepStatusAssertion extends AbstractLoadTestAssertion implements Configurable
{
	private static final String NAME_FIELD = "Name";
	private static final String NAME_ELEMENT = "name";
	private static final String MINIMUM_REQUESTS_FIELD = "Minimum Requests";
	private static final String MIN_REQUESTS_ELEMENT = "min-requests";
	private static final String MAX_ERRORS_ELEMENT = "max-errors";
	private static final String MAX_ERRORS_FIELD = "Max Errors";

	private int minRequests;
	private int maxErrors;
	private XFormDialog dialog;
	public static final String STEP_STATUS_TYPE = "Step Status";

	public TestStepStatusAssertion( LoadTestAssertionConfig assertionConfig, WsdlLoadTest loadTest )
	{
		super( assertionConfig, loadTest );

		init( assertionConfig );
		initIcon( "/status_loadtest_assertion.gif" );
	}

	private void init( LoadTestAssertionConfig assertionConfig )
	{
		XmlObject configuration = assertionConfig.getConfiguration();
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( configuration );

		setName( reader.readString( TestStepStatusAssertion.NAME_ELEMENT, "Step Status" ) );
		minRequests = reader.readInt( TestStepStatusAssertion.MIN_REQUESTS_ELEMENT, 0 );
		setTargetStep( reader.readString( TestStepStatusAssertion.TEST_STEP_ELEMENT, ANY_TEST_STEP ) );
		maxErrors = reader.readInt( MAX_ERRORS_ELEMENT, -1 );
	}

	public String getDescription()
	{
		return "testStep: " + getTargetStep() + ", minRequests: " + minRequests + ", maxErrors: " + maxErrors;
	}

	public String assertResult( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
			TestRunner testRunner, TestRunContext runContext )
	{
		WsdlLoadTest loadTest = ( WsdlLoadTest )loadTestRunner.getLoadTest();
		LoadTestStatistics statisticsModel = loadTest.getStatisticsModel();

		TestStep step = result.getTestStep();

		if( targetStepMatches( step ) )
		{
			int index = step.getTestCase().getIndexOfTestStep( step );

			if( statisticsModel.getStatistic( index, Statistic.COUNT ) >= minRequests
					&& result.getStatus() == TestStepStatus.FAILED )
			{
				return returnErrorOrFail( "TestStep [" + step.getName() + "] result status is "
						+ result.getStatus().toString() + "; " + Arrays.toString( result.getMessages() ), maxErrors,
						loadTestRunner, context );
			}
			else
				return null;
		}

		return null;
	}

	public String assertResults( LoadTestRunner loadTestRunner, LoadTestRunContext context, TestRunner testRunner,
			TestRunContext runContext )
	{
		return null;
	}

	public boolean configure()
	{
		if( dialog == null )
		{
			buildDialog();
		}

		StringToStringMap values = new StringToStringMap();

		values.put( NAME_FIELD, getName() );
		values.put( MINIMUM_REQUESTS_FIELD, String.valueOf( minRequests ) );
		values.put( TEST_STEP_FIELD, getTargetStep() );
		values.put( MAX_ERRORS_FIELD, String.valueOf( maxErrors ) );

		dialog.setOptions( TestStepStatusAssertion.TEST_STEP_FIELD, getTargetStepOptions( false ) );
		values = dialog.show( values );

		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			try
			{
				minRequests = Integer.parseInt( values.get( MINIMUM_REQUESTS_FIELD ) );
				maxErrors = Integer.parseInt( values.get( MAX_ERRORS_FIELD ) );
				setName( values.get( NAME_FIELD ) );
				setTargetStep( values.get( TEST_STEP_FIELD ) );
			}
			catch( Exception e )
			{
				UISupport.showErrorMessage( e.getMessage() );
			}

			updateConfiguration();
			return true;
		}

		return false;
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "TestStep Status Assertion" );
		XForm form = builder.createForm( "Basic" );

		form.addTextField( NAME_FIELD, "Name of this assertion", FieldType.TEXT );
		form.addTextField( MINIMUM_REQUESTS_FIELD, "Minimum number of runs before asserting", FieldType.TEXT );
		form.addTextField( MAX_ERRORS_FIELD, "Maximum number of errors before failing", FieldType.TEXT );
		form.addComboBox( TEST_STEP_FIELD, new String[0], "TestStep to assert" );

		dialog = builder.buildDialog( builder
				.buildOkCancelHelpActions( HelpUrls.STEP_STATUS_LOAD_TEST_ASSERTION_HELP_URL ),
				"Specify options for this TestStep Status Assertion", UISupport.OPTIONS_ICON );
	}

	protected void updateConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();

		builder.add( NAME_ELEMENT, getName() );
		builder.add( MIN_REQUESTS_ELEMENT, minRequests );
		builder.add( TEST_STEP_ELEMENT, getTargetStep() );
		builder.add( MAX_ERRORS_ELEMENT, maxErrors );

		setConfiguration( builder.finish() );
	}
}
