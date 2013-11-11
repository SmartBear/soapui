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
package com.eviware.soapui.security.panels;

import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.SensitiveInformationConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.security.SensitiveInformationTableModel;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.SensitiveInformationPropertyHolder;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import org.apache.xmlbeans.XmlObject;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ProjectSensitiveInformationPanel
{

	private XFormDialog dialog;
	private SensitiveInformationConfig config;
	private List<String> projectSpecificExposureList;
	public static final String PROJECT_SPECIFIC_EXPOSURE_LIST = "ProjectSpecificExposureList";
	private SensitiveInformationTableModel sensitiveInformationTableModel;
	private JXTable tokenTable;
	private JPanel sensitiveInfoTableForm;

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
		projectSpecificExposureList = StringUtils.toStringList( reader.readStrings( PROJECT_SPECIFIC_EXPOSURE_LIST ) );
		extractTokenTable();
	}

	private void extractTokenTable()
	{
		SensitiveInformationPropertyHolder siph = new SensitiveInformationPropertyHolder();
		for( String str : projectSpecificExposureList )
		{
			String[] tokens = str.split( "###" );
			if( tokens.length == 2 )
			{
				siph.setPropertyValue( tokens[0], tokens[1] );
			}
			else
			{
				siph.setPropertyValue( tokens[0], "" );
			}
		}
		sensitiveInformationTableModel = new SensitiveInformationTableModel( siph );
	}

	public boolean build()
	{
		if( dialog == null )
			buildDialog();

		return false;
	}

	public void save()
	{
		projectSpecificExposureList = createListFromTable();
		setConfiguration( createConfiguration() );
	}

	private List<String> createListFromTable()
	{
		List<String> temp = new ArrayList<String>();
		for( TestProperty tp : sensitiveInformationTableModel.getHolder().getPropertyList() )
		{
			String tokenPlusDescription = tp.getName() + "###" + tp.getValue();
			temp.add( tokenPlusDescription );
		}
		return temp;
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( PROJECT_SPECIFIC_EXPOSURE_LIST,
				projectSpecificExposureList.toArray( new String[projectSpecificExposureList.size()] ) );
		return builder.finish();
	}

	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( SensitiveInformationConfigDialog.class );
		dialog.getFormField( SensitiveInformationConfigDialog.TOKENS ).setProperty( "component", getForm() );

	}

	// TODO : update help URL
	@AForm( description = "Configure Sensitive Information Exposure Assertion", name = "Sensitive Information Exposure Assertion", helpUrl = HelpUrls.SECURITY_SENSITIVE_INFORMATION_EXPOSURE_ASSERTION_HELP )
	protected interface SensitiveInformationConfigDialog
	{

		@AField( description = "Sensitive informations to check. Use ~ as prefix for values that are regular expressions.", name = "Sensitive Information Tokens", type = AFieldType.COMPONENT )
		public final static String TOKENS = "Sensitive Information Tokens";
	}

	public void setConfiguration( XmlObject configuration )
	{
		config.set( configuration );
	}

	public XFormDialog getDialog()
	{
		return dialog;
	}

	public JPanel getForm()
	{
		if( sensitiveInfoTableForm == null )
		{
			sensitiveInfoTableForm = new JPanel( new BorderLayout() );

			JXToolBar toolbar = UISupport.createToolbar();

			toolbar.add( UISupport.createToolbarButton( new AddTokenAction() ) );
			toolbar.add( UISupport.createToolbarButton( new RemoveTokenAction() ) );

			tokenTable = JTableFactory.getInstance().makeJXTable( sensitiveInformationTableModel );
			tokenTable.setPreferredSize( new Dimension( 200, 100 ) );
			sensitiveInfoTableForm.add( toolbar, BorderLayout.NORTH );
			sensitiveInfoTableForm.add( new JScrollPane( tokenTable ), BorderLayout.CENTER );
		}

		return sensitiveInfoTableForm;
	}

	class AddTokenAction extends AbstractAction
	{

		public AddTokenAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds a token to assertion" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			String newToken = "";
			newToken = UISupport.prompt( "Enter token", "New Token", newToken );
			String newValue = "";
			newValue = UISupport.prompt( "Enter description", "New Description", newValue );

			sensitiveInformationTableModel.addToken( newToken, newValue );
			save();
		}

	}

	class RemoveTokenAction extends AbstractAction
	{

		public RemoveTokenAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes token from assertion" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			sensitiveInformationTableModel.removeRows( tokenTable.getSelectedRows() );
			save();
		}
	}

	public void release()
	{
		if( dialog != null )
			dialog.release();
	}
}
