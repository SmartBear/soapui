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

import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageExchangeTestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.security.ui.GroovySecurityCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityCheck extends AbstractSecurityCheck implements HttpSecurityAnalyser
{
	public static final String SCRIPT_PROPERTY = GroovySecurityCheck.class.getName() + "@script";
	public static final String TYPE = "GroovySecurityCheck";
	private GroovyEditor executeEditor;
	private GroovyEditor analyzeEditor;
	private GroovySecurityCheckConfig groovyscc;
	private boolean next = true;
	private Object scriptResult;
	private TestProperty response;

	XFormDialog dialog;
	private static final String checkTitle = "Configure GroovyScript Check";

	public GroovySecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{

		super( testStep, config, parent, icon );
		if( config.getConfig() == null )
		{
			groovyscc = GroovySecurityCheckConfig.Factory.newInstance();
			groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
			groovyscc.getExecuteScript().setLanguage( "groovy" );
			groovyscc.getExecuteScript().setStringValue( "" );
			groovyscc.setAnalyzeScript( ScriptConfig.Factory.newInstance() );
			groovyscc.getAnalyzeScript().setLanguage( "groovy" );
			groovyscc.getAnalyzeScript().setStringValue( "" );
			config.setConfig( groovyscc );
		}
		else
		{
			groovyscc = ( GroovySecurityCheckConfig )config.getConfig();
			if( groovyscc.getExecuteScript() == null )
			{
				groovyscc.setExecuteScript( ScriptConfig.Factory.newInstance() );
				groovyscc.getExecuteScript().setLanguage( "groovy" );
				groovyscc.getExecuteScript().setStringValue( "" );
			}
			if( groovyscc.getAnalyzeScript() == null )
			{
				groovyscc.setAnalyzeScript( ScriptConfig.Factory.newInstance() );
				groovyscc.getAnalyzeScript().setLanguage( "groovy" );
				groovyscc.getAnalyzeScript().setStringValue( "" );
			}
		}

	}

	@Override
	protected SecurityCheckRequestResult execute( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult )
	{
		scriptEngine.setScript( getExecuteScript() );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		scriptEngine.setVariable( "context", context );
		scriptEngine.setVariable( "status", status );
		scriptEngine.setVariable( "executionStrategy", getExecutionStrategy() );

		try
		{
			scriptResult = scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}
		// TODO
		return null;
	}

	@Override
	protected boolean hasNext()
	{
		boolean result = next;
		next = !next;
		return result;
	}

	@Override
	protected void executeNew( TestStep testStep, SecurityTestRunContext context )
	{
		scriptEngine.setScript( groovyscc.getExecuteScript().getStringValue() );
		scriptEngine.setVariable( "request", this.testStep.getProperty( "Request" ).getValue() );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		try
		{
			scriptResult = scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			if( scriptResult != null )
			{
				this.testStep.getProperty( "Request" ).setValue( ( String )scriptResult );
				WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
						new StringToObjectMap() );
				this.testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
			}
			scriptEngine.clearVariables();
		}

	}

	@Override
	protected void analyzeNew( TestStep testStep, SecurityTestRunContext context )
	{
		scriptEngine.setScript( groovyscc.getAnalyzeScript().getStringValue() );
		scriptEngine.setVariable( "response", this.testStep.getProperty( "Response" ).getValue() );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		try
		{
			scriptResult = scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			if( scriptResult != null )
			{
				if( this.testStep instanceof WsdlTestRequestStep )
				{
					WsdlResponseMessageExchange m = new WsdlResponseMessageExchange(
							( ( WsdlTestRequestStep )testStep ).getTestRequest() );
					m.setMessages( new String[]{"CCC"} );
					securityCheckReqResult.setMessageExchange( m );
				}
				if ( Boolean.valueOf( scriptResult.toString() ) ) 
					securityCheckReqResult.setStatus( SecurityCheckStatus.OK );
				else 
					securityCheckReqResult.setStatus( SecurityCheckStatus.FAILED );
			}
			scriptEngine.clearVariables();
		}
	}

	public void setExecuteScript( String script )
	{
		String old = getExecuteScript();
		groovyscc.getExecuteScript().setStringValue( script );
		notifyPropertyChanged( SCRIPT_PROPERTY, old, script );
	}

	public String getExecuteScript()
	{
		return groovyscc.getExecuteScript().getStringValue();
	}

	@Override
	public SecurityCheckRequestResult analyze( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult )
	{
		// TODO
		return null;
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return true;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return new GroovySecurityCheckPanel( this );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection( MessageExchange messageExchange, JSecurityTestRunLog securityTestLog )
	{
		scriptEngine.setScript( getExecuteScript() );
		scriptEngine.setVariable( "testStep", null );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		scriptEngine.setVariable( "context", null );
		scriptEngine.setVariable( "messageExchange", messageExchange );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			scriptEngine.clearVariables();
		}

	}

	@Override
	public boolean canRun()
	{

		return true;
	}

	@Override
	public String getTitle()
	{
		return checkTitle;
	}

	@Override
	protected void buildDialog()
	{
		executeEditor = new GroovyEditor( new GroovySecurityCheckScriptModel()
		{

			@Override
			public String getScript()
			{
				return groovyscc.getExecuteScript().getStringValue();
			}

		} );
		analyzeEditor = new GroovyEditor( new GroovySecurityCheckScriptModel()
		{

			@Override
			public String getScript()
			{
				return groovyscc.getAnalyzeScript().getStringValue();
			}

		} );
		dialog = ADialogBuilder.buildDialog( GroovySecurityConfigDialog.class );
		dialog.getFormField( GroovySecurityConfigDialog.EXECUTE ).setProperty( "component", executeEditor );
		dialog.getFormField( GroovySecurityConfigDialog.ANALYZE ).setProperty( "component", analyzeEditor );
	}

	@Override
	public boolean configure()
	{
		if ( dialog == null )
			buildDialog();
		if( dialog.show() )
		{
			groovyscc.getExecuteScript().setStringValue( executeEditor.getEditArea().getText() );
			groovyscc.getAnalyzeScript().setStringValue( analyzeEditor.getEditArea().getText() );
		}

		return true;
	}

	@AForm( description = "Configure Groovy Types Check", name = "Invalid Groovy Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface GroovySecurityConfigDialog
	{

		@AField( description = "Modify request", name = "Modify Request", type = AFieldType.COMPONENT )
		public final static String EXECUTE = "Modify Request";

		@AField( description = "Verify Results", name = "Verify Results", type = AFieldType.COMPONENT )
		public final static String ANALYZE = "Verify Results";

	}

	private abstract class GroovySecurityCheckScriptModel extends AbstractGroovyEditorModel
	{
		public GroovySecurityCheckScriptModel()
		{
			super( new String[] { "log", "request" }, testStep.getModelItem(), "Execute Script" );
		}

		public Action getRunAction()
		{
			return null;
		}

		public void setScript( String text )
		{

		}
	}

}
