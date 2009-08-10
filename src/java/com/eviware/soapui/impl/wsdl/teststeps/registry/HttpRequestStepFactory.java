/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.validators.RequiredValidator;

/**
 * Factory for WsdlTestRequestSteps
 * 
 * @author Ole.Matzura
 */

public class HttpRequestStepFactory extends WsdlTestStepFactory
{
	public static final String HTTPREQUEST_TYPE = "httprequest";
	private XFormDialog dialog;
	public static final MessageSupport messages = MessageSupport.getMessages( HttpRequestStepFactory.class );
	private XmlBeansRestParamsTestPropertyHolder params;
	private RestParamsTable paramsTable;

	public HttpRequestStepFactory()
	{
		super( HTTPREQUEST_TYPE, "HTTP Test Request", "Submits a HTTP Request and validates its response",
				"/http_request.gif" );
	}

	public WsdlTestStep buildTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		return new HttpTestRequestStep( testCase, config, forLoadTest );
	}

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name )
	{
		if( dialog == null )
		{
			buildDialog();
		}
		else
		{
			dialog.setValue( Form.ENDPOINT, "" );
		}

		params = new XmlBeansRestParamsTestPropertyHolder( testCase, RestParametersConfig.Factory.newInstance() );

		paramsTable = new RestParamsTable( params, false );
		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );
		dialog.setValue( Form.STEPNAME, name );

		try
		{
			if( dialog.show() )
			{
				HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
				httpRequest.setEndpoint( dialog.getValue( Form.ENDPOINT ) );
				httpRequest.setMethod( dialog.getValue( Form.HTTPMETHOD ) );
				new XmlBeansRestParamsTestPropertyHolder( testCase, httpRequest.addNewParameters() ).addParameters( params );

				TestStepConfig testStep = TestStepConfig.Factory.newInstance();
				testStep.setType( HTTPREQUEST_TYPE );
				testStep.setConfig( httpRequest );
				testStep.setName( dialog.getValue( Form.STEPNAME ) );

				return testStep;
			}
			else
			{
				return null;
			}
		}
		finally
		{
			paramsTable.release();
			paramsTable = null;
			params = null;
			dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );
		}
	}

	public boolean canCreate()
	{
		return true;
	}

	private void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( Form.class );
		dialog.getFormField( Form.STEPNAME ).addFormFieldValidator( new RequiredValidator() );
		dialog.getFormField( Form.EXTRACTPARAMS ).setProperty( "action", new ExtractParamsAction() );
		( ( XFormOptionsField )dialog.getFormField( Form.HTTPMETHOD ) ).setOptions( RestRequestInterface.RequestMethod
				.getMethods() );
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.TestStepName.Description", type = AField.AFieldType.STRING )
		public final static String STEPNAME = messages.get( "Form.TestStepName.Label" );

		@AField( description = "Form.Endpoint.Description", type = AField.AFieldType.STRING )
		public final static String ENDPOINT = messages.get( "Form.Endpoint.Label" );

		@AField( description = "Form.ExtractParams.Description", type = AField.AFieldType.ACTION )
		public final static String EXTRACTPARAMS = messages.get( "Form.ExtractParams.Label" );

		@AField( description = "Form.ParamsTable.Description", type = AField.AFieldType.COMPONENT )
		public final static String PARAMSTABLE = messages.get( "Form.ParamsTable.Label" );

		@AField( description = "Form.HttpMethod.Description", type = AField.AFieldType.ENUMERATION )
		public final static String HTTPMETHOD = messages.get( "Form.HttpMethod.Label" );
	}

	private class ExtractParamsAction extends AbstractAction
	{
		public ExtractParamsAction()
		{
			super( "Extract Params" );
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				String path = RestUtils.extractParams( dialog.getValue( Form.ENDPOINT ), params, true );
				dialog.setValue( Form.ENDPOINT, path );

				if( StringUtils.isNullOrEmpty( dialog.getValue( Form.STEPNAME ) ) )
				{
					setNameFromPath( path );
				}

				paramsTable.refresh();
			}
			catch( Exception e1 )
			{
				UISupport.showInfoMessage( "No parameters to extract!" );
			}
		}

		private void setNameFromPath( String path )
		{
			String[] items = path.split( "/" );

			if( items.length > 0 )
			{
				dialog.setValue( Form.STEPNAME, items[items.length - 1] );
			}
		}
	}

}