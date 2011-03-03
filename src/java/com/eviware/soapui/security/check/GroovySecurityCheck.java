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

import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.security.ui.GroovySecurityCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
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
	private GroovySecurityCheckConfig groovyscc;
	private boolean next = true;
	private Object scriptResult;
	// private TestProperty response;

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
	protected boolean hasNext(TestStep testStep,SecurityTestRunContext context)
	{
		boolean result = next;
		next = !next;
		return result;
	}

	@Override
	protected void execute(  SecurityTestRunner  securityTestRunner,TestStep testStep, SecurityTestRunContext context )
	{
////		getScriptEngine().setScript( groovyscc.getExecuteScript().getStringValue() );
////		getScriptEngine().setVariable( "request", getTestStep().getProperty( "Request" ).getValue() );
////		getScriptEngine().setVariable( "log", SoapUI.ensureGroovyLog() );
//		try
//		{
//			scriptResult = getScriptEngine().run();
//		}
//		catch( Exception e )
//		{
//			SoapUI.logError( e );
//		}
//		finally
//		{
//			if( scriptResult != null )
//			{
//				getTestStep().getProperty( "Request" ).setValue( ( String )scriptResult );
//
//				getTestStep().run( (TestCaseRunner)securityTestRunner, ( TestCaseRunContext )securityTestRunner.getRunContext() );
//			}
////			getScriptEngine().clearVariables();
//		}

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
//		getScriptEngine().setScript( getExecuteScript() );
//		getScriptEngine().setVariable( "testStep", null );
//		getScriptEngine().setVariable( "log", SoapUI.ensureGroovyLog() );
//		getScriptEngine().setVariable( "context", null );
//		getScriptEngine().setVariable( "messageExchange", messageExchange );
//
//		try
//		{
//			getScriptEngine().run();
//		}
//		catch( Exception e )
//		{
//			SoapUI.logError( e );
//		}
//		finally
//		{
//			getScriptEngine().clearVariables();
//		}

	}

	@Override
	public boolean canRun()
	{

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
			super( new String[] { "log", "request" }, getTestStep().getModelItem(), "Execute Script" );
		}

		public Action getRunAction()
		{
			return null;
		}

		public void setScript( String text )
		{

		}
	}
	
	@Override
	public String getConfigDescription()
	{
		return "Configuration for Groovy Security Check";
	}

	@Override
	public String getConfigName()
	{
		return "Configuration for Groovy Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

}
