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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.StringUtils;
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
	public static final String ID = "Sensitive Information Exposure";
	public static final String LABEL = "Sensitive Information Exposure";

	private List<String> specificExposureList;
	private List<String> gloablExposureList;
	private List<String> checkList;
	private XFormDialog dialog;
	private static final String SPECIFIC_EXPOSURE_LIST = "SpecificExposureList";
	private static final String USE_REGEXP = "UseRegexp";
	private static final String INCLUDE_GLOBAL = "IncludeGlobal";
	private boolean useRegexp;
	private boolean includeGlolbal;

	public SensitiveInfoExposureAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, true, false, true );

		init();
	}

	private void init()
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		useRegexp = reader.readBoolean( USE_REGEXP, false );
		includeGlolbal = reader.readBoolean( INCLUDE_GLOBAL, true );
		specificExposureList = StringUtils.toStringList( reader.readStrings( SPECIFIC_EXPOSURE_LIST ) );
		gloablExposureList = SecurityCheckUtil.entriesList();
		checkList = createCheckList();
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		for( String exposureContent : checkList )
		{
			if( SecurityCheckUtil.contains( context, new String( messageExchange.getRawResponseData() ), exposureContent,
					useRegexp ) )
			{
				String message = "Sensitive information '" + exposureContent + "' is exposed in : "
						+ messageExchange.getModelItem().getName();
				throw new AssertionException( new AssertionError( message ) );
			}
		}

		return "OK";
	}

	private List<String> createCheckList()
	{
		List<String> checkList = new ArrayList<String>( specificExposureList );
		if( includeGlolbal )
		{
			checkList.addAll( gloablExposureList );
		}
		return checkList;
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
		builder.add( SPECIFIC_EXPOSURE_LIST, specificExposureList.toArray( new String[specificExposureList.size()] ) );
		builder.add( USE_REGEXP, useRegexp );
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
					.getFormField( SensitiveInformationConfigDialog.INFOLIST );

			String[] stringList = jsringListFormField.getOptions();
			specificExposureList = StringUtils.toStringList( stringList );
			includeGlolbal = Boolean.valueOf( dialog.getFormField( SensitiveInformationConfigDialog.INCLUDE_GLOBAL )
					.getValue() );
			useRegexp = Boolean.valueOf( dialog.getFormField( SensitiveInformationConfigDialog.USE_REGEXP ).getValue() );
			checkList = createCheckList();
			setConfiguration( createConfiguration() );

			return true;
		}
		return false;
	}

	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( SensitiveInformationConfigDialog.class );
		dialog.setBooleanValue( SensitiveInformationConfigDialog.INCLUDE_GLOBAL, includeGlolbal );
		dialog.setOptions( SensitiveInformationConfigDialog.INFOLIST, specificExposureList.toArray() );
		dialog.setBooleanValue( SensitiveInformationConfigDialog.USE_REGEXP, useRegexp );
	}

	// TODO : update help URL
	@AForm( description = "Configure Sensitive Information Exposure Assertion", name = "Sensitive Information Exposure Assertion", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface SensitiveInformationConfigDialog
	{

		@AField( description = "Sensitive Info to Check", name = "Sensitive Info to Check", type = AFieldType.STRINGLIST )
		public final static String INFOLIST = "Sensitive Info to Check";

		@AField( description = "Include Global Sensitive Information Configuration", name = "Include Global Configuration", type = AFieldType.BOOLEAN )
		public final static String INCLUDE_GLOBAL = "Include Global Configuration";

		@AField( description = "check to use regular expressions", name = "Use regular expressions", type = AFieldType.BOOLEAN )
		public final static String USE_REGEXP = "Use regular expressions";

	}

}
