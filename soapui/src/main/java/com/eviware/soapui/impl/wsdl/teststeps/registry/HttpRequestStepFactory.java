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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.RestParameterConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.monitor.WsdlMonitorMessageExchange;
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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory for WsdlTestRequestSteps
 *
 * @author Ole.Matzura
 */

public class HttpRequestStepFactory extends WsdlTestStepFactory
{
	private static final String HTTPREQUEST = "httprequest";
	public static final String HTTPREQUEST_TYPE = HTTPREQUEST;
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

		paramsTable = new RestParamsTable( params, false, NewRestResourceActionBase.ParamLocation.RESOURCE, true, false );
		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );
		dialog.setValue( Form.STEPNAME, name );

		try
		{
			if( dialog.show() )
			{
				HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
				httpRequest.setEndpoint( HttpUtils.ensureEndpointStartsWithProtocol( dialog.getValue( Form.ENDPOINT ) ) );
				httpRequest.setMethod( dialog.getValue( Form.HTTPMETHOD ) );
				XmlBeansRestParamsTestPropertyHolder tempParams = new XmlBeansRestParamsTestPropertyHolder( testCase,
						httpRequest.addNewParameters() );
				tempParams.addParameters( params );
				tempParams.release();

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

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name, String endpoint, String method )
	{
		RestParametersConfig restParamConf = RestParametersConfig.Factory.newInstance();
		params = new XmlBeansRestParamsTestPropertyHolder( testCase, restParamConf );

		HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
		httpRequest.setMethod( method );

		String path = RestUtils.extractParams( endpoint, params, true );
		endpoint = path;

		XmlBeansRestParamsTestPropertyHolder tempParams = new XmlBeansRestParamsTestPropertyHolder( testCase,
				httpRequest.addNewParameters() );
		tempParams.addParameters( params );

		httpRequest.setEndpoint( HttpUtils.ensureEndpointStartsWithProtocol( endpoint ) );

		TestStepConfig testStep = TestStepConfig.Factory.newInstance();
		testStep.setType( HTTPREQUEST_TYPE );
		testStep.setConfig( httpRequest );
		testStep.setName( name );

		return testStep;
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

	public TestStepConfig createConfig( WsdlMonitorMessageExchange me, String stepName )
	{
		HttpRequestConfig testRequestConfig = HttpRequestConfig.Factory.newInstance();

		testRequestConfig.setName( stepName );
		testRequestConfig.setEncoding( "UTF-8" );
		testRequestConfig.setEndpoint( me.getEndpoint() );
		testRequestConfig.setMethod( me.getRequestMethod() );

		// set parameters
		RestParametersConfig parametersConfig = testRequestConfig.addNewParameters();
		Map<String, String> parametersMap = me.getHttpRequestParameters();
		List<RestParameterConfig> parameterConfigList = new ArrayList<RestParameterConfig>();
		for( String name : parametersMap.keySet() )
		{
			RestParameterConfig parameterConf = RestParameterConfig.Factory.newInstance();
			parameterConf.setName( name );
			parameterConf.setValue( parametersMap.get( name ) );
			parameterConfigList.add( parameterConf );
		}
		parametersConfig.setParameterArray( parameterConfigList.toArray( new RestParameterConfig[parametersMap.size()] ) );
		testRequestConfig.setParameters( parametersConfig );

		// String requestContent = me.getRequestContent();
		// testRequestConfig.addNewRequest().setStringValue( requestContent );

		TestStepConfig testStep = TestStepConfig.Factory.newInstance();
		testStep.setType( HTTPREQUEST );
		testStep.setConfig( testRequestConfig );
		testStep.setName( stepName );
		return testStep;
	}

}
