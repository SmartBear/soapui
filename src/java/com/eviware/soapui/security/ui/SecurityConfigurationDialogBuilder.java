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

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.support.UISupport;
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

	public static XFormDialog buildSecurityCheckConfigurationDialog( AbstractSecurityCheck securityCheck )
	{

		XFormDialog dialog = ADialogBuilder.buildDialog( DefaultDialog.class );
		XFormField field = dialog.getFormField( DefaultDialog.PARAMETERS );
		field.setProperty( "component", new SecurityCheckedParametersTable( new SecurityParametersTableModel(
				securityCheck.getParameterHolder() ), securityCheck.getTestStep().getProperties() ) );

		return dialog;
	}

	public static XFormDialog buildSecurityCheckConfigurationDialog( String name, String description, ImageIcon icon,
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
	private static void buildBasicDialog( String name, String description, ImageIcon icon, String helpUrl,
			AbstractSecurityCheck securityCheck, XFormDialog dialog )
	{
		XFormField field = dialog.getFormField( DefaultDialog.PARAMETERS );
		field.setProperty( "component", new SecurityCheckedParametersTable( new SecurityParametersTableModel(
				securityCheck.getParameterHolder() ), securityCheck.getTestStep().getProperties() ) );

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
		tabs.setProperty( "component", ( ( JTabbedFormDialog )ADialogBuilder.buildTabbedDialog( TabsForm.class, null ) )
				.getTabs() );
	}

	public static XFormDialog buildSecurityCheckConfigurationDialog( String name, String description, ImageIcon icon,
			String helpUrl, JComponent component, AbstractSecurityCheck securityCheck )
	{

		XFormDialog dialog = null;
		if( component != null )
			dialog = ADialogBuilder.buildDialog( DefaultDialog.class );
		else
			dialog = ADialogBuilder.buildDialog( GroovyDialog.class );

		buildBasicDialog( name, description, icon, helpUrl, securityCheck, dialog );

		return dialog;
	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface DefaultDialog
	{

		@AField( description = "Parameters to Check", name = "Parameters", type = AFieldType.COMPONENT )
		public final static String PARAMETERS = "Parameters";

		@AField( description = "Tabs", name = "Tabs", type = AFieldType.COMPONENT )
		public final static String TABS = "Tabs";

	}

	@AForm( description = "Configure Security Check", name = "Configure Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface GroovyDialog
	{

		@AField( description = "Parameters to Check", name = "Parameters", type = AFieldType.COMPONENT )
		public final static String PARAMETERS = "Parameters";

		@AField( description = "Optinal", name = "Optional", type = AFieldType.COMPONENT )
		public final static String OPTIONAL = "Optional";

		@AField( description = "Tabs", name = "Tabs", type = AFieldType.COMPONENT )
		public final static String TABS = "Tabs";

	}

	@AForm( description = "Set options for this LoadTest", name = "LoadTest Options", helpUrl = HelpUrls.LOADTESTOPTIONS_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
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

	@AForm( description = "Assertions", name = "Assertions", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface Assertions
	{

		@AField( description = "Assertions", name = "Select assertions to apply", type = AFieldType.COMPONENT )
		public final static String ASSERTIONS = "Select assertions to apply";

	}

	@AForm( description = "Strategy", name = "Strategy", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface Strategy
	{

		@AField( description = "Strategy", name = "Select strategy", type = AFieldType.COMPONENT )
		public final static String STRATEGY = "Select strategy";

	}

	@AForm( description = "Advanced Settings", name = "Advanced Settings", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface AdvancedSettings
	{

		@AField( description = "Settings", name = "Settings", type = AFieldType.COMPONENT )
		public final static String SETTINGS = "Settings";

	}

	@AForm( description = "Setup Script", name = "Setup Script", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface SetupScript
	{

		@AField( description = "Script", name = "Setup script", type = AFieldType.COMPONENT )
		public final static String SCRIPT = "Setup script";

	}

	@AForm( description = "TearDown Script", name = "TearDown Script", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface TearDownScript
	{

		@AField( description = "TearDown Script", name = "TearDown Script", type = AFieldType.COMPONENT )
		public final static String SCRIPT = "TearDown Script";

	}
}
