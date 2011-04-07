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
package com.eviware.soapui.security.assertion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.xmlbeans.XmlObject;
import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SensitiveInformationTableModel;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.security.SensitiveInformationPropertyHolder;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JStringListFormField;

public class SensitiveInfoExposureAssertion extends WsdlMessageAssertion implements ResponseAssertion
{
	private static final String PREFIX = "~";
	public static final String ID = "Sensitive Information Exposure";
	public static final String LABEL = "Sensitive Information Exposure";

	private List<String> assertionSpecificExposureList;

	private XFormDialog dialog;
	private static final String ASSERTION_SPECIFIC_EXPOSURE_LIST = "AssertionSpecificExposureList";
	private static final String INCLUDE_GLOBAL = "IncludeGlobal";
	private static final String INCLUDE_PROJECT_SPECIFIC = "IncludeProjectSpecific";
	private boolean includeGlolbal;
	private boolean includeProjectSpecific;
	private JPanel sensitiveInfoTableForm;
	private SensitiveInformationTableModel sensitivInformationTableModel;
	private JXTable tokenTable;

	public SensitiveInfoExposureAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, true, false, true );

		init();
	}

	private void init()
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		includeGlolbal = reader.readBoolean( INCLUDE_GLOBAL, true );
		includeProjectSpecific = reader.readBoolean( INCLUDE_PROJECT_SPECIFIC, true );
		assertionSpecificExposureList = StringUtils.toStringList( reader.readStrings( ASSERTION_SPECIFIC_EXPOSURE_LIST ) );
		extractTokenTable();
	}

	private void extractTokenTable()
	{
		SensitiveInformationPropertyHolder siph = new SensitiveInformationPropertyHolder();
		for( String str : assertionSpecificExposureList )
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
		siph.setPropertyValue( "name1", "value1" );
		siph.setPropertyValue( "name2", "value2" );
		sensitivInformationTableModel = new SensitiveInformationTableModel( siph );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		List<String> checkList = createCheckList( context );
		boolean throwException = false;
		List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
		for( String exposureContent : checkList )
		{
			boolean useRegexp = exposureContent.trim().startsWith( PREFIX );
			if( useRegexp )
			{
				exposureContent = exposureContent.substring( exposureContent.indexOf( PREFIX ) + 1 );
			}

			if( SecurityCheckUtil.contains( context, new String( messageExchange.getRawResponseData() ), exposureContent,
					useRegexp ) )
			{
				String message = "Sensitive information '" + exposureContent + "' is exposed in : "
						+ messageExchange.getModelItem().getName();
				assertionErrorList.add( new AssertionError( message ) );
				throwException = true;
			}
		}

		if( throwException )
		{
			throw new AssertionException( assertionErrorList.toArray( new AssertionError[assertionErrorList.size()] ) );
		}

		return "OK";
	}

	private List<String> createCheckList( SubmitContext context )
	{
		List<String> checkList = new ArrayList<String>( assertionSpecificExposureList );
		if( includeProjectSpecific )
		{
			checkList.addAll( SecurityCheckUtil.projectEntriesList( this ) );
		}

		if( includeGlolbal )
		{
			checkList.addAll( SecurityCheckUtil.globalEntriesList() );
		}
		List<String> expandedList = propertyExpansionSupport( checkList, context );
		return expandedList;
	}

	private List<String> propertyExpansionSupport( List<String> checkList, SubmitContext context )
	{
		List<String> expanded = new ArrayList<String>();
		for( String content : checkList )
		{
			expanded.add( context.expand( content ) );
		}
		return expanded;
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( SensitiveInfoExposureAssertion.ID, SensitiveInfoExposureAssertion.LABEL,
					SensitiveInfoExposureAssertion.class, AbstractSecurityCheck.class );

		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return SensitiveInfoExposureAssertion.class;
		}
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return null;
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( ASSERTION_SPECIFIC_EXPOSURE_LIST, assertionSpecificExposureList
				.toArray( new String[assertionSpecificExposureList.size()] ) );
		builder.add( INCLUDE_PROJECT_SPECIFIC, includeProjectSpecific );
		builder.add( INCLUDE_GLOBAL, includeGlolbal );
		return builder.finish();
	}

	@Override
	public boolean configure()
	{
		if( dialog == null )
			buildDialog();
		if( dialog.show() )
		{

			JStringListFormField jsringListFormField = ( JStringListFormField )dialog
					.getFormField( SensitiveInformationConfigDialog.TOKENS );

			String[] stringList = jsringListFormField != null ? jsringListFormField.getOptions() : new String[0];
			assertionSpecificExposureList = StringUtils.toStringList( stringList );
			includeProjectSpecific = Boolean.valueOf( dialog.getFormField(
					SensitiveInformationConfigDialog.INCLUDE_PROJECT_SPECIFIC ).getValue() );
			includeGlolbal = Boolean.valueOf( dialog.getFormField( SensitiveInformationConfigDialog.INCLUDE_GLOBAL )
					.getValue() );
			setConfiguration( createConfiguration() );

			return true;
		}
		return false;
	}

	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( SensitiveInformationConfigDialog.class );
		dialog.setBooleanValue( SensitiveInformationConfigDialog.INCLUDE_GLOBAL, includeGlolbal );
		dialog.setBooleanValue( SensitiveInformationConfigDialog.INCLUDE_PROJECT_SPECIFIC, includeProjectSpecific );
		dialog.getFormField( SensitiveInformationConfigDialog.TOKENS ).setProperty( "component", getForm() );
		// dialog.setOptions( SensitiveInformationConfigDialog.TOKENS,
		// assertionSpecificExposureList.toArray() );
	}

	// TODO : update help URL
	@AForm( description = "Configure Sensitive Information Exposure Assertion", name = "Sensitive Information Exposure Assertion", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface SensitiveInformationConfigDialog
	{

		@AField( description = "Sensitive informations to check. Use ~ as prefix for values that are regular expressions.", name = "Sensitive Information Tokens", type = AFieldType.COMPONENT )
		public final static String TOKENS = "Sensitive Information Tokens";

		@AField( description = "Include project specific sensitive information configuration", name = "Project Specific", type = AFieldType.BOOLEAN )
		public final static String INCLUDE_PROJECT_SPECIFIC = "Project Specific";

		@AField( description = "Include global sensitive information configuration", name = "Global Configuration", type = AFieldType.BOOLEAN )
		public final static String INCLUDE_GLOBAL = "Global Configuration";

	}

	public JPanel getForm()
	{
		if( sensitiveInfoTableForm == null )
		{
			sensitiveInfoTableForm = new JPanel( new BorderLayout() );

			JXToolBar toolbar = UISupport.createToolbar();

			toolbar.add( UISupport.createToolbarButton( new AddTokenAction() ) );
			toolbar.add( UISupport.createToolbarButton( new RemoveTokenAction() ) );

			tokenTable = new JXTable( sensitivInformationTableModel );
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
			putValue( Action.SHORT_DESCRIPTION, "Adds a parameter to assertion" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			String newToken = "newToken";
			newToken = UISupport.prompt( "Enter token name", "New Token", newToken );
			String newValue = "value";
			newValue = UISupport.prompt( "Enter value", "New Token Value", newValue );
			
			sensitivInformationTableModel.addToken(newToken, newValue);
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
			sensitivInformationTableModel.removeRows(tokenTable.getSelectedRows());
		}

	}
}
