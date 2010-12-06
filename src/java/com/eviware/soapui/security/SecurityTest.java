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
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.panels.SecurityChecksPanel;
import com.eviware.soapui.security.registry.AbstractSecurityCheckFactory;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * This class is used to connect a TestCase with a set of security checks
 * 
 * @author soapUI team
 */
public class SecurityTest extends AbstractTestPropertyHolderWsdlModelItem<SecurityTestConfig> implements TestModelItem,
		TestRunnable
{
	public final static String STARTUP_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@startupScript";
	public final static String TEARDOWN_SCRIPT_PROPERTY = SecurityTest.class.getName() + "@tearDownScript";
//	public final static String SECURITY_CHECK_MAP_PROPERTY = SecurityTest.class.getName() + "@securityCheckMap";
	private WsdlTestCase testCase;
	private SecurityTestLogModel securityTestLog;
	private SecurityChecksPanel.SecurityCheckListModel listModel;

	public void setListModel( SecurityChecksPanel.SecurityCheckListModel listModel )
	{
		this.listModel = listModel;
	}

	/**
	 * Gets the current security log
	 * 
	 * @return
	 */
	public SecurityTestLogModel getSecurityTestLog()
	{
		return securityTestLog;
	}

	private SecurityTestRunnerImpl runner;
	private SoapUIScriptEngine scriptEngine;

	public SecurityTest( WsdlTestCase testCase, SecurityTestConfig config )
	{
		super( config, testCase, "/loadTest.gif" );
		this.testCase = testCase;
		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( getConfig().getProperties() );

		securityTestLog = new SecurityTestLogModel();
	}

	/**
	 * Adds new securityCheck for the specific TestStep
	 * 
	 * @param testStep
	 * @param securityCheckType
	 * @param securityCheckName
	 * @return SecurityCheck
	 */
	public SecurityCheck addSecurityCheck( TestStep testStep, String securityCheckType, String securityCheckName )
	{
		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( securityCheckType );
		SecurityCheckConfig newSecCheckConfig = factory.createNewSecurityCheck( securityCheckName );
		SecurityCheck newSecCheck = factory.buildSecurityCheck( newSecCheckConfig );

		boolean hasChecks = false;
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList )
			{
				if( testStepSecurityTest.getTestStepId().equals( testStep.getId() ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					securityCheckList.add( newSecCheckConfig );
					hasChecks = true;
				}
			}
		}
		if( !hasChecks )
		{
			TestStepSecurityTestConfig testStepSecurityTest = getConfig().addNewTestStepSecurityTest();
			testStepSecurityTest.setTestStepId( testStep.getId() );
			SecurityCheckConfig newSecurityCheck = testStepSecurityTest.addNewTestStepSecurityCheck();
			newSecurityCheck.setConfig( newSecCheckConfig.getConfig() );
			newSecurityCheck.setType( newSecCheck.getType() );
			newSecurityCheck.setName( newSecCheck.getName() );
		}
		listModel.securityCheckAdded( testStep, newSecCheck );
		return newSecCheck;

	}

	/**
	 * Remove securityCheck for the specific TestStep
	 * 
	 * @param testStep
	 * @param securityCheck
	 * 
	 */
	public void removeSecurityCheck( TestStep testStep, SecurityCheck securityCheck )
	{
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList )
			{
				if( testStepSecurityTest.getTestStepId().equals( testStep.getId() ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					securityCheckList.remove( securityCheck.getConfig() );
					if( securityCheckList.isEmpty() )
					{
						testStepSecurityTestList.remove( testStepSecurityTest );
						return;
					}
					listModel.securityCheckRemoved( testStep, securityCheck );
				}
			}
		}

	}

	/**
	 * Returns a map of testids to security checks
	 * 
	 * @return A map of TestStepIds to their relevant security checks
	 */
	public HashMap<String, List<SecurityCheck>> getSecurityChecksMap()
	{
		HashMap<String, List<SecurityCheck>> securityChecksMap = new HashMap<String, List<SecurityCheck>>();
		if( getConfig() != null )
		{
			if( !getConfig().getTestStepSecurityTestList().isEmpty() )
			{
				for( TestStepSecurityTestConfig testStepSecurityTestListConfig : getConfig().getTestStepSecurityTestList() )
				{
					List<SecurityCheck> checkList = new ArrayList<SecurityCheck>();
					if( testStepSecurityTestListConfig != null )
					{
						if( !testStepSecurityTestListConfig.getTestStepSecurityCheckList().isEmpty() )
						{
							for( SecurityCheckConfig secCheckConfig : testStepSecurityTestListConfig
									.getTestStepSecurityCheckList() )
							{
								SecurityCheck securityCheck = SecurityCheckRegistry.getInstance().getFactory(
										secCheckConfig.getType() ).buildSecurityCheck( secCheckConfig );
								checkList.add( securityCheck );
							}
						}
					}
					securityChecksMap.put( testStepSecurityTestListConfig.getTestStepId(), checkList );
				}
			}
		}
		return securityChecksMap;
	}

	/**
	 * @return the current testcase
	 */
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

	/**
	 * Sets the script to be used on startup
	 * 
	 * @param script
	 */
	public void setStartupScript( String script )
	{
		String oldScript = getStartupScript();

		if( !getConfig().isSetSetupScript() )
			getConfig().addNewSetupScript();

		getConfig().getSetupScript().setStringValue( script );
		if( scriptEngine != null )
			scriptEngine.setScript( script );

		notifyPropertyChanged( STARTUP_SCRIPT_PROPERTY, oldScript, script );
	}

	/**
	 * @return The current startup script
	 */
	public String getStartupScript()
	{
		return getConfig() != null ? ( getConfig().isSetSetupScript() ? getConfig().getSetupScript().getStringValue()
				: "" ) : "";
	}

	/**
	 * Executes the startup Script
	 * 
	 * @param runContext
	 * @param runner
	 * @return
	 * @throws Exception
	 */
	public Object runStartupScript( WsdlTestRunContext runContext, SecurityTestRunner runner ) throws Exception
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

	/**
	 * Sets the script to be used on teardown
	 * 
	 * @param script
	 */
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

	/**
	 * @return The current teardown script
	 */
	public String getTearDownScript()
	{
		return getConfig() != null ? ( getConfig().isSetTearDownScript() ? getConfig().getTearDownScript()
				.getStringValue() : "" ) : "";
	}

	/**
	 * Executes the teardown Script
	 * 
	 * @param runContext
	 * @param runner
	 * @return
	 * @throws Exception
	 */
	public Object runTearDownScript( WsdlTestRunContext runContext, SecurityTestRunner runner ) throws Exception
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

	public List<SecurityCheck> getTestStepSecurityChecks( String testStepId )
	{
		return getSecurityChecksMap().get( testStepId ) != null ? getSecurityChecksMap().get( testStepId )
				: new ArrayList<SecurityCheck>();
	}

	public SecurityCheck getTestStepSecurityCheckByName( String testStepId, String securityCheckName )
	{
		List<SecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
		for( int c = 0; c < securityChecksList.size(); c++ )
		{
			SecurityCheck securityCheck = getTestStepSecurityCheckAt( testStepId, c );
			if( securityCheckName.equals( securityCheck.getName() ) )
				return securityCheck;
		}

		return null;
	}

	public SecurityCheck getTestStepSecurityCheckAt( String testStepId, int index )
	{
		List<SecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
		return securityChecksList.get( index );
	}

	public int getTestStepSecurityChecksCount( String testStepId )
	{
		if( getSecurityChecksMap().isEmpty() )
		{
			return 0;
		}
		else
		{
			return getSecurityChecksMap().get( testStepId ).size();
		}
	}

	public String findTestStepCheckUniqueName( String testStepId, String type )
	{
		String name = type;
		int numNames = 0;
		List<SecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
		if( securityChecksList != null && !securityChecksList.isEmpty() )
		{
			for( SecurityCheck existingCheck : securityChecksList )
			{
				if( existingCheck.getType().equals( name ) )
					numNames++ ;
			}
		}
		if( numNames != 0 )
		{
			name += " " + numNames;
		}
		return name;
	}

}
