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
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.boundary.EnumerationValuesExtractor;
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
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			CheckedParametersListConfig boundary = CheckedParametersListConfig.Factory.newInstance();
			config.setConfig( boundary );
		}
		else
		{
			List<String> selected = new ArrayList<String>();
			if( config.getConfig() == null )
			{
				config.addNewConfig();
			}
			else
			{
				config.getConfig().changeType( CheckedParametersListConfig.type );
				for( CheckedParameterConfig cpc : ( ( CheckedParametersListConfig )config.getConfig() ).getParametersList() )
				{
					if( cpc.getChecked() )
						selected.add( cpc.getParameterName() );
				}
				enumerationValuesExtractor.setSelectedEnumerationParameters( selected );
			}
		}
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

	
	/**
	 * not used any more
	 */
	protected void analyze( TestStep testStep, SecurityTestRunContext context )
	{
		if( testStep instanceof WsdlTestRequestStep )
		{
			int statusCode = ( ( WsdlTestRequestStep )testStep ).getTestRequest().getResponse().getStatusCode();
			if( statusCode == HttpStatus.SC_OK )
			{
				createMessageExchange( testStep );
				securityCheckRequestResult
						.addMessage( "Server is accepting invalid enumeration value and returns status code 200 (OK) !" );
				securityCheckRequestResult.setStatus( SecurityCheckStatus.FAILED );
			}
		}
		else if( false )
		{
			// here we need to add analyze for different types of request
		}
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

			CheckedParametersListConfig boundary = CheckedParametersListConfig.Factory.newInstance();
			CheckedParameterConfig[] checkedParametersArray = new CheckedParameterConfig[enumerationValuesExtractor
					.getEnumerationParameters().size()];
			int i = 0;
			for( String value : enumerationValuesExtractor.getEnumerationParameters() )
			{
				CheckedParameterConfig checkedParameter = CheckedParameterConfig.Factory.newInstance();
				checkedParameter.setParameterName( value );
				checkedParameter.setChecked( Arrays.asList( selectedList ).contains( value ) );
				checkedParametersArray[i++ ] = checkedParameter;
			}
			boundary.setParametersArray( checkedParametersArray );
			config.setConfig( boundary );
			return true;
		}
		return false;
	}

	@Override
	protected void buildDialog()
	{
		List<String> selectedOptions = new ArrayList<String>();
		config.getConfig().changeType( CheckedParametersListConfig.type );
		for( CheckedParameterConfig cpc : ( ( CheckedParametersListConfig )config.getConfig() ).getParametersList() )
		{
			if( cpc.getChecked() )
				selectedOptions.add( cpc.getParameterName() );
		}

		dialog = ADialogBuilder.buildDialog( BoundaryConfigDialog.class );
		XFormMultiSelectList field = ( XFormMultiSelectList )dialog.getFormField( BoundaryConfigDialog.PARAMETERS );
		field.setOptions( enumerationValuesExtractor.getEnumerationParameters().toArray(
				new String[enumerationValuesExtractor.getEnumerationParameters().size()] ) );
		field.setSelectedOptions( selectedOptions.toArray() );
		
		XFormField assertionsPanel = dialog.getFormField( BoundaryConfigDialog.ASSERTIONS );
		SecurityAssertionPanel securityAssertionPanel =  new SecurityAssertionPanel( this ); 
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