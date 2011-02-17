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
package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.boundary.EnumerationValuesExtractor;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;
import com.eviware.x.form.support.AField.AFieldType;

public class BoundarySecurityCheck extends AbstractSecurityCheck
{

	private boolean hasNext = true;
	private XFormDialog dialog;
	public static final String TYPE = "BoundaryCheck";
	public static final String LABEL = "Boundary";
	private EnumerationValuesExtractor enumerationValuesExtractor;

	public BoundarySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
		enumerationValuesExtractor = new EnumerationValuesExtractor( ( ( WsdlTestRequestStep )testStep ).getTestRequest() );
		List<String> selected = new ArrayList<String>();
		for( SecurityCheckedParameter cpc : parameterHolder.getParameterList() )
		{
			if( cpc.isChecked() )
				selected.add( cpc.getName() );
		}
		enumerationValuesExtractor.setSelectedEnumerationParameters( selected );
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{

		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	protected void execute( TestStep testStep, SecurityTestRunContext context )
	{
		if( acceptsTestStep( testStep ) )
		{
			try
			{
				updateRequestContent( testStep );
			}
			catch( Exception e )
			{
				SoapUI.log.error( "Error extracting enumeration values from message", e );
			}

			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
					new StringToObjectMap() );

			testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
			createMessageExchange( testStep );
			this.hasNext = false;

		}
	}

	private void createMessageExchange( TestStep testStep )
	{
		MessageExchange messageExchange = new WsdlResponseMessageExchange( ( ( WsdlTestRequestStep )testStep )
				.getTestRequest() );
		securityCheckRequestResult.setMessageExchange( messageExchange );
	}

	private void updateRequestContent( TestStep testStep ) throws XmlException, Exception
	{
		( ( WsdlTestRequestStep )testStep ).getTestRequest().setRequestContent( enumerationValuesExtractor.extract() );
	}

	protected boolean hasNext()
	{
		return this.hasNext;
	}

	@Override
	public boolean configure()
	{
		if( dialog == null )
			buildDialog();
		if( dialog.show() )
		{

			String[] selectedList = StringUtils.toStringArray( ( ( XFormMultiSelectList )dialog
					.getFormField( BoundaryConfigDialog.PARAMETERS ) ).getSelectedOptions() );
			enumerationValuesExtractor.setSelectedEnumerationParameters( Arrays.asList( selectedList ) );

			for( String paramName : enumerationValuesExtractor.getEnumerationParameters() )
			{
				SecurityCheckedParameterImpl param = ( SecurityCheckedParameterImpl )parameterHolder.getParametarByName(paramName);
				if ( param == null ) {
					param = ( SecurityCheckedParameterImpl )parameterHolder.addParameter( paramName );
					param.setName( paramName );
				}
				( ( SecurityCheckedParameterImpl )param ).setChecked( Arrays.asList( selectedList ).contains( paramName ) );
			}
			
			return true;
		}
		return false;
	}

	@Override
	protected void buildDialog()
	{
		List<String> selectedOptions = new ArrayList<String>();
		for( SecurityCheckedParameter cpc : parameterHolder.getParameterList() )
		{
			if( cpc.isChecked() )
				selectedOptions.add( cpc.getName() );
		}

		dialog = ADialogBuilder.buildDialog( BoundaryConfigDialog.class );
		XFormMultiSelectList field = ( XFormMultiSelectList )dialog.getFormField( BoundaryConfigDialog.PARAMETERS );
		field.setOptions( enumerationValuesExtractor.getEnumerationParameters().toArray(
				new String[enumerationValuesExtractor.getEnumerationParameters().size()] ) );
		field.setSelectedOptions( selectedOptions.toArray() );

		XFormField assertionsPanel = dialog.getFormField( BoundaryConfigDialog.ASSERTIONS );
		SecurityAssertionPanel securityAssertionPanel = new SecurityAssertionPanel( this );
		assertionsPanel.setProperty( "component", securityAssertionPanel );

	}

	@Override
	public boolean isConfigurable()
	{
		return true;
	}

	@AForm( description = "Configure Out of Boundary Check", name = "Configure Out of Boundary Check", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface BoundaryConfigDialog
	{

		@AField( description = "Parameters to Check", name = "Select parameters to check", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Select parameters to check";

		@AField( description = "Assertions", name = "Select assertions to apply", type = AFieldType.COMPONENT )
		public final static String ASSERTIONS = "Select assertions to apply";

	}

}