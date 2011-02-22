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
package com.eviware.soapui.security.panels;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.SensitiveInformationConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JCheckBoxFormField;
import com.eviware.x.impl.swing.JStringListFormField;

public class ProjectSensitiveInformationPanel
{

	private XFormDialog dialog;
	private SensitiveInformationConfig config;
	private List<String> projectSpecificExposureList;
	private static final String PROJECT_SPECIFIC_EXPOSURE_LIST = "ProjectSpecificExposureList";
	private static final String USE_REGEXP = "UseRegexp";
	private boolean useRegexp;

	public ProjectSensitiveInformationPanel( ProjectConfig projectConfig )
	{
		config = projectConfig.getSensitiveInformation();
		if( config == null )
		{
			config = SensitiveInformationConfig.Factory.newInstance();
			projectConfig.addNewSensitiveInformation();
			projectConfig.setSensitiveInformation( config );
		}
		init();
	}

	private void init()
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config );
		useRegexp = reader.readBoolean( USE_REGEXP, false );
		projectSpecificExposureList = StringUtils.toStringList( reader.readStrings( PROJECT_SPECIFIC_EXPOSURE_LIST ) );
	}

	public boolean build()
	{
		if( dialog == null )
			buildDialog();

		return false;
	}

	public void save()
	{
		JStringListFormField jsringListFormField = ( JStringListFormField )dialog
				.getFormField( SensitiveInformationConfigDialog.INFOLIST );

		String[] stringList = jsringListFormField.getOptions();
		projectSpecificExposureList = StringUtils.toStringList( stringList );
		useRegexp = Boolean.valueOf( dialog.getFormField( SensitiveInformationConfigDialog.USE_REGEXP ).getValue() );
		setConfiguration( createConfiguration() );
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( PROJECT_SPECIFIC_EXPOSURE_LIST, projectSpecificExposureList
				.toArray( new String[projectSpecificExposureList.size()] ) );
		builder.add( USE_REGEXP, useRegexp );
		return builder.finish();
	}

	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( SensitiveInformationConfigDialog.class );
		dialog.setOptions( SensitiveInformationConfigDialog.INFOLIST, projectSpecificExposureList.toArray() );
		dialog.setBooleanValue( SensitiveInformationConfigDialog.USE_REGEXP, useRegexp );

		addListeners();
	}

	private void addListeners()
	{
		
		dialog.getFormField(  SensitiveInformationConfigDialog.USE_REGEXP ).addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if(sourceField instanceof JCheckBoxFormField  )
				save();
				
			}
		});
		
		((JStringListFormField)dialog.getFormField(  SensitiveInformationConfigDialog.INFOLIST )).getComponent().addPropertyChangeListener( new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				save();
			}

		});
	}

	// TODO : update help URL
	@AForm( description = "Configure Sensitive Information Exposure Assertion", name = "Sensitive Information Exposure Assertion", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface SensitiveInformationConfigDialog
	{

		@AField( description = "Sensitive Info to Check", name = "Sensitive Info to Check", type = AFieldType.STRINGLIST )
		public final static String INFOLIST = "Sensitive Info to Check";

		@AField( description = "check to use regular expressions", name = "Use regular expressions", type = AFieldType.BOOLEAN )
		public final static String USE_REGEXP = "Use regular expressions";

	}

	public void setConfiguration( XmlObject configuration )
	{
		config.set( configuration );
	}

	public XFormDialog getDialog()
	{
		return dialog;
	}
}
