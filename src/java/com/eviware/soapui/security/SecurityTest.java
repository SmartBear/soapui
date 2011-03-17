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

package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestStepSecurityTestConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.panels.SecurityChecksPanel;
import com.eviware.soapui.security.registry.AbstractSecurityCheckFactory;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.security.support.SecurityTestRunListener;
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
	public final static String FAIL_ON_CHECKS_ERRORS_PROPERTY = SecurityTest.class.getName() + "@failOnChecksErrors";
	public final static String FAIL_ON_ERROR_PROPERTY = SecurityTest.class.getName() + "@failOnError";
	private WsdlTestCase testCase;
	private SecurityTestLogModel securityTestLog;
	private SecurityChecksPanel.SecurityCheckListModel listModel;
	private Set<SecurityTestRunListener> securityTestRunListeners = new HashSet<SecurityTestRunListener>();
	private Map<TestStep, Set<SecurityTestRunListener>> securityTestStepRunListeners = new HashMap<TestStep, Set<SecurityTestRunListener>>();
	private Map<TestStep, SecurityTestStepResult> securityTestStepResultMap;

	private HashMap<String, List<AbstractSecurityCheck>> securityChecksMap = new HashMap<String, List<AbstractSecurityCheck>>();

	private SecurityTestRunnerImpl runner;
	private SoapUIScriptEngine scriptEngine;

	public SecurityTest( WsdlTestCase testCase, SecurityTestConfig config )
	{
		super( config, testCase, "/securityTest.png" );
		this.testCase = testCase;
		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( getConfig().getProperties() );

		securityTestLog = new SecurityTestLogModel();
		securityTestStepResultMap = new HashMap<TestStep, SecurityTestStepResult>();

		for( SecurityTestRunListener listener : SoapUI.getListenerRegistry().getListeners( SecurityTestRunListener.class ) )
		{
			addSecurityTestRunListener( listener );
		}
	}

	public void release()
	{
		super.release();

		securityTestRunListeners.clear();
	}

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

	/**
	 * Adds new securityCheck for the specific TestStep
	 * 
	 * @param testStep
	 * @param securityCheckType
	 * @param securityCheckName
	 * @param securityCheckConfig
	 * @return AbstractSecurityCheck
	 */
	public SecurityCheck addSecurityCheck( TestStep testStep, String securityCheckType, String securityCheckName )
	{
		AbstractSecurityCheckFactory factory = SoapUI.getSoapUICore().getSecurityCheckRegistry()
				.getFactory( securityCheckType );
		SecurityCheckConfig newSecCheckConfig = factory.createNewSecurityCheck( securityCheckName );
		AbstractSecurityCheck newSecCheck = null;
		// newSecCheck.setTestStep( testStep );

		boolean hasChecks = false;
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( int i = 0; i < testStepSecurityTestList.size(); i++ )
			{
				TestStepSecurityTestConfig testStepSecurityTest = testStepSecurityTestList.get( i );
				if( testStepSecurityTest.getTestStepId().equals( testStep.getId() ) )
				{
					newSecCheck = addSecurityCheck( factory, newSecCheckConfig, testStepSecurityTest, testStep );
					hasChecks = true;
					break;
				}
			}
		}
		if( !hasChecks )
		{
			TestStepSecurityTestConfig testStepSecurityTest = getConfig().addNewTestStepSecurityTest();
			testStepSecurityTest.setTestStepId( testStep.getId() );

			newSecCheck = addSecurityCheck( factory, newSecCheckConfig, testStepSecurityTest, testStep );
		}

		if( listModel != null )
			listModel.securityCheckAdded( newSecCheck );

		addSecurityCheckByTestStepId( testStep.getId(), newSecCheck );
		return newSecCheck;

	}

	/**
	 * Adds new security check to TestStep SceurutyTest config
	 * 
	 * @param factory
	 * @param newSecCheckConfig
	 * @param testStepSecurityTestConfig
	 * @param testStep
	 * @return
	 */
	private AbstractSecurityCheck addSecurityCheck( AbstractSecurityCheckFactory factory,
			SecurityCheckConfig newSecCheckConfig, TestStepSecurityTestConfig testStepSecurityTestConfig, TestStep testStep )
	{
		SecurityCheckConfig newSecurityCheck = testStepSecurityTestConfig.addNewTestStepSecurityCheck();
		newSecurityCheck.setConfig( newSecCheckConfig.getConfig() );
		newSecurityCheck.setType( newSecCheckConfig.getType() );
		newSecurityCheck.setName( newSecCheckConfig.getName() );

		return factory.buildSecurityCheck( testStep, newSecurityCheck, this );
	}

	private void addSecurityCheckByTestStepId( String testStepId, AbstractSecurityCheck newSecCheck )
	{
		if( securityChecksMap.containsKey( testStepId ) )
		{
			if( !securityChecksMap.get( testStepId ).contains( newSecCheck ) )
			{
				securityChecksMap.get( testStepId ).add( newSecCheck );
			}
		}
		else
		{
			List<AbstractSecurityCheck> list = new ArrayList<AbstractSecurityCheck>();
			list.add( newSecCheck );
			securityChecksMap.put( testStepId, list );
		}
	}

	/**
	 * Remove securityCheck for the specific TestStep
	 * 
	 * @param testStep
	 * @param securityCheck
	 * 
	 */
	public void removeSecurityCheck( TestStep testStep, AbstractSecurityCheck securityCheck )
	{
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( int i = 0; i < testStepSecurityTestList.size(); i++ )
			{
				TestStepSecurityTestConfig testStepSecurityTest = testStepSecurityTestList.get( i );
				if( testStepSecurityTest.getTestStepId().equals( testStep.getId() ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					Iterator<SecurityCheckConfig> secListIterator = securityCheckList.iterator();
					while( secListIterator.hasNext() )
					{
						SecurityCheckConfig current = secListIterator.next();
						if( current.getName().equals( securityCheck.getName() ) )
						{
							secListIterator.remove();
							break;
						}
					}
					if( securityCheckList.isEmpty() )
					{
						getConfig().removeTestStepSecurityTest( i );
					}
					listModel.securityCheckRemoved( securityCheck );
				}
			}
		}
		removeSecurityCheckByTestStepId( testStep.getId(), securityCheck );
	}

	private void removeSecurityCheckByTestStepId( String testStepId, AbstractSecurityCheck securityCheck )
	{
		if( securityChecksMap.containsKey( testStepId ) )
		{
			if( securityChecksMap.get( testStepId ).contains( securityCheck ) )
			{
				securityChecksMap.get( testStepId ).remove( securityCheck );
			}
		}
	}

	/**
	 * Returns a map of testids to security checks
	 * 
	 * @return A map of TestStepIds to their relevant security checks
	 */
	public HashMap<String, List<AbstractSecurityCheck>> getSecurityChecksMap()
	{
		if( !securityChecksMap.isEmpty() )
		{
			return securityChecksMap;
		}

		return createSecurityChecksMap();
	}

	public int getSecurityCheckCount()
	{
		Iterator<List<AbstractSecurityCheck>> checkedSteps = getSecurityChecksMap().values().iterator();
		int count = 0;
		while( checkedSteps.hasNext() )
		{
			List<AbstractSecurityCheck> checkList = checkedSteps.next();
			count += checkList.size();
		}
		return count;
	}

	private HashMap<String, List<AbstractSecurityCheck>> createSecurityChecksMap()
	{
		if( getConfig() != null )
		{
			if( !getConfig().getTestStepSecurityTestList().isEmpty() )
			{
				for( TestStepSecurityTestConfig testStepSecurityTestListConfig : getConfig().getTestStepSecurityTestList() )
				{
					List<AbstractSecurityCheck> checkList = new ArrayList<AbstractSecurityCheck>();
					if( testStepSecurityTestListConfig != null )
					{
						if( !testStepSecurityTestListConfig.getTestStepSecurityCheckList().isEmpty() )
						{
							for( SecurityCheckConfig secCheckConfig : testStepSecurityTestListConfig
									.getTestStepSecurityCheckList() )
							{
								TestStep testStep = null;
								for( TestStep ts : testCase.getTestSteps().values() )
									if( testStepSecurityTestListConfig.getTestStepId().equals( ts.getId() ) )
									{
										testStep = ts;
										AbstractSecurityCheck securityCheck = SoapUI.getSoapUICore().getSecurityCheckRegistry()
												.getFactory( secCheckConfig.getType() )
												.buildSecurityCheck( testStep, secCheckConfig, this );
										checkList.add( securityCheck );
									}
							}
						}
					}
					securityChecksMap.put( testStepSecurityTestListConfig.getTestStepId(), checkList );
				}
			}
		}
		return securityChecksMap;
	}

	public Map<TestStep, SecurityTestStepResult> getSecurityTestStepResultMap()
	{
		return securityTestStepResultMap;
	}

	/**
	 * Puts result of a SecurityTest on a TestStep level to a map, if map
	 * previously contained value for specified TestStep it is beeing replaced
	 * with the new result value
	 * 
	 * @param testStep
	 * @param securityTestStepResult
	 */
	public void putSecurityTestStepResult( TestStep testStep, SecurityTestStepResult securityTestStepResult )
	{
		securityTestStepResultMap.put( testStep, securityTestStepResult );
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

		runner = new SecurityTestRunnerImpl( this, context );
		runner.start( async );
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

	public List<AbstractSecurityCheck> getTestStepSecurityChecks( String testStepId )
	{
		return getSecurityChecksMap().get( testStepId ) != null ? getSecurityChecksMap().get( testStepId )
				: new ArrayList<AbstractSecurityCheck>();
	}

	public SecurityCheck getTestStepSecurityCheckByName( String testStepId, String securityCheckName )
	{
		List<AbstractSecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
		for( int c = 0; c < securityChecksList.size(); c++ )
		{
			AbstractSecurityCheck securityCheck = getTestStepSecurityCheckAt( testStepId, c );
			if( securityCheckName.equals( securityCheck.getName() ) )
				return securityCheck;
		}

		return null;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends SecurityCheck> List<T> getTestStepSecurityCheckByType( String testStepId,
			Class<T> securityCheckType )
	{
		List<T> result = new ArrayList<T>();
		for( SecurityCheck check : getTestStepSecurityChecks( testStepId ) )
			if( securityCheckType.isAssignableFrom( check.getClass() ) )
				result.add( ( T )check );

		return result;
	}

	public AbstractSecurityCheck getTestStepSecurityCheckAt( String testStepId, int index )
	{
		List<AbstractSecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
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
			if( getSecurityChecksMap().get( testStepId ) != null )
				return getSecurityChecksMap().get( testStepId ).size();
			else
				return 0;
		}
	}

	/**
	 * Moves specified SecurityCheck of a TestStep in a list
	 * 
	 * @param testStep
	 * @param securityCheck
	 * @param index
	 * @param offset
	 *           specifies position to move to , negative value means moving up
	 *           while positive value means moving down
	 * @return new AbstractSecurityCheck
	 */
	public AbstractSecurityCheck moveTestStepSecurityCheck( TestStep testStep, AbstractSecurityCheck securityCheck,
			int index, int offset )
	{
		List<TestStepSecurityTestConfig> testStepSecurityTestList = getConfig().getTestStepSecurityTestList();
		if( !testStepSecurityTestList.isEmpty() )
		{
			for( TestStepSecurityTestConfig testStepSecurityTest : testStepSecurityTestList )
			{
				if( testStepSecurityTest.getTestStepId().equals( testStep.getId() ) )
				{
					List<SecurityCheckConfig> securityCheckList = testStepSecurityTest.getTestStepSecurityCheckList();
					AbstractSecurityCheckFactory factory = SoapUI.getSoapUICore().getSecurityCheckRegistry()
							.getFactory( securityCheck.getType() );
					SecurityCheckConfig newSecCheckConfig = ( SecurityCheckConfig )securityCheck.getConfig().copy();
					AbstractSecurityCheck newSecCheck = factory.buildSecurityCheck( testStep, newSecCheckConfig, this );

					securityCheckList.remove( securityCheck.getConfig() );
					securityCheckList.add( index + offset, newSecCheckConfig );
					SecurityCheckConfig[] cc = new SecurityCheckConfig[securityCheckList.size()];
					for( int i = 0; i < securityCheckList.size(); i++ )
					{
						cc[i] = securityCheckList.get( i );
					}
					testStepSecurityTest.setTestStepSecurityCheckArray( cc );

					TestStepSecurityTestConfig[] vv = new TestStepSecurityTestConfig[testStepSecurityTestList.size()];
					for( int i = 0; i < testStepSecurityTestList.size(); i++ )
					{
						vv[i] = testStepSecurityTestList.get( i );
					}
					getConfig().setTestStepSecurityTestArray( vv );
					listModel.securityCheckMoved( newSecCheck, index, offset );
					return newSecCheck;
				}
			}
		}
		return null;
	}

	public String findTestStepCheckUniqueName( String testStepId, String type )
	{
		String name = type;
		int numNames = 0;
		List<AbstractSecurityCheck> securityChecksList = getTestStepSecurityChecks( testStepId );
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

	public void addSecurityTestRunListener( SecurityTestRunListener listener )
	{
		if( listener == null )
			throw new RuntimeException( "listener must not be null" );

		securityTestRunListeners.add( listener );
	}

	public void removeSecurityTestRunListener( SecurityTestRunListener listener )
	{
		securityTestRunListeners.remove( listener );
	}

	public SecurityTestRunListener[] getSecurityTestRunListeners()
	{
		return securityTestRunListeners.toArray( new SecurityTestRunListener[securityTestRunListeners.size()] );
	}

	public boolean getFailSecurityTestOnCheckErrors()
	{
		return getConfig().getFailSecurityTestOnCheckErrors();
	}

	public void setFailSecurityTestOnCheckErrors( boolean failSecurityTestOnErrors )
	{
		boolean old = getFailSecurityTestOnCheckErrors();
		if( old != failSecurityTestOnErrors )
		{
			getConfig().setFailSecurityTestOnCheckErrors( failSecurityTestOnErrors );
			notifyPropertyChanged( FAIL_ON_CHECKS_ERRORS_PROPERTY, old, failSecurityTestOnErrors );
		}
	}

	public boolean getFailOnError()
	{
		return getConfig().getFailOnError();
	}

	public void setFailOnError( boolean failOnError )
	{
		boolean old = getFailOnError();
		if( old != failOnError )
		{
			getConfig().setFailOnError( failOnError );
			notifyPropertyChanged( FAIL_ON_ERROR_PROPERTY, old, failOnError );
		}
	}

	public void addTestStepRunListener( TestStep testStep, SecurityTestRunListener listener )
	{
		if( listener == null )
			throw new RuntimeException( "listener must not be null" );

		if( securityTestStepRunListeners.containsKey( testStep ) )
		{
			securityTestStepRunListeners.get( testStep ).add( listener );
		}
		else
		{
			Set<SecurityTestRunListener> listeners = new HashSet<SecurityTestRunListener>();
			listeners.add( listener );
			securityTestStepRunListeners.put( testStep, listeners );
		}
	}

	public void removeTestStepRunListener( TestStep testStep, SecurityTestRunListener listener )
	{
		securityTestStepRunListeners.remove( securityTestStepRunListeners.get( testStep ) );
	}

	public SecurityTestRunListener[] getTestStepRunListeners( TestStep testStep )
	{
		if( securityTestStepRunListeners.containsKey( testStep ) )
		{
			Set<SecurityTestRunListener> listeners = securityTestStepRunListeners.get( testStep );
			return listeners.toArray( new SecurityTestRunListener[listeners.size()] );
		}
		else
		{
			return new SecurityTestRunListener[0];
		}
	}

	@Override
	public List<? extends ModelItem> getChildren()
	{
		List<ModelItem> result = new ArrayList<ModelItem>();
		Set<String> testStepIds = getSecurityChecksMap().keySet();
		for( String testStepId : testStepIds )
		{
			List<AbstractSecurityCheck> t = getSecurityChecksMap().get( testStepId );
			for( int i = 0; i < t.size(); i++ )
			{
				AbstractSecurityCheck check = t.get( i );
				result.add( check );
			}
		}
		return result;
	}

	public void resetConfigOnMove( SecurityTestConfig securityTestConfig )
	{
		setConfig( securityTestConfig );

		if( securityTestConfig != null )
		{
			if( !securityTestConfig.getTestStepSecurityTestList().isEmpty() )
			{
				for( TestStepSecurityTestConfig testStepSecurityTestListConfig : securityTestConfig
						.getTestStepSecurityTestList() )
				{
					List<AbstractSecurityCheck> checkList = getSecurityChecksMap().get(
							testStepSecurityTestListConfig.getTestStepId() );

					for( int i = 0; i < checkList.size(); i++ )
					{
						checkList.get( i ).updateSecurityConfig(
								testStepSecurityTestListConfig.getTestStepSecurityCheckList().get( i ) );
					}
				}
			}
		}
	}

}
