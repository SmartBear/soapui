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
package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.APage;
import com.eviware.x.form.support.XFormRadioGroup;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTabbedFormDialog;

public class SecurityConfigurationDialogBuilder
{

	public static final String TABS_NAME = "###Tabs";
	public static final String PARAMETERS_NAME = "Parameters";
	public static final String OPTIONAL_NAME = "###Optional";

	// private GroovyEditorComponent setupGroovyEditor;
	// private GroovyEditorComponent tearDownGroovyEditor;

	public SecurityConfigurationDialog buildSecurityScanConfigurationDialog( SecurityScan securityCheck )
	{
		return new SecurityConfigurationDialog( securityCheck );

		// return buildSecurityScanConfigurationDialog(
		// securityScan.getConfigName(),
		// securityScan.getConfigDescription(), securityScan.getIcon(),
		// securityScan.getHelpURL(),
		// securityScan.getComponent(), securityScan );
	}

	/**
	 * @param name
	 * @param description
	 * @param icon
	 * @param helpUrl
	 * @param securityCheck
	 * @param dialog
	 */
	private void buildBasicDialog( String name, String description, ImageIcon icon, String helpUrl,
			SecurityScan securityCheck, XFormDialog dialog )
	{
		XFormField field = dialog.getFormField( PARAMETERS_NAME );
		if( field != null )
		{
			addParameterTable( securityCheck, field );
		}

		Container content = ( ( JFormDialog )dialog ).getDialog().getContentPane();

		if( description != null || icon != null )
		{
			content.remove( 0 );
			content.add( UISupport.buildDescription( name, description, icon ), BorderLayout.NORTH );
		}

		if( helpUrl != null )
		{
			( ( JFormDialog )dialog ).setHelpUrl( helpUrl );
		}

		XFormField tabs = dialog.getFormField( DefaultDialog.TABS );

		JTabbedFormDialog tabDialog = ( ( JTabbedFormDialog )ADialogBuilder.buildTabbedDialog( TabsForm.class, null ) );

		// tabDialog.setResizable( true );
		// tabDialog.setSize( 600, 400 );

		tabDialog.getFormField( Assertions.ASSERTIONS ).setProperty( "component",
				new SecurityAssertionPanel( ( Assertable )securityCheck ) );
		tabDialog.getFormField( Assertions.ASSERTIONS ).setProperty( "dimension", new Dimension( 345, 170 ) );

		// tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "component",
		// buildSetupScriptPanel( securityCheck ) );
		// tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "dimension",
		// new Dimension( 385, 260 ) );
		//
		// tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty(
		// "component",
		// buildTearDownScriptPanel( securityCheck ) );
		// tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty(
		// "dimension", new Dimension( 360, 260 ) );

		tabDialog.getFormField( AdvancedSettings.SETTINGS ).setProperty( "component",
				new JScrollPane( securityCheck.getAdvancedSettingsPanel() ) );
		tabDialog.getFormField( AdvancedSettings.SETTINGS ).setProperty( "dimension", new Dimension( 410, 170 ) );

		addStrategyPanel( tabDialog, securityCheck );
		tabs.setProperty( "component", tabDialog.getTabs() );
		tabs.setProperty( "dimension", new Dimension( 420, 170 ) );

		XFormField parametersFormField = dialog.getFormField( PARAMETERS_NAME );
		if( parametersFormField != null )
		{
			parametersFormField.setProperty( "preferredSize", new Dimension( 400, 150 ) );
		}

	}

	private void addStrategyPanel( XFormDialog dialog, final SecurityScan securityCheck )
	{
		XFormRadioGroup strategy = ( XFormRadioGroup )dialog.getFormField( Strategy.STRATEGY );
		final String[] strategyOptions = new String[] { "One by One", "All At Once" };
		strategy.setOptions( strategyOptions );

		if( securityCheck.getExecutionStrategy().getStrategy() == StrategyTypeConfig.NO_STRATEGY )
			strategy.setEnabled( false );
		else
		{
			if( securityCheck.getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
				strategy.setValue( strategyOptions[0] );
			else
				strategy.setValue( strategyOptions[1] );
		}

		// default is ONE_BY_ONE
		if( securityCheck.getExecutionStrategy().getImmutable() )
		{
			strategy.setDisabled();
		}

		strategy.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{

				if( newValue.equals( strategyOptions[0] ) )
					securityCheck.getExecutionStrategy().setStrategy( StrategyTypeConfig.ONE_BY_ONE );
				else
					securityCheck.getExecutionStrategy().setStrategy( StrategyTypeConfig.ALL_AT_ONCE );

			}
		} );

		XFormField delay = dialog.getFormField( Strategy.DELAY );
		delay.setValue( String.valueOf( securityCheck.getExecutionStrategy().getDelay() ) );

		delay.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				try
				{
					Integer.valueOf( newValue );
					securityCheck.getExecutionStrategy().setDelay( Integer.valueOf( newValue ) );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( "Delay value must be integer number" );
				}
			}
		} );
		XFormField applyToFailedTests = dialog.getFormField( Strategy.APPLY_TO_FAILED_STEPS );
		applyToFailedTests.setValue( String.valueOf( securityCheck.isApplyForFailedStep() ) );
		applyToFailedTests.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				securityCheck.setApplyForFailedTestStep( Boolean.parseBoolean( newValue ) );
			}
		} );
		XFormField runOnlyOnce = dialog.getFormField( Strategy.RUN_ONLY_ONCE );
		runOnlyOnce.setValue( String.valueOf( securityCheck.isRunOnlyOnce() ) );
		runOnlyOnce.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				securityCheck.setRunOnlyOnce( Boolean.parseBoolean( newValue ) );
			}
		} );

	}

	// private GroovyEditorComponent buildTearDownScriptPanel( SecurityScan
	// securityCheck )
	// {
	// tearDownGroovyEditor = new GroovyEditorComponent( new
	// TearDownScriptGroovyEditorModel(
	// ( ( Assertable )securityCheck ).getModelItem() ), null );
	// return tearDownGroovyEditor;
	// }
	//
	// protected GroovyEditorComponent buildSetupScriptPanel( SecurityScan
	// securityCheck )
	// {
	// setupGroovyEditor = new GroovyEditorComponent( new
	// SetupScriptGroovyEditorModel(
	// ( ( Assertable )securityCheck ).getModelItem() ), null );
	//
	// return setupGroovyEditor;
	// }

	/*
	 * 
	 * Adds Parameter table
	 * 
	 * @param securityCheck
	 * 
	 * @param field
	 */
	protected void addParameterTable( SecurityScan securityCheck, XFormField field )
	{
		field.setProperty( "component", new SecurityCheckedParametersTablePanel( new SecurityParametersTableModel(
				( ( AbstractSecurityScanWithProperties )securityCheck ).getParameterHolder() ), securityCheck.getTestStep()
				.getProperties(), ( AbstractSecurityScanWithProperties )securityCheck ) );
	}

	private XFormDialog buildSecurityScanConfigurationDialog( String name, String description, ImageIcon icon,
			String helpUrl, JComponent component, SecurityScan securityCheck )
	{
		XFormDialog dialog = null;
		if( component == null )
		{
			if( securityCheck instanceof AbstractSecurityScanWithProperties )
				dialog = ADialogBuilder.buildDialog( DialogWithParameters.class );
			else
				dialog = ADialogBuilder.buildDialog( DefaultDialog.class );
		}
		else
		{
			if( securityCheck instanceof AbstractSecurityScanWithProperties )
				dialog = ADialogBuilder.buildDialog( OptionalDialogWithParameters.class );
			else
				dialog = ADialogBuilder.buildDialog( OptionalDialog.class );

			dialog.getFormField( OptionalDialog.OPTIONAL ).setProperty( "component", component );
			dialog.getFormField( OptionalDialog.OPTIONAL ).setProperty( "dimension", component.getPreferredSize() );
		}

		buildBasicDialog( name, description, icon, helpUrl, securityCheck, dialog );

		// ( ( JFormDialog )dialog ).getDialog().setResizable( false );

		return dialog;
	}

	/**
	 * General forms
	 * 
	 * @author robert
	 * 
	 */
	@AForm( description = "Configure Security Scan", name = "Configure Security Scan", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DefaultDialog
	{

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	@AForm( description = "Configure Security Scan", name = "Configure Security Scan", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DialogWithParameters
	{
		@AField( description = "Parameters to Scan", name = PARAMETERS_NAME, type = AFieldType.COMPONENT )
		public final static String PARAMETERS = PARAMETERS_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;
	}

	@AForm( description = "Configure Security Scan", name = "Configure Security Scan", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface OptionalDialog
	{

		@AField( description = "Optinal", name = OPTIONAL_NAME, type = AFieldType.COMPONENT )
		public final static String OPTIONAL = OPTIONAL_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	@AForm( description = "Configure Security Scan", name = "Configure Security Scan", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface OptionalDialogWithParameters
	{

		@AField( description = "Parameters to Scan", name = PARAMETERS_NAME, type = AFieldType.COMPONENT )
		public final static String PARAMETERS = PARAMETERS_NAME;

		@AField( description = "Optinal", name = OPTIONAL_NAME, type = AFieldType.COMPONENT )
		public final static String OPTIONAL = OPTIONAL_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	/*
	 * Other parts of configuration panel
	 */
	@AForm( description = "Security Scan Tabs", name = "" )
	private interface TabsForm
	{
		@APage( name = "Assertions" )
		public final static Assertions ASSERTIONS = null;

		@APage( name = "Strategy" )
		public final static Strategy STRATEGY = null;

		@APage( name = "Advanced" )
		public final static AdvancedSettings ADVANCED = null;

		// @APage( name = "SetUp" )
		// public final static SetupScript SETUP = null;
		//
		// @APage( name = "TearDown" )
		// public final static TearDownScript TEARDOWN = null;
	}

	@AForm( description = "Assertions", name = "Assertions" )
	protected interface Assertions
	{

		@AField( description = "Assertions", name = "###Security assertions", type = AFieldType.COMPONENT )
		public final static String ASSERTIONS = "###Security assertions";

	}

	@AForm( description = "Strategy", name = "Strategy" )
	protected interface Strategy
	{

		@AField( description = "Strategy", name = "Select strategy", type = AFieldType.RADIOGROUP )
		public final static String STRATEGY = "Select strategy";

		@AField( description = "Request Delay", name = "Request Delay(ms)", type = AFieldType.INT )
		public final static String DELAY = "Request Delay(ms)";

		@AField( description = "Apply to Failed TestSteps", name = "Apply to Failed TestSteps", type = AFieldType.BOOLEAN )
		public final static String APPLY_TO_FAILED_STEPS = "Apply to Failed TestSteps";

		//indicates if security scan should run only once in case of DataSource Loop involved
		@AField( description = "Run only once", name = "Run only once", type = AFieldType.BOOLEAN )
		public final static String RUN_ONLY_ONCE = "Run only once";

	}

	@AForm( description = "Advanced Settings", name = "Advanced Settings" )
	protected interface AdvancedSettings
	{

		@AField( description = "Settings", name = "###Settings", type = AFieldType.COMPONENT )
		public final static String SETTINGS = "###Settings";

	}

	@AForm( description = "Setup Script", name = "Setup Script" )
	protected interface SetupScript
	{

		@AField( description = "Script", name = "###Setup script", type = AFieldType.COMPONENT )
		public final static String SCRIPT = "###Setup script";

	}

	@AForm( description = "TearDown Script", name = "TearDown Script" )
	protected interface TearDownScript
	{

		@AField( description = "TearDown Script", name = "###TearDown Script", type = AFieldType.COMPONENT )
		public final static String SCRIPT = "###TearDown Script";

	}

	// private class SetupScriptGroovyEditorModel extends
	// AbstractGroovyEditorModel
	// {
	// @Override
	// public Action createRunAction()
	// {
	// return new AbstractAction()
	// {
	//
	// public void actionPerformed( ActionEvent e )
	// {
	//
	// // MockSecurityTestRunner securityTestRunner = new
	// // MockSecurityTestRunner(
	// // ( SecurityTest )( ( SecurityScan
	// // )SetupScriptGroovyEditorModel.this.getModelItem()
	// // ).getParent() );
	// // try
	// // {
	// // ( ( SecurityScan
	// // )SetupScriptGroovyEditorModel.this.getModelItem()
	// // ).runSetupScript(
	// // securityTestRunner, ( SecurityTestRunContext
	// // )securityTestRunner.getRunContext() );
	// // }
	// // catch( Exception e1 )
	// // {
	// // UISupport.showErrorMessage( e1 );
	// // }
	// }
	// };
	// }
	//
	// public SetupScriptGroovyEditorModel( ModelItem modelItem )
	// {
	// super( new String[] { "log", "context", "securityScan" }, modelItem,
	// "Setup" );
	// }
	//
	// public String getScript()
	// {
	// return ( ( SecurityScan )getModelItem() ).getSetupScript();
	// }
	//
	// public void setScript( String text )
	// {
	// ( ( SecurityScan )getModelItem() ).setSetupScript( text );
	// }
	// }
	//
	// private class TearDownScriptGroovyEditorModel extends
	// AbstractGroovyEditorModel
	// {
	// @Override
	// public Action createRunAction()
	// {
	// return new AbstractAction()
	// {
	// public void actionPerformed( ActionEvent e )
	// {
	// // try
	// // {
	// // MockSecurityTestRunner securityTestRunner = new
	// // MockSecurityTestRunner(
	// // ( SecurityTest )( ( SecurityScan
	// // )TearDownScriptGroovyEditorModel.this.getModelItem() )
	// // .getParent() );
	// // ( ( SecurityScan
	// // )TearDownScriptGroovyEditorModel.this.getModelItem()
	// // ).runTearDownScript(
	// // securityTestRunner, ( SecurityTestRunContext
	// // )securityTestRunner.getRunContext() );
	// // }
	// // catch( Exception e1 )
	// // {
	// // UISupport.showErrorMessage( e1 );
	// // }
	// }
	// };
	// }
	//
	// public TearDownScriptGroovyEditorModel( ModelItem modelItem )
	// {
	// super( new String[] { "log", "context", "securityScan" }, modelItem,
	// "TearDown" );
	// }
	//
	// public String getScript()
	// {
	// return ( ( SecurityScan )getModelItem() ).getTearDownScript();
	// }
	//
	// public void setScript( String text )
	// {
	// ( ( SecurityScan )getModelItem() ).setTearDownScript( text );
	// }
	//
	// }

}
