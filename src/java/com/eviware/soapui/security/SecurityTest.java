/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestStepSecurityTestConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLog;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * SecurityTest
 * 
 * @author soapUI team
 */
public class SecurityTest extends AbstractWsdlModelItem<SecurityTestConfig> implements ModelItem, TestRunnable
{
	public final static String STARTUP_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@startupScript";
	public final static String TEARDOWN_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@tearDownScript";
	public final static String SECURITY_CHECK_MAP_PROPERTY = SecurityTest.class.getName() + "@securityCheckMap";
	private WsdlTestCase testCase;
	// private HashMap<String, List<SecurityCheck>> securityChecksMap;
	private SecurityTestLog securityTestLog;

	public SecurityTestLog getSecurityTestLog()
	{
		return securityTestLog;
	}

	private SecurityTestRunnerImpl runner;
	private SoapUIScriptEngine scriptEngine;

	public SecurityTest( WsdlTestCase testCase, SecurityTestConfig config )
	{
		super( config, testCase, "/loadTest.gif" );
		this.testCase = testCase;
		securityTestLog = new SecurityTestLog( this );
	}

	/**
	 * Adds new securityCheck for the specific TestStep
	 * 
	 * @param testStepName
	 * @param securityCheck
	 * 
	 * @return HashMap<TestStep, List<SecurityCheck>>
	 */
	public void addSecurityCheck( String testStepName, SecurityCheck securityCheck )
	{
		boolean hasChecks = false;
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList )
			{
				if( testStepSecurityTest.getTestStepName().equals( testStepName ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					securityCheckList.add( securityCheck.getConfig() );
					hasChecks = true;
				}
			}
		}
		if( !hasChecks )
		{
			TestStepSecurityTestConfig testStepSecurityTest = getConfig().addNewTestStepSecurityTest();
			testStepSecurityTest.setTestStepName( testStepName );
			SecurityCheckConfig newSecurityCheck = testStepSecurityTest.addNewTestStepSecurityCheck();
			newSecurityCheck.setConfig( securityCheck.getConfig() );
			newSecurityCheck.setType( securityCheck.getType() );
		}

	}

	/**
	 * Remove securityCheck for the specific TestStep
	 * 
	 * @param testStepName
	 * @param securityCheck
	 * 
	 * @return HashMap<TestStep, List<SecurityCheck>>
	 */
	public void removeSecurityCheck( String testStepName, SecurityCheck securityCheck )
	{
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList )
			{
				if( testStepSecurityTest.getTestStepName().equals( testStepName ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					securityCheckList.remove( securityCheck.getConfig() );
					if( securityCheckList.isEmpty() )
					{
						testStepSecurityTestList.remove( testStepSecurityTest );
					}
				}
			}
		}

	}

	public HashMap<String, List<SecurityCheck>> getSecurityChecksMap()
	{
		HashMap<String, List<SecurityCheck>> securityChecksMap = new HashMap<String, List<SecurityCheck>>();
		if( getConfig() != null )
		{
			if( !getConfig().getTestStepSecurityTestList().isEmpty() )
			{
				for( TestStepSecurityTestConfig testStepSecurityTestList : getConfig().getTestStepSecurityTestList() )
				{
					List<SecurityCheck> checkList = new ArrayList<SecurityCheck>();
					if( testStepSecurityTestList != null )
					{
						if( !testStepSecurityTestList.getTestStepSecurityCheckList().isEmpty() )
						{
							for( SecurityCheckConfig secCheckConfig : testStepSecurityTestList.getTestStepSecurityCheckList() )
							{
								SecurityCheck securityCheck = SecurityCheckRegistry.getInstance().getFactory(
										secCheckConfig.getType() ).buildSecurityCheck( secCheckConfig );
								checkList.add( securityCheck );
							}
						}
					}
					securityChecksMap.put( testStepSecurityTestList.getTestStepName(), checkList );
				}
			}
		}
		return securityChecksMap;
	}

	public WsdlTestCase getTestCase()
	{
		return testCase;
	}

	public SecurityTestRunner run( StringToObjectMap context, boolean async )
	{
		if( runner != null && runner.getStatus() == Status.RUNNING )
			return null;

		if( runner != null )
			runner.release();

		runner = new SecurityTestRunnerImpl( this );
		runner.start();
		return runner;
	}

	public void setStartupScript( String script )
	{
		String oldScript = getStartupScript();

		if( !getConfig().isSetStartupScript() )
			getConfig().addNewStartupScript();

		getConfig().getStartupScript().setStringValue( script );
		if( scriptEngine != null )
			scriptEngine.setScript( script );

		notifyPropertyChanged( STARTUP_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getStartupScript()
	{
		return getConfig() != null ? ( getConfig().isSetStartupScript() ? getConfig().getStartupScript().getStringValue()
				: "" ) : "";
	}

	public Object runStartupScript( SecurityTestRunContext runContext, SecurityTestRunner runner ) throws Exception
	{
		String script = getStartupScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( scriptEngine == null )
		{
			scriptEngine = SoapUIScriptEngineRegistry.create( this );
			scriptEngine.setScript( script );
		}

		scriptEngine.setVariable( "context", runContext );
		scriptEngine.setVariable( "securityTestRunner", runner );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return scriptEngine.run();
	}

	public void setTearDownScript( String script )
	{
		String oldScript = getTearDownScript();

		if( !getConfig().isSetTearDownScript() )
			getConfig().addNewTearDownScript();

		getConfig().getTearDownScript().setStringValue( script );
		if( scriptEngine != null )
			scriptEngine.setScript( script );

		notifyPropertyChanged( TEARDOWN_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getTearDownScript()
	{
		return getConfig() != null ? ( getConfig().isSetTearDownScript() ? getConfig().getTearDownScript()
				.getStringValue() : "" ) : "";
	}

	public Object runTearDownScript( SecurityTestRunContext runContext, SecurityTestRunner runner ) throws Exception
	{
		String script = getTearDownScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( scriptEngine == null )
		{
			scriptEngine = SoapUIScriptEngineRegistry.create( this );
			scriptEngine.setScript( script );
		}

		scriptEngine.setVariable( "context", runContext );
		scriptEngine.setVariable( "securityTestRunner", runner );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return scriptEngine.run();
	}

}
