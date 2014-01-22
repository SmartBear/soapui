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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.GroovyUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.log4j.Logger;

import javax.swing.*;

import static com.eviware.soapui.model.iface.Script.*;

/**
 * TestStep that executes an arbitrary Groovy script
 * 
 * @author ole.matzura
 */

public class WsdlGroovyScriptTestStep extends WsdlTestStepWithProperties implements PropertyExpansionContainer
{
	private final static Logger logger = Logger.getLogger( "groovy.log" );
	private String scriptText = "";
	private Object scriptResult;
	private ImageIcon failedIcon;
	private ImageIcon okIcon;
	private SoapUIScriptEngine scriptEngine;

	public WsdlGroovyScriptTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( !forLoadTest )
		{
			okIcon = UISupport.createImageIcon( "/groovy_script.gif" );
			setIcon( okIcon );
			failedIcon = UISupport.createImageIcon( "/groovy_script_failed.gif" );
		}

		if( config.getConfig() == null )
		{
			if( !forLoadTest )
				saveScript( config );
		}
		else
		{
			readConfig( config );
		}

        addProperty( new DefaultTestStepProperty( RESULT_PROPERTY, true, new DefaultTestStepProperty.PropertyHandlerAdapter()
		{

			public String getValue( DefaultTestStepProperty property )
			{
				return scriptResult == null ? null : scriptResult.toString();
			}
		}, this ) );

		addProperty( new TestStepBeanProperty( SCRIPT_PROPERTY, false, this, SCRIPT_PROPERTY, this ) );

		scriptEngine = SoapUIScriptEngineRegistry.create( this );
		scriptEngine.setScript( getScript() );
		if( forLoadTest && !isDisabled() )
			try
			{
				scriptEngine.compile();
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
	}

	public Logger getLogger()
	{
		SoapUI.ensureGroovyLog();
		return logger;
	}

	private void readConfig( TestStepConfig config )
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config.getConfig() );
		scriptText = reader.readString( SCRIPT_PROPERTY, "" );
	}

	private void saveScript( TestStepConfig config )
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( SCRIPT_PROPERTY, scriptText );
		config.setConfig( builder.finish() );
	}

	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );
		readConfig( config );
	}

	public String getDefaultSourcePropertyName()
	{
		return RESULT_PROPERTY;
	}

	public TestStepResult run( TestCaseRunner testRunner, TestCaseRunContext context )
	{
		SoapUI.ensureGroovyLog();

		WsdlTestStepResult result = new WsdlTestStepResult( this );
		Logger log = ( Logger )context.getProperty( "log" );
		if( log == null )
			log = logger;

		try
		{
			if( scriptText.trim().length() > 0 )
				synchronized( this )
				{
					scriptEngine.setVariable( "context", context );
					scriptEngine.setVariable( "testRunner", testRunner );
					scriptEngine.setVariable( "log", log );

					result.setTimeStamp( System.currentTimeMillis() );
					result.startTimer();
					scriptResult = scriptEngine.run();
					result.stopTimer();

					if( scriptResult != null )
					{
						result.addMessage( "Script-result: " + scriptResult.toString() );
						// FIXME The property should not me hard coded
						firePropertyValueChanged( RESULT_PROPERTY, null, String.valueOf( result ) );
					}

				}

			// testRunner status may have been changed by script..
			Status testRunnerStatus = testRunner.getStatus();
			if( testRunnerStatus == Status.FAILED )
				result.setStatus( TestStepStatus.FAILED );
			else if( testRunnerStatus == Status.CANCELED )
				result.setStatus( TestStepStatus.CANCELED );
			else
				result.setStatus( TestStepStatus.OK );
		}
		catch( Throwable e )
		{
			String errorLineNumber = GroovyUtils.extractErrorLineNumber( e );

			SoapUI.logError( e );
			result.stopTimer();
			result.addMessage( e.toString() );
			if( errorLineNumber != null )
				result.addMessage( "error at line: " + errorLineNumber );
			result.setError( e );
			result.setStatus( TestStepStatus.FAILED );
		}
		finally
		{
			if( !isForLoadTest() )
				setIcon( result.getStatus() == TestStepStatus.FAILED ? failedIcon : okIcon );

			if( scriptEngine != null )
				scriptEngine.clearVariables();
		}

		return result;
	}

	public String getScript()
	{
		return scriptText;
	}

	public void setScript( String scriptText )
	{
		if( scriptText.equals( this.scriptText ) )
			return;

		String oldScript = this.scriptText;
		this.scriptText = scriptText;
		scriptEngine.setScript( scriptText );
		saveScript( getConfig() );

		notifyPropertyChanged( SCRIPT_PROPERTY, oldScript, scriptText );
	}

	@Override
	public void release()
	{
		super.release();
		scriptEngine.release();
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		PropertyExpansionsResult result = new PropertyExpansionsResult( this );

		result.extractAndAddAll( SCRIPT_PROPERTY );

		return result.toArray();
	}
}
