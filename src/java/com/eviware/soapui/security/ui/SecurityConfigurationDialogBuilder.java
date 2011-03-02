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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.panels.support.MockSecurityTestRunner;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.APage;
import com.eviware.x.form.support.XFormRadioGroup;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTabbedFormDialog;

public class SecurityConfigurationDialogBuilder
{

	public static final String TABS_NAME = "###Tabs";
	public static final String PARAMETERS_NAME = "Parameters";
	public static final String OPTIONAL_NAME = "###Optional";

	private GroovyEditorComponent setupGroovyEditor;
	private GroovyEditorComponent tearDownGroovyEditor;

	public XFormDialog buildSecurityCheckConfigurationDialog( AbstractSecurityCheck securityCheck )
	{

		return buildSecurityCheckConfigurationDialog( securityCheck.getConfigName(),
				securityCheck.getConfigDescription(), securityCheck.getIcon(), securityCheck.getHelpURL(), securityCheck
						.getComponent(), securityCheck );
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
			SecurityCheck securityCheck, XFormDialog dialog )
	{
		XFormField field = dialog.getFormField( PARAMETERS_NAME );
		if( field != null )
			addParameterTable( securityCheck, field );

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

		tabDialog.getFormField( Assertions.ASSERTIONS ).setProperty( "component",
				new SecurityAssertionPanel( ( Assertable )securityCheck ) );
		tabDialog.getFormField( Assertions.ASSERTIONS ).setProperty( "dimension", new Dimension( 345, 165 ) );

		tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "component", buildSetupScriptPanel( securityCheck ) );
		tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "dimension", new Dimension( 385, 165 ) );

		tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty( "component",
				buildTearDownScriptPanel( securityCheck ) );

		tabDialog.getFormField( AdvancedSettings.SETTINGS ).setProperty( "component",
				securityCheck.getAdvancedSettingsPanel() );
		tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty( "dimension", new Dimension( 360, 165 ) );

		addStrategyPanel( tabDialog, securityCheck );
		tabs.setProperty( "component", tabDialog.getTabs() );

	}

	private void addStrategyPanel( XFormDialog dialog, final SecurityCheck securityCheck )
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
		strategy.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{

				if( newValue.equals( strategyOptions ) )
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

	}

	private GroovyEditorComponent buildTearDownScriptPanel( SecurityCheck securityCheck )
	{
		tearDownGroovyEditor = new GroovyEditorComponent( new TearDownScriptGroovyEditorModel(
				( ( Assertable )securityCheck ).getModelItem() ), null );
		return tearDownGroovyEditor;
	}

	protected GroovyEditorComponent buildSetupScriptPanel( SecurityCheck securityCheck )
	{
		setupGroovyEditor = new GroovyEditorComponent( new SetupScriptGroovyEditorModel( ( ( Assertable )securityCheck )
				.getModelItem() ), null );

		return setupGroovyEditor;
	}

	/*
	 * 
	 * Adds Parameter table
	 * 
	 * @param securityCheck
	 * 
	 * @param field
	 */
	protected void addParameterTable( SecurityCheck securityCheck, XFormField field )
	{
		field.setProperty( "component", new SecurityCheckedParametersTablePanel( new SecurityParametersTableModel(
				( ( AbstractSecurityCheckWithProperties )securityCheck ).getParameterHolder() ), securityCheck
				.getTestStep().getProperties() ) );
	}

	public XFormDialog buildSecurityCheckConfigurationDialog( String name, String description, ImageIcon icon,
			String helpUrl, JComponent component, AbstractSecurityCheck securityCheck )
	{

		XFormDialog dialog = null;
		if( component == null )
		{
			if( securityCheck instanceof AbstractSecurityCheckWithProperties )
				dialog = ADialogBuilder.buildDialog( DialogWithParameters.class );
			else
				dialog = ADialogBuilder.buildDialog( DefaultDialog.class );
		}
		else
		{
			if( securityCheck instanceof AbstractSecurityCheckWithProperties )
				dialog = ADialogBuilder.buildDialog( OptionalDialogWithParameters.class );
			else
				dialog = ADialogBuilder.buildDialog( OptionalDialog.class );
			dialog.getFormField( OptionalDialog.OPTIONAL ).setProperty( "component", component );
		}

		buildBasicDialog( name, description, icon, helpUrl, securityCheck, dialog );

		( ( JFormDialog )dialog ).getDialog().setResizable( false );

		return dialog;
	}

	/**
	 * General forms
	 * 
	 * @author robert
	 * 
	 */
	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DefaultDialog
	{

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DialogWithParameters
	{

		@AField( description = "Parameters to Check", name = PARAMETERS_NAME, type = AFieldType.COMPONENT )
		public final static String PARAMETERS = PARAMETERS_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface OptionalDialog
	{

		@AField( description = "Optinal", name = OPTIONAL_NAME, type = AFieldType.COMPONENT )
		public final static String OPTIONAL = OPTIONAL_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface OptionalDialogWithParameters
	{

		@AField( description = "Parameters to Check", name = PARAMETERS_NAME, type = AFieldType.COMPONENT )
		public final static String PARAMETERS = PARAMETERS_NAME;

		@AField( description = "Optinal", name = OPTIONAL_NAME, type = AFieldType.COMPONENT )
		public final static String OPTIONAL = OPTIONAL_NAME;

		@AField( description = "Tabs", name = TABS_NAME, type = AFieldType.COMPONENT )
		public final static String TABS = TABS_NAME;

	}

	/*
	 * Other parts of configuration panel
	 */
	@AForm( description = "Security Check Tabs", name = "" )
	private interface TabsForm
	{
		@APage( name = "Assertions" )
		public final static Assertions ASSERTIONS = null;

		@APage( name = "Strategy" )
		public final static Strategy STRATEGY = null;

		@APage( name = "Advanced" )
		public final static AdvancedSettings ADVANCED = null;

		@APage( name = "SetUp" )
		public final static SetupScript SETUP = null;

		@APage( name = "TearDown" )
		public final static TearDownScript TEARDOWN = null;
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

		@AField( description = "Request Delay", name = "Request Delay", type = AFieldType.INT )
		public final static String DELAY = "Request Delay";

	}

	@AForm( description = "Advanced Settings", name = "Advanced Settings" )
	protected interface AdvancedSettings
	{

		@AField( description = "Settings", name = "Settings", type = AFieldType.COMPONENT )
		public final static String SETTINGS = "Settings";

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

	private class SetupScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{

					MockSecurityTestRunner securityTestRunner = new MockSecurityTestRunner(
							( SecurityTest )( ( AbstractSecurityCheck )SetupScriptGroovyEditorModel.this.getModelItem() )
									.getParent() );
					try
					{
						( ( SecurityCheck )SetupScriptGroovyEditorModel.this.getModelItem() ).runSetupScript(
								securityTestRunner, ( SecurityTestRunContext )securityTestRunner.getRunContext() );
					}
					catch( Exception e1 )
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
		}

		public SetupScriptGroovyEditorModel( ModelItem modelItem )
		{
			super( new String[] { "log", "context", "securityCheck" }, modelItem, "Setup" );
		}

		public String getScript()
		{
			return ( ( SecurityCheck )getModelItem() ).getSetupScript();
		}

		public void setScript( String text )
		{
			( ( SecurityCheck )getModelItem() ).setSetupScript( text );
		}
	}

	private class TearDownScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{

					try
					{
						MockSecurityTestRunner securityTestRunner = new MockSecurityTestRunner(
								( SecurityTest )( ( AbstractSecurityCheck )TearDownScriptGroovyEditorModel.this.getModelItem() )
										.getParent() );
						( ( SecurityCheck )TearDownScriptGroovyEditorModel.this.getModelItem() ).runTearDownScript(
								securityTestRunner, ( SecurityTestRunContext )securityTestRunner.getRunContext() );
					}
					catch( Exception e1 )
					{
						UISupport.showErrorMessage( e1 );
					}

				}
			};
		}

		public TearDownScriptGroovyEditorModel( ModelItem modelItem )
		{
			super( new String[] { "log", "context", "securityCheck" }, modelItem, "TearDown" );
		}

		public String getScript()
		{
			return ( ( SecurityCheck )getModelItem() ).getTearDownScript();
		}

		public void setScript( String text )
		{
			( ( SecurityCheck )getModelItem() ).setTearDownScript( text );
		}

	}

}
