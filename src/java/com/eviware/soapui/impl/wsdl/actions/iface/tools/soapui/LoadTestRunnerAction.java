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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.soapui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.AbstractToolsAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ArgumentBuilder;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ProcessToolRunner;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.ToolHost;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;

/**
 * Invokes soapUI TestRunner tool
 * 
 * @author Ole.Matzura
 */

public class LoadTestRunnerAction extends AbstractToolsAction<WsdlProject>
{
	private static final String ALL_VALUE = "<all>";
	private static final String ENDPOINT = "Endpoint";
	private static final String HOSTPORT = "Host:Port";
	private static final String LIMIT = "Limit";
	private static final String TESTSUITE = "TestSuite";
	private static final String TESTCASE = "TestCase";
	private static final String LOADTEST = "LoadTest";
	private static final String THREADCOUNT = "ThreadCount";
	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String DOMAIN = "Domain";
	private static final String PRINTREPORT = "Print Report";
	private static final String ROOTFOLDER = "Root Folder";
	private static final String TESTRUNNERPATH = "TestRunner Path";
	private static final String SAVEPROJECT = "Save Project";
	private static final String ADDSETTINGS = "Add Settings";

	private XForm mainForm;
	private final static Logger log = Logger.getLogger( LoadTestRunnerAction.class );
	public static final String SOAPUI_ACTION_ID = "LoadTestRunnerAction";
	private XForm advForm;
	private boolean updating;

	public LoadTestRunnerAction()
	{
		super( "Launch LoadTestRunner", "Launch command-line LoadTestRunner for this project" );
	}

	protected XFormDialog buildDialog( WsdlProject modelItem )
	{
		if( modelItem == null )
			return null;

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Launch LoadTestRunner" );

		mainForm = builder.createForm( "Basic" );
		mainForm.addComboBox( TESTSUITE, new String[] {}, "The TestSuite to run" ).addFormFieldListener(
				new XFormFieldListener()
				{

					public void valueChanged( XFormField sourceField, String newValue, String oldValue )
					{
						updateCombos();
					}
				} );

		mainForm.addComboBox( TESTCASE, new String[] {}, "The TestCase to run" ).addFormFieldListener(
				new XFormFieldListener()
				{

					public void valueChanged( XFormField sourceField, String newValue, String oldValue )
					{
						updateCombos();
					}
				} );
		mainForm.addComboBox( LOADTEST, new String[] {}, "The LoadTest to run" );
		mainForm.addSeparator();
		mainForm.addCheckBox( PRINTREPORT, "Creates a report in the specified folder" );
		mainForm.addTextField( ROOTFOLDER, "Folder for reporting", XForm.FieldType.FOLDER );
		mainForm.addSeparator();
		mainForm.addTextField( TESTRUNNERPATH, "Folder containing TestRunner.bat to use", XForm.FieldType.FOLDER );
		mainForm.addCheckBox( SAVEPROJECT, "Saves project before running" ).setEnabled( !modelItem.isRemote() );
		;
		mainForm.addCheckBox( ADDSETTINGS, "Adds global settings to command-line" );

		advForm = builder.createForm( "Overrides" );
		advForm.addComboBox( ENDPOINT, new String[] { "" }, "endpoint to forward to" );
		advForm.addTextField( HOSTPORT, "Host:Port to use for requests", XForm.FieldType.TEXT );
		advForm.addTextField( LIMIT, "Limit for LoadTest", XForm.FieldType.TEXT );
		advForm.addTextField( THREADCOUNT, "ThreadCount for LoadTest", XForm.FieldType.TEXT );
		advForm.addSeparator();
		advForm.addTextField( USERNAME, "The username to set for all requests", XForm.FieldType.TEXT );
		advForm.addTextField( PASSWORD, "The password to set for all requests", XForm.FieldType.PASSWORD );
		advForm.addTextField( DOMAIN, "The domain to set for all requests", XForm.FieldType.TEXT );

		setToolsSettingsAction( null );
		buildArgsForm( builder, false, "TestRunner" );

		return builder.buildDialog( buildDefaultActions( HelpUrls.TESTRUNNER_HELP_URL, modelItem ),
				"Specify arguments for launching soapUI LoadTestRunner", UISupport.TOOL_ICON );
	}

	protected Action createRunOption( WsdlProject modelItem )
	{
		Action action = super.createRunOption( modelItem );
		action.putValue( Action.NAME, "Launch" );
		return action;
	}

	protected StringToStringMap initValues( WsdlProject modelItem, Object param )
	{
		if( modelItem != null && mainForm != null )
		{
			List<String> endpoints = new ArrayList<String>();

			for( Interface iface : modelItem.getInterfaceList() )
			{
				for( String endpoint : iface.getEndpoints() )
				{
					if( !endpoints.contains( endpoint ) )
						endpoints.add( endpoint );
				}
			}

			endpoints.add( 0, null );
			advForm.setOptions( ENDPOINT, endpoints.toArray() );
			List<TestSuite> testSuites = modelItem.getTestSuiteList();
			for( int c = 0; c < testSuites.size(); c++ )
			{
				int cnt = 0;

				for( TestCase testCase : testSuites.get( c ).getTestCaseList() )
				{
					cnt += testCase.getLoadTestCount();
				}

				if( cnt == 0 )
				{
					testSuites.remove( c );
					c-- ;
				}
			}

			mainForm.setOptions( TESTSUITE, ModelSupport.getNames( new String[] { ALL_VALUE }, testSuites ) );
		}
		else if( mainForm != null )
		{
			mainForm.setOptions( ENDPOINT, new String[] { null } );
		}

		StringToStringMap values = super.initValues( modelItem, param );
		updateCombos();

		if( mainForm != null && param instanceof WsdlLoadTest )
		{
			mainForm.getFormField( TESTSUITE ).setValue( ( ( WsdlLoadTest )param ).getTestCase().getTestSuite().getName() );
			mainForm.getFormField( TESTCASE ).setValue( ( ( WsdlLoadTest )param ).getTestCase().getName() );
			mainForm.getFormField( LOADTEST ).setValue( ( ( WsdlLoadTest )param ).getName() );

			values.put( TESTSUITE, mainForm.getComponentValue( TESTSUITE ) );
			values.put( TESTCASE, mainForm.getComponentValue( TESTCASE ) );
			values.put( LOADTEST, mainForm.getComponentValue( LOADTEST ) );

			mainForm.getComponent( SAVEPROJECT ).setEnabled( !modelItem.isRemote() );
		}

		return values;
	}

	protected void generate( StringToStringMap values, ToolHost toolHost, WsdlProject modelItem ) throws Exception
	{
		String testRunnerDir = mainForm.getComponentValue( TESTRUNNERPATH );

		ProcessBuilder builder = new ProcessBuilder();
		ArgumentBuilder args = buildArgs( modelItem );
		builder.command( args.getArgs() );
		if( StringUtils.isNullOrEmpty( testRunnerDir ) )
			builder.directory( new File( "." ) );
		else
			builder.directory( new File( testRunnerDir ) );

		if( mainForm.getComponentValue( SAVEPROJECT ).equals( Boolean.TRUE.toString() ) )
		{
			modelItem.save();
		}
		else if( StringUtils.isNullOrEmpty( modelItem.getPath() ) )
		{
			UISupport.showErrorMessage( "Project [" + modelItem.getName() + "] has not been saved to file." );
			return;
		}

		if( log.isDebugEnabled() )
			log.debug( "Launching loadtestrunner in directory [" + builder.directory() + "] with arguments ["
					+ args.toString() + "]" );

		toolHost.run( new ProcessToolRunner( builder, "soapUI LoadTestRunner", modelItem, args ) );
	}

	private ArgumentBuilder buildArgs( WsdlProject modelItem ) throws IOException
	{
		if( dialog == null )
		{
			ArgumentBuilder builder = new ArgumentBuilder( new StringToStringMap() );
			builder.startScript( "loadtestrunner", ".bat", ".sh" );
			return builder;
		}

		StringToStringMap values = dialog.getValues();

		ArgumentBuilder builder = new ArgumentBuilder( values );

		builder.startScript( "loadtestrunner", ".bat", ".sh" );

		builder.addString( ENDPOINT, "-e", "" );
		builder.addString( HOSTPORT, "-h", "" );

		if( !values.get( TESTSUITE ).equals( ALL_VALUE ) )
			builder.addString( TESTSUITE, "-s", "" );

		if( !values.get( TESTCASE ).equals( ALL_VALUE ) )
			builder.addString( TESTCASE, "-c", "" );

		if( !values.get( LOADTEST ).equals( ALL_VALUE ) )
			builder.addString( LOADTEST, "-l", "" );

		builder.addString( LIMIT, "-m", "" );
		builder.addString( THREADCOUNT, "-h", "" );
		builder.addString( USERNAME, "-u", "" );
		builder.addStringShadow( PASSWORD, "-p", "" );
		builder.addString( DOMAIN, "-d", "" );

		builder.addBoolean( PRINTREPORT, "-r" );
		builder.addString( ROOTFOLDER, "-f", "" );

		if( dialog.getBooleanValue( ADDSETTINGS ) )
		{
			try
			{
				builder.addBoolean( ADDSETTINGS, "-t" + SoapUI.saveSettings() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		builder.addArgs( new String[] { modelItem.getPath() } );

		addToolArgs( values, builder );

		return builder;
	}

	private void updateCombos()
	{
		if( updating )
			return;

		updating = true;

		List<String> testCases = new ArrayList<String>();
		List<String> loadTests = new ArrayList<String>();

		TestSuite ts = getModelItem().getTestSuiteByName( mainForm.getComponentValue( TESTSUITE ) );
		String testCaseName = mainForm.getComponentValue( TESTCASE );
		if( ALL_VALUE.equals( testCaseName ) )
			testCaseName = null;

		for( TestSuite testSuite : getModelItem().getTestSuiteList() )
		{
			if( ts != null && testSuite != ts )
				continue;

			for( TestCase testCase : testSuite.getTestCaseList() )
			{
				if( testCase.getLoadTestCount() == 0 )
					continue;

				if( !testCases.contains( testCase.getName() ) )
					testCases.add( testCase.getName() );

				if( testCaseName != null && !testCase.getName().equals( testCaseName ) )
					continue;

				for( LoadTest loadTest : testCase.getLoadTestList() )
				{
					if( !loadTests.contains( loadTest.getName() ) )
						loadTests.add( loadTest.getName() );
				}
			}
		}

		testCases.add( 0, ALL_VALUE );
		mainForm.setOptions( TESTCASE, testCases.toArray() );

		loadTests.add( 0, ALL_VALUE );
		mainForm.setOptions( LOADTEST, loadTests.toArray() );

		updating = false;
	}
}
