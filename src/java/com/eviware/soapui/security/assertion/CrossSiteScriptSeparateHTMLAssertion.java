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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class CrossSiteScriptSeparateHTMLAssertion extends WsdlMessageAssertion implements ResponseAssertion
{
	public static final String ID = "CrosSiteScriptSeparateHTML";
	public static final String LABEL = "Cross Site Scripting Separate HTML";
	public static final String GROOVY_SCRIPT = "groovyScript";

	private XFormDialog dialog;
	private String script;
	private GroovyEditorModel groovyEditorModel;
	private SoapUIScriptEngine scriptEngine;

	MessageExchange messageExchange;
	SubmitContext context;

	public CrossSiteScriptSeparateHTMLAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, false, true, false, true );
		groovyEditorModel = new GroovyEditorModel( this );
		init();
		scriptEngine = SoapUIScriptEngineRegistry.create( this );
	}

	private void init()
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		script = reader.readString( GROOVY_SCRIPT, "" );
		groovyEditorModel.setScript( script );
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		TestStep testStep = ( TestStep )context.getProperty( ParameterExposureCheck.TEST_STEP );
		testStep = SecurityTestRunnerImpl.cloneTestStepForSecurityCheck( ( WsdlTestStep )testStep );
		SecurityTestRunner securityTestRunner = ( SecurityTestRunner )context
				.getProperty( ParameterExposureCheck.TEST_CASE_RUNNER );

		List<String> urls = submitScript( messageExchange, context );

		ParameterExposureCheckConfig parameterExposureCheckConfig = ( ParameterExposureCheckConfig )context
				.getProperty( ParameterExposureCheck.PARAMETER_EXPOSURE_CHECK_CONFIG );
		boolean throwException = false;
		List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
		for( String url : urls )
		{
			HttpTestRequestStep httpRequest = createHttpRequest( ( WsdlTestStep )testStep, url );
			MessageExchange messageExchange2 = ( MessageExchange )httpRequest.run( ( TestCaseRunner )securityTestRunner,
					( SecurityTestRunContext )context );

			for( String value : parameterExposureCheckConfig.getParameterExposureStringsList() )
			{
				value = context.expand( value );// property expansion support
				String match = SecurityCheckUtil.contains( context, new String( messageExchange2.getRawResponseData() ),
						value, false );
				if( match != null )
				{
					String shortValue = value.length() > 25 ? value.substring( 0, 22 ) + "... " : value;
					String message = "XSS content sent in request '" + shortValue + "' is exposed in response on link "
							+ url + " . Possibility for XSS script attack in: " + messageExchange.getModelItem().getName();
					assertionErrorList.add( new AssertionError( message ) );
					throwException = true;
				}
			}
		}
		if( throwException )
		{
			throw new AssertionException( assertionErrorList.toArray( new AssertionError[assertionErrorList.size()] ) );
		}

		return "OK";
	}

	private List<String> submitScript( MessageExchange messageExchange, SubmitContext context )
	{
		List<String> urls = new ArrayList<String>();
		scriptEngine.setScript( script );
		scriptEngine.setVariable( "urls", urls );
		scriptEngine.setVariable( "messageExchange", messageExchange );
		this.messageExchange = messageExchange;
		scriptEngine.setVariable( "context", context );
		this.context = context;
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

		try
		{
			Object result = scriptEngine.run();
			if( result instanceof List )
			{
				urls = ( List<String> )result;
			}
		}
		catch( Exception ex )
		{
			SoapUI.logError( ex );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
		return urls;
	}

	private HttpTestRequestStep createHttpRequest( WsdlTestStep testStep2, String url )
	{
		HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
		httpRequest.setEndpoint( HttpUtils.ensureEndpointStartsWithProtocol( url ) );
		httpRequest.setMethod( "GET" );

		TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
		testStepConfig.setType( HttpRequestStepFactory.HTTPREQUEST_TYPE );
		testStepConfig.setConfig( httpRequest );
		testStepConfig.setName( "Separate Request" );

		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory(
				( HttpRequestStepFactory.HTTPREQUEST_TYPE ) );
		return ( HttpTestRequestStep )factory.buildTestStep( ( WsdlTestCase )testStep2.getTestCase(), testStepConfig,
				false );

	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( CrossSiteScriptSeparateHTMLAssertion.ID, CrossSiteScriptSeparateHTMLAssertion.LABEL,
					CrossSiteScriptSeparateHTMLAssertion.class, ParameterExposureCheck.class );

		}

		@Override
		public Class<? extends WsdlMessageAssertion> getAssertionClassType()
		{
			return CrossSiteScriptSeparateHTMLAssertion.class;
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
		builder.add( GROOVY_SCRIPT, script );
		return builder.finish();
	}

	public boolean configure()
	{
		if( dialog == null )
			buildDialog();

		dialog.show();
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			setConfiguration( createConfiguration() );
		}
		return true;
	}

	private class GroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{
				public void actionPerformed( ActionEvent e )
				{
					Object result = null;
					List<String> urls = new ArrayList<String>();
					scriptEngine.setScript( script );
					scriptEngine.setVariable( "urls", urls );
					scriptEngine.setVariable( "messageExchange", messageExchange );
					scriptEngine.setVariable( "context", context );
					scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

					try
					{
						result = scriptEngine.run();
						if( result instanceof List )
						{
							urls = ( List<String> )result;
						}
						String generatedUrls = "";
						for( String url : urls )
						{
							generatedUrls += "\n" + url;
						}
						UISupport.showInfoMessage( "Generated urls :" + generatedUrls + " \n\nScript result"
								+ ( ( result == null ) ? "" : ": " + result + "" ) );
					}
					catch( Exception ex )
					{
						SoapUI.logError( ex );
					}
					finally
					{
						scriptEngine.clearVariables();
					}
				}
			};
		}

		public GroovyEditorModel( ModelItem modelItem )
		{
			super( new String[] { "urls", "log", "context", "messageExchange" }, modelItem, "" );
		}

		public String getScript()
		{
			return script;
		}

		public void setScript( String text )
		{
			script = text;
		}
	}

	protected GroovyEditorComponent buildGroovyPanel()
	{
		return new GroovyEditorComponent( groovyEditorModel, null );
	}

	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( CrossSiteScripSeparateHTMLConfigDialog.class );
		dialog.setSize( 600, 600);
		dialog.getFormField( CrossSiteScripSeparateHTMLConfigDialog.GROOVY )
				.setProperty( "component", new JScrollPane( buildGroovyPanel()) );
		dialog.getFormField( CrossSiteScripSeparateHTMLConfigDialog.GROOVY )
		.setProperty( "dimension",new Dimension( 450, 450 )  );
	}

	// TODO : update help URL
	@AForm( description = "", name = "Cross Site Scripting on Separate HTML", helpUrl = HelpUrls.HELP_URL_ROOT )
	protected interface CrossSiteScripSeparateHTMLConfigDialog
	{
		@AField( description = "", name = "Custom script that returns list of urls to check for XSS", type = AFieldType.LABEL )
		public final static String LABEL = "Custom script that returns list of urls to check for XSS";

		@AField( description = "Groovy script", name = "###Groovy url list", type = AFieldType.COMPONENT )
		public final static String GROOVY = "###Groovy url list";
	}
}
