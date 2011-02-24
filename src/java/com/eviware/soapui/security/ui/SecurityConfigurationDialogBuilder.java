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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.eviware.soapui.impl.wsdl.panels.support.MockSecurityTestRunner;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.APage;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTabbedFormDialog;

public class SecurityConfigurationDialogBuilder
{

	private GroovyEditorComponent setupGroovyEditor;
	private GroovyEditorComponent tearDownGroovyEditor;

	public XFormDialog buildSecurityCheckConfigurationDialog( AbstractSecurityCheck securityCheck )
	{

		return buildSecurityCheckConfigurationDialog( securityCheck.getConfigName(),
				securityCheck.getConfigDescription(), securityCheck.getIcon(), securityCheck.getHelpURL(), securityCheck
						.getComponent(), securityCheck );
	}

	/**
	 * 
	 * Creates a dialog for configuring security checks.
	 * 
	 * @param name
	 * @param description
	 * @param icon
	 * @param helpUrl
	 * @param securityCheck
	 * @return
	 */
	public XFormDialog buildSecurityCheckConfigurationDialog( String name, String description, ImageIcon icon,
			String helpUrl, AbstractSecurityCheck securityCheck )
	{

		XFormDialog dialog = ADialogBuilder.buildDialog( DefaultDialog.class );

		buildBasicDialog( name, description, icon, helpUrl, securityCheck, dialog );

		return dialog;
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
			AbstractSecurityCheck securityCheck, XFormDialog dialog )
	{
		XFormField field = dialog.getFormField( DefaultDialog.PARAMETERS );
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
				new SecurityAssertionPanel( securityCheck ) );
		tabDialog.getFormField( Assertions.ASSERTIONS ).setProperty( "dimension", new Dimension( 345, 165 ) );

		tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "component", buildSetupScriptPanel( securityCheck ) );
		tabDialog.getFormField( SetupScript.SCRIPT ).setProperty( "dimension", new Dimension( 385, 165 ) );

		tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty( "component",
				buildTearDownScriptPanel( securityCheck ) );
		tabDialog.getFormField( TearDownScript.SCRIPT ).setProperty( "dimension", new Dimension( 360, 165 ) );
		tabs.setProperty( "component", tabDialog.getTabs() );

	}

	private GroovyEditorComponent buildTearDownScriptPanel( AbstractSecurityCheck securityCheck )
	{
		tearDownGroovyEditor = new GroovyEditorComponent( new TearDownScriptGroovyEditorModel( securityCheck
				.getModelItem() ), null );
		return tearDownGroovyEditor;
	}

	protected GroovyEditorComponent buildSetupScriptPanel( AbstractSecurityCheck securityCheck )
	{
		setupGroovyEditor = new GroovyEditorComponent( new SetupScriptGroovyEditorModel( securityCheck.getModelItem() ),
				null );
		
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
	protected void addParameterTable( AbstractSecurityCheck securityCheck, XFormField field )
	{
		field.setProperty( "component", new SecurityCheckedParametersTable( new SecurityParametersTableModel(
				securityCheck.getParameterHolder() ), securityCheck.getTestStep().getProperties() ) );
	}

	public XFormDialog buildSecurityCheckConfigurationDialog( String name, String description, ImageIcon icon,
			String helpUrl, JComponent component, AbstractSecurityCheck securityCheck )
	{

		XFormDialog dialog = null;
		if( component == null )
			dialog = ADialogBuilder.buildDialog( DefaultDialog.class );
		else
			dialog = ADialogBuilder.buildDialog( OptionalDialog.class );

		if( component != null )
			dialog.getFormField( OptionalDialog.OPTIONAL ).setProperty( "component", component );
		buildBasicDialog( name, description, icon, helpUrl, securityCheck, dialog );

		return dialog;
	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DefaultDialog
	{

		@AField( description = "Parameters to Check", name = "Parameters", type = AFieldType.COMPONENT )
		public final static String PARAMETERS = "Parameters";

		@AField( description = "Tabs", name = "###Tabs", type = AFieldType.COMPONENT )
		public final static String TABS = "###Tabs";

	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface OptionalDialog
	{

		@AField( description = "Parameters to Check", name = "Parameters", type = AFieldType.COMPONENT )
		public final static String PARAMETERS = "Parameters";

		@AField( description = "Optinal", name = "###Optional", type = AFieldType.COMPONENT )
		public final static String OPTIONAL = "###Optional";

		@AField( description = "Tabs", name = "###Tabs", type = AFieldType.COMPONENT )
		public final static String TABS = "###Tabs";

	}

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

		@AField( description = "Strategy", name = "Select strategy", type = AFieldType.COMPONENT )
		public final static String STRATEGY = "Select strategy";

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
						( ( AbstractSecurityCheck )SetupScriptGroovyEditorModel.this.getModelItem() ).runSetupScript(
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
			return ( ( AbstractSecurityCheck )getModelItem() ).getSetupScript();
		}

		public void setScript( String text )
		{
			( ( AbstractSecurityCheck )getModelItem() ).setSetupScript( text );
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
						( ( AbstractSecurityCheck )TearDownScriptGroovyEditorModel.this.getModelItem() ).runTearDownScript(
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
			return ( ( AbstractSecurityCheck )getModelItem() ).getTearDownScript();
		}

		public void setScript( String text )
		{
			( ( AbstractSecurityCheck )getModelItem() ).setTearDownScript( text );
		}

	}

}
