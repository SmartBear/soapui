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

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XForm.FieldType;

/**
 * Options dialog for testcases
 * 
 * @author Ole.Matzura
 */

public class TestCaseOptionsAction extends AbstractSoapUIAction<WsdlTestCase>
{
	private static final String KEEP_SESSION = "Session";
	private static final String FAIL_ON_ERROR = "Abort on Error";
	private static final String FAIL_TESTCASE_ON_ERROR = "Fail TestCase on Error";
	private static final String DISCARD_OK_RESULTS = "Discard OK Results";
	private static final String SOCKET_TIMEOUT = "Socket timeout";
	private static final String SEARCH_PROPERTIES = "Search Properties";
	public static final String SOAPUI_ACTION_ID = "TestCaseOptionsAction";
	private static final String TESTCASE_TIMEOUT = "TestCase timeout";
	private static final String MAXRESULTS = "Max Results";

	private XFormDialog dialog;
	private XForm form;

	public TestCaseOptionsAction()
	{
		super( "Options", "Sets options for this TestCase" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		if( dialog == null )
		{
			XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "TestCase Options" );
			form = builder.createForm( "Basic" );
			form.addCheckBox( SEARCH_PROPERTIES, "Search preceding teststeps for property values" );
			form.addCheckBox( KEEP_SESSION, "Maintain HTTP session" );
			form.addCheckBox( FAIL_ON_ERROR, "Fail on error" ).addFormFieldListener( new XFormFieldListener()
			{

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					form.getFormField( FAIL_TESTCASE_ON_ERROR ).setEnabled( !Boolean.parseBoolean( newValue ) );
				}
			} );

			form.addCheckBox( FAIL_TESTCASE_ON_ERROR, "Fail TestCase if it has failed TestSteps" );
			form.addCheckBox( DISCARD_OK_RESULTS, "Discards successfull testresults to preserve memory" );
			form.addTextField( SOCKET_TIMEOUT, "Socket timeout in milliseconds", FieldType.TEXT );
			form.addTextField( TESTCASE_TIMEOUT, "Timeout in milliseconds for entire TestCase", FieldType.TEXT );
			form.addTextField( MAXRESULTS, "Maximum number of TestStep Results to keep in memory during a run",
					FieldType.TEXT );

			dialog = builder.buildDialog( builder.buildOkCancelHelpActions( HelpUrls.TESTCASEOPTIONS_HELP_URL ),
					"Specify general options for this TestCase", UISupport.OPTIONS_ICON );
		}

		StringToStringMap values = new StringToStringMap();

		values.put( SEARCH_PROPERTIES, String.valueOf( testCase.getSearchProperties() ) );
		values.put( KEEP_SESSION, String.valueOf( testCase.getKeepSession() ) );
		values.put( FAIL_ON_ERROR, String.valueOf( testCase.getFailOnError() ) );
		values.put( FAIL_TESTCASE_ON_ERROR, String.valueOf( testCase.getFailTestCaseOnErrors() ) );
		values.put( DISCARD_OK_RESULTS, String.valueOf( testCase.getDiscardOkResults() ) );
		values
				.put( SOCKET_TIMEOUT, String.valueOf( testCase.getSettings().getString( HttpSettings.SOCKET_TIMEOUT, "" ) ) );
		values.put( TESTCASE_TIMEOUT, String.valueOf( testCase.getTimeout() ) );
		values.put( MAXRESULTS, String.valueOf( testCase.getMaxResults() ) );

		dialog.getFormField( FAIL_TESTCASE_ON_ERROR ).setEnabled(
				!Boolean.parseBoolean( String.valueOf( testCase.getFailOnError() ) ) );

		values = dialog.show( values );

		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			try
			{
				testCase.setSearchProperties( Boolean.parseBoolean( values.get( SEARCH_PROPERTIES ) ) );
				testCase.setKeepSession( Boolean.parseBoolean( values.get( KEEP_SESSION ) ) );
				testCase.setDiscardOkResults( Boolean.parseBoolean( values.get( DISCARD_OK_RESULTS ) ) );
				testCase.setFailOnError( Boolean.parseBoolean( values.get( FAIL_ON_ERROR ) ) );
				testCase.setFailTestCaseOnErrors( Boolean.parseBoolean( values.get( FAIL_TESTCASE_ON_ERROR ) ) );
				testCase.setTimeout( Long.parseLong( values.get( TESTCASE_TIMEOUT ) ) );
				testCase.setMaxResults( Integer.parseInt( values.get( MAXRESULTS ) ) );

				String timeout = values.get( SOCKET_TIMEOUT );
				if( timeout.trim().length() == 0 )
					testCase.getSettings().clearSetting( HttpSettings.SOCKET_TIMEOUT );
				else
					testCase.getSettings().setString( HttpSettings.SOCKET_TIMEOUT, timeout );
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1.getMessage() );
			}
		}
	}
}