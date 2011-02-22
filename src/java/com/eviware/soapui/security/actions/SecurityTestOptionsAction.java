/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.actions;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Options dialog for securitytests
 * 
 * @author dragica.soldo
 */

public class SecurityTestOptionsAction extends AbstractSoapUIAction<SecurityTest>
{
	private static final String FAIL_ON_ERROR = "Abort on Error";
	private static final String FAIL_SECURITYTEST_ON_ERROR = "Fail SecurityTest on Error";
	private static final String SOCKET_TIMEOUT = "Socket timeout";
	public static final String SOAPUI_ACTION_ID = "SecurityTestOptionsAction";
	private static final String SECURITYTEST_TIMEOUT = "SecurityTest timeout";
	private static final String MAXRESULTS = "Max Results";

	private XFormDialog dialog;
	private XForm form;

	public SecurityTestOptionsAction()
	{
		super( "Options", "Sets options for this SecurityTest" );
	}

	public void perform( SecurityTest securityTest, Object param )
	{
		if( dialog == null )
		{
			XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "SecurityTest Options" );
			form = builder.createForm( "Basic" );
			form.addCheckBox( FAIL_ON_ERROR, "Fail on error" );
			form.addCheckBox( FAIL_SECURITYTEST_ON_ERROR, "Fail SecurityTest if it has failed TestSteps" );
			// form.addTextField( SOCKET_TIMEOUT, "Socket timeout in milliseconds",
			// FieldType.TEXT );
			// form.addTextField( SECURITYTEST_TIMEOUT,
			// "Timeout in milliseconds for entire SecurityTest", FieldType.TEXT );
			// form.addTextField( MAXRESULTS,
			// "Maximum number of TestStep results to keep in memory during a run",
			// FieldType.TEXT );

			dialog = builder.buildDialog( builder.buildOkCancelHelpActions( HelpUrls.TESTCASEOPTIONS_HELP_URL ),
					"Specify general options for this SecurityTest", UISupport.OPTIONS_ICON );
		}

		StringToStringMap values = new StringToStringMap();

		values.put( FAIL_ON_ERROR, String.valueOf( securityTest.getFailOnError() ) );
		values.put( FAIL_SECURITYTEST_ON_ERROR, String.valueOf( securityTest.getFailSecurityTestOnCheckErrors() ) );
		// values.put( SOCKET_TIMEOUT, String.valueOf( securityTest.getSettings()
		// .getString( HttpSettings.SOCKET_TIMEOUT, "" ) ) );
		// values.put( TESTCASE_TIMEOUT, String.valueOf( securityTest.getTimeout()
		// ) );
		// values.put( MAXRESULTS, String.valueOf( securityTest.getMaxResults() )
		// );

		values = dialog.show( values );

		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			try
			{
				securityTest.setFailOnError( Boolean.parseBoolean( values.get( FAIL_ON_ERROR ) ) );
				securityTest.setFailSecurityTestOnCheckErrors( Boolean.parseBoolean( values
						.get( FAIL_SECURITYTEST_ON_ERROR ) ) );
				// securityTest.setTimeout( Long.parseLong( values.get(
				// TESTCASE_TIMEOUT ) ) );
				// securityTest.setMaxResults( Integer.parseInt( values.get(
				// MAXRESULTS ) ) );

				// String timeout = values.get( SOCKET_TIMEOUT );
				// if( timeout.trim().length() == 0 )
				// securityTest.getSettings().clearSetting(
				// HttpSettings.SOCKET_TIMEOUT );
				// else
				// securityTest.getSettings().setString(
				// HttpSettings.SOCKET_TIMEOUT, timeout );

			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1.getMessage() );
			}
		}
	}
}
