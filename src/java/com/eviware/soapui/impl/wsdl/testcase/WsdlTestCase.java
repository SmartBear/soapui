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

package com.eviware.soapui.impl.wsdl.testcase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.AbstractTestPropertyHolderWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.loadtest.LoadTestAssertion;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.loadtest.assertions.TestStepStatusAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * TestCase implementation for WSDL projects
 * 
 * @author Ole.Matzura
 */

public class WsdlTestCase extends AbstractTestPropertyHolderWsdlModelItem<TestCaseConfig> implements TestCase
{
	private final static Logger logger = Logger.getLogger( WsdlTestCase.class );
	public final static String KEEP_SESSION_PROPERTY = WsdlTestCase.class.getName() + "@keepSession";
	public final static String FAIL_ON_ERROR_PROPERTY = WsdlTestCase.class.getName() + "@failOnError";
	public final static String FAIL_ON_ERRORS_PROPERTY = WsdlTestCase.class.getName() + "@failOnErrors";
	public final static String DISCARD_OK_RESULTS = WsdlTestCase.class.getName() + "@discardOkResults";
	public final static String SETUP_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@setupScript";
	public final static String TEARDOWN_SCRIPT_PROPERTY = WsdlTestCase.class.getName() + "@tearDownScript";
	public static final String TIMEOUT_PROPERTY = WsdlTestCase.class.getName() + "@timeout";
	public static final String SEARCH_PROPERTIES_PROPERTY = WsdlTestCase.class.getName() + "@searchProperties";

	private final WsdlTestSuite testSuite;
	private List<WsdlTestStep> testSteps = new ArrayList<WsdlTestStep>();
	private List<WsdlLoadTest> loadTests = new ArrayList<WsdlLoadTest>();
	private Set<TestRunListener> testRunListeners = new HashSet<TestRunListener>();
	private DefaultActionList createActions;
	private final boolean forLoadTest;
	private SoapUIScriptEngine setupScriptEngine;
	private SoapUIScriptEngine tearDownScriptEngine;

	public WsdlTestCase( WsdlTestSuite testSuite, TestCaseConfig config, boolean forLoadTest )
	{
		super( config, testSuite, "/testCase.gif" );

		this.testSuite = testSuite;
		this.forLoadTest = forLoadTest;

		List<TestStepConfig> testStepConfigs = config.getTestStepList();
		for( TestStepConfig tsc : testStepConfigs )
		{
			WsdlTestStep testStep = createTestStepFromConfig( tsc );
			if( testStep != null )
			{
				ensureUniqueName( testStep );
				testSteps.add( testStep );
			}
		}

		if( !forLoadTest )
		{
			List<LoadTestConfig> loadTestConfigs = config.getLoadTestList();
			for( LoadTestConfig tsc : loadTestConfigs )
			{
				WsdlLoadTest loadTest = new WsdlLoadTest( this, tsc );
				loadTests.add( loadTest );
			}
		}

		// init default configs
		if( !config.isSetFailOnError() )
			config.setFailOnError( true );

		if( !config.isSetFailTestCaseOnErrors() )
			config.setFailTestCaseOnErrors( true );

		if( !config.isSetKeepSession() )
			config.setKeepSession( false );

		if( !config.isSetMaxResults() )
			config.setMaxResults( 0 );

		for( TestRunListener listener : SoapUI.getListenerRegistry().getListeners( TestRunListener.class ) )
		{
			addTestRunListener( listener );
		}

		if( !getConfig().isSetProperties() )
			getConfig().addNewProperties();

		setPropertiesConfig( getConfig().getProperties() );
	}

	public boolean getKeepSession()
	{
		return getConfig().getKeepSession();
	}

	public void setKeepSession( boolean keepSession )
	{
		boolean old = getKeepSession();
		if( old != keepSession )
		{
			getConfig().setKeepSession( keepSession );
			notifyPropertyChanged( KEEP_SESSION_PROPERTY, old, keepSession );
		}
	}

	public void setSetupScript( String script )
	{
		String oldScript = getSetupScript();

		if( !getConfig().isSetSetupScript() )
			getConfig().addNewSetupScript();

		getConfig().getSetupScript().setStringValue( script );
		if( setupScriptEngine != null )
			setupScriptEngine.setScript( script );

		notifyPropertyChanged( SETUP_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getSetupScript()
	{
		return getConfig().isSetSetupScript() ? getConfig().getSetupScript().getStringValue() : null;
	}

	public void setTearDownScript( String script )
	{
		String oldScript = getTearDownScript();

		if( !getConfig().isSetTearDownScript() )
			getConfig().addNewTearDownScript();

		getConfig().getTearDownScript().setStringValue( script );
		if( tearDownScriptEngine != null )
			tearDownScriptEngine.setScript( script );

		notifyPropertyChanged( TEARDOWN_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getTearDownScript()
	{
		return getConfig().isSetTearDownScript() ? getConfig().getTearDownScript().getStringValue() : null;
	}

	public boolean getFailOnError()
	{
		return getConfig().getFailOnError();
	}

	public boolean getFailTestCaseOnErrors()
	{
		return getConfig().getFailTestCaseOnErrors();
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

	public void setFailTestCaseOnErrors( boolean failTestCaseOnErrors )
	{
		boolean old = getFailTestCaseOnErrors();
		if( old != failTestCaseOnErrors )
		{
			getConfig().setFailTestCaseOnErrors( failTestCaseOnErrors );
			notifyPropertyChanged( FAIL_ON_ERRORS_PROPERTY, old, failTestCaseOnErrors );
		}
	}

	public boolean getSearchProperties()
	{
		return getConfig().getSearchProperties();
	}

	public void setSearchProperties( boolean searchProperties )
	{
		boolean old = getSearchProperties();
		if( old != searchProperties )
		{
			getConfig().setSearchProperties( searchProperties );
			notifyPropertyChanged( SEARCH_PROPERTIES_PROPERTY, old, searchProperties );
		}
	}

	public boolean getDiscardOkResults()
	{
		return getConfig().getDiscardOkResults();
	}

	public void setDiscardOkResults( boolean discardOkResults )
	{
		boolean old = getDiscardOkResults();
		if( old != discardOkResults )
		{
			getConfig().setDiscardOkResults( discardOkResults );
			notifyPropertyChanged( DISCARD_OK_RESULTS, old, discardOkResults );
		}
	}

	public int getMaxResults()
	{
		return getConfig().getMaxResults();
	}

	public void setMaxResults( int maxResults )
	{
		int old = getMaxResults();
		if( old != maxResults )
		{
			getConfig().setMaxResults( maxResults );
			notifyPropertyChanged( "maxResults", old, maxResults );
		}
	}

	private WsdlTestStep createTestStepFromConfig( TestStepConfig tsc )
	{
		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory( tsc.getType() );
		if( factory != null )
		{
			//Forward incompatibility, test for incompatible future version of HttpTestStep:
			if( tsc.getConfig() != null && tsc.getConfig() instanceof RestRequestStepConfig && 
					((RestRequestStepConfig)tsc.getConfig().changeType(RestRequestStepConfig.type)).getRestRequest() == null)
				return null;
			
			WsdlTestStep testStep = factory.buildTestStep( this, tsc, forLoadTest );
			return testStep;
		}
		else
		{
			logger.error( "Failed to create test step for [" + tsc.getName() + "]" );
			return null;
		}
	}

	private boolean ensureUniqueName( WsdlTestStep testStep )
	{
		String name = testStep.getName();
		while( name == null || getTestStepByName( name ) != null )
		{
			if( name == null )
				name = testStep.getName();
			else
			{
				int cnt = 0;

				while( getTestStepByName( name ) != null )
				{
					cnt++ ;
					name = testStep.getName() + " " + cnt;
				}

				if( cnt == 0 )
					break;
			}

			name = UISupport.prompt( "TestStep name must be unique, please specify new name for step\n" + "["
					+ testStep.getName() + "] in TestCase [" + getTestSuite().getProject().getName() + "->"
					+ getTestSuite().getName() + "->" + getName() + "]", "Change TestStep name", name );

			if( name == null )
				return false;
		}

		if( !name.equals( testStep.getName() ) )
			testStep.setName( name );

		return true;
	}

	public WsdlLoadTest addNewLoadTest( String name )
	{
		WsdlLoadTest loadTest = new WsdlLoadTest( this, getConfig().addNewLoadTest() );
		loadTest.setStartDelay( 0 );
		loadTest.setName( name );
		loadTests.add( loadTest );

		loadTest.addAssertion( TestStepStatusAssertion.STEP_STATUS_TYPE, LoadTestAssertion.ANY_TEST_STEP, false );

		( getTestSuite() ).fireLoadTestAdded( loadTest );

		return loadTest;
	}

	public void removeLoadTest( WsdlLoadTest loadTest )
	{
		int ix = loadTests.indexOf( loadTest );

		loadTests.remove( ix );

		try
		{
			( getTestSuite() ).fireLoadTestRemoved( loadTest );
		}
		finally
		{
			loadTest.release();
			getConfig().removeLoadTest( ix );
		}
	}

	public WsdlTestSuite getTestSuite()
	{
		return testSuite;
	}

	public WsdlTestStep cloneStep( WsdlTestStep testStep, String name )
	{
		return testStep.clone( this, name );
	}

	public WsdlTestStep getTestStepAt( int index )
	{
		return testSteps.get( index );
	}

	public int getTestStepCount()
	{
		return testSteps.size();
	}

	public WsdlLoadTest getLoadTestAt( int index )
	{
		return loadTests.get( index );
	}

	public LoadTest getLoadTestByName( String loadTestName )
	{
		return ( LoadTest )getWsdlModelItemByName( loadTests, loadTestName );
	}

	public int getLoadTestCount()
	{
		return loadTests.size();
	}

	public WsdlTestStep addTestStep( TestStepConfig stepConfig )
	{
		return insertTestStep( stepConfig, -1, true );
	}

	public WsdlTestStep addTestStep( String type, String name )
	{
		TestStepConfig newStepConfig = WsdlTestStepRegistry.getInstance().getFactory( type ).createNewTestStep( this,
				name );
		if( newStepConfig != null )
		{
			return addTestStep( newStepConfig );
		}
		else
			return null;
	}

	public WsdlTestStep insertTestStep( String type, String name, int index )
	{
		TestStepConfig newStepConfig = WsdlTestStepRegistry.getInstance().getFactory( type ).createNewTestStep( this,
				name );
		if( newStepConfig != null )
		{
			return insertTestStep( newStepConfig, index, false );
		}
		else
			return null;
	}

	public WsdlTestStep importTestStep( WsdlTestStep testStep, String name, int index, boolean createCopy )
	{
		testStep.beforeSave();
		TestStepConfig newStepConfig = ( TestStepConfig )testStep.getConfig().copy();
		newStepConfig.setName( name );

		WsdlTestStep result = insertTestStep( newStepConfig, index, createCopy );

		if( createCopy )
		{
			ModelSupport.unsetIds( result );
		}

		resolveTestCase();
		return result;
	}

	private void resolveTestCase()
	{
		ResolveDialog resolver = new ResolveDialog( "Validate TestCase", "Checks TestCase for inconsistencies", null );
		resolver.setShowOkMessage( false );
		resolver.resolve( this );
	}

	public WsdlTestStep[] importTestSteps( WsdlTestStep[] testSteps, int index, boolean createCopies )
	{
		TestStepConfig[] newStepConfigs = new TestStepConfig[testSteps.length];

		for( int c = 0; c < testSteps.length; c++ )
		{
			testSteps[c].beforeSave();
			newStepConfigs[c] = ( TestStepConfig )testSteps[c].getConfig().copy();
		}

		WsdlTestStep[] result = insertTestSteps( newStepConfigs, index, createCopies );

		resolveTestCase();
		return result;
	}

	public WsdlTestStep insertTestStep( TestStepConfig stepConfig, int ix )
	{
		return insertTestStep( stepConfig, ix, true );
	}

	public WsdlTestStep insertTestStep( TestStepConfig stepConfig, int ix, boolean clearIds )
	{
		TestStepConfig newStepConfig = ix == -1 ? getConfig().addNewTestStep() : getConfig().insertNewTestStep( ix );
		newStepConfig.set( stepConfig );
		WsdlTestStep testStep = createTestStepFromConfig( newStepConfig );

		if( !ensureUniqueName( testStep ) )
			return null;

		if( clearIds )
		{
			ModelSupport.unsetIds( testStep );
		}

		if( ix == -1 )
			testSteps.add( testStep );
		else
			testSteps.add( ix, testStep );

		testStep.afterLoad();

		if( getTestSuite() != null )
			( getTestSuite() ).fireTestStepAdded( testStep, ix == -1 ? testSteps.size() - 1 : ix );

		return testStep;
	}

	public WsdlTestStep[] insertTestSteps( TestStepConfig[] stepConfig, int ix, boolean clearIds )
	{
		WsdlTestStep[] result = new WsdlTestStep[stepConfig.length];

		for( int c = 0; c < stepConfig.length; c++ )
		{
			TestStepConfig newStepConfig = ix == -1 ? getConfig().addNewTestStep() : getConfig()
					.insertNewTestStep( ix + c );
			newStepConfig.set( stepConfig[c] );
			WsdlTestStep testStep = createTestStepFromConfig( newStepConfig );

			if( !ensureUniqueName( testStep ) )
				return null;

			if( clearIds )
				ModelSupport.unsetIds( testStep );

			if( ix == -1 )
				testSteps.add( testStep );
			else
				testSteps.add( ix + c, testStep );

			result[c] = testStep;
		}

		for( int c = 0; c < result.length; c++ )
		{
			result[c].afterLoad();

			if( getTestSuite() != null )
				( getTestSuite() ).fireTestStepAdded( result[c], getIndexOfTestStep( result[c] ) );
		}

		return result;
	}

	public void removeTestStep( WsdlTestStep testStep )
	{
		int ix = testSteps.indexOf( testStep );
		if( ix == -1 )
		{
			logger.error( "TestStep [" + testStep.getName() + "] passed to removeTestStep in testCase [" + getName()
					+ "] not found" );
			return;
		}

		testSteps.remove( ix );

		try
		{
			( getTestSuite() ).fireTestStepRemoved( testStep, ix );
		}
		finally
		{
			testStep.release();

			for( int c = 0; c < getConfig().sizeOfTestStepArray(); c++ )
			{
				if( testStep.getConfig() == getConfig().getTestStepArray( c ) )
				{
					getConfig().removeTestStep( c );
					break;
				}
			}
		}
	}

	public WsdlTestCaseRunner run( StringToObjectMap properties, boolean async )
	{
		WsdlTestCaseRunner runner = new WsdlTestCaseRunner( this, properties );
		runner.start( async );
		return runner;
	}

	public void addTestRunListener( TestRunListener listener )
	{
		if( listener == null )
			throw new RuntimeException( "listener must not be null" );

		testRunListeners.add( listener );
	}

	public void removeTestRunListener( TestRunListener listener )
	{
		testRunListeners.remove( listener );
	}

	public TestRunListener[] getTestRunListeners()
	{
		return testRunListeners.toArray( new TestRunListener[testRunListeners.size()] );
	}

	public Map<String, TestStep> getTestSteps()
	{
		Map<String, TestStep> result = new HashMap<String, TestStep>();
		for( TestStep testStep : testSteps )
			result.put( testStep.getName(), testStep );

		return result;
	}

	public Map<String, LoadTest> getLoadTests()
	{
		Map<String, LoadTest> result = new HashMap<String, LoadTest>();
		for( LoadTest loadTest : loadTests )
			result.put( loadTest.getName(), loadTest );

		return result;
	}

	public int getIndexOfTestStep( TestStep step )
	{
		return testSteps.indexOf( step );
	}

	/**
	 * Moves a step by the specified offset, a bit awkward since xmlbeans doesn't
	 * support reordering of arrays, we need to create copies of the contained
	 * XmlObjects
	 * 
	 * @param ix
	 * @param offset
	 */

	public void moveTestStep( int ix, int offset )
	{
		if( offset == 0 )
			return;
		WsdlTestStep step = testSteps.get( ix );

		if( ix + offset >= testSteps.size() )
			offset = testSteps.size() - ix - 1;

		testSteps.remove( ix );
		testSteps.add( ix + offset, step );

		TestStepConfig[] configs = new TestStepConfig[testSteps.size()];

		TestCaseConfig conf = getConfig();
		for( int c = 0; c < testSteps.size(); c++ )
		{
			if( offset > 0 )
			{
				if( c < ix )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c ).copy();
				else if( c < ( ix + offset ) )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c + 1 ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( ix ).copy();
				else
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c ).copy();
			}
			else
			{
				if( c < ix + offset )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( ix ).copy();
				else if( c <= ix )
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c - 1 ).copy();
				else
					configs[c] = ( TestStepConfig )conf.getTestStepArray( c ).copy();
			}
		}

		conf.setTestStepArray( configs );
		for( int c = 0; c < configs.length; c++ )
		{
			( testSteps.get( c ) ).resetConfigOnMove( conf.getTestStepArray( c ) );
		}

		( getTestSuite() ).fireTestStepMoved( step, ix, offset );
	}

	public int getIndexOfLoadTest( LoadTest loadTest )
	{
		return loadTests.indexOf( loadTest );
	}

	public int getTestStepIndexByName( String stepName )
	{
		for( int c = 0; c < testSteps.size(); c++ )
		{
			if( testSteps.get( c ).getName().equals( stepName ) )
				return c;
		}

		return -1;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends TestStep> T findPreviousStepOfType( TestStep referenceStep, Class<T> stepClass )
	{
		int currentStepIndex = getIndexOfTestStep( referenceStep );
		int ix = currentStepIndex - 1;
		while( ix >= 0 && !stepClass.isAssignableFrom( getTestStepAt( ix ).getClass()) )
		{
			ix-- ;
		}

		return ( T )( ix < 0 ? null : getTestStepAt( ix ) );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends TestStep> T findNextStepOfType( TestStep referenceStep, Class<T> stepClass )
	{
		int currentStepIndex = getIndexOfTestStep( referenceStep );
		int ix = currentStepIndex + 1;
		while( ix < getTestStepCount() && !stepClass.isAssignableFrom( getTestStepAt( ix ).getClass()) )
		{
			ix++ ;
		}

		return ( T )( ix >= getTestStepCount() ? null : getTestStepAt( ix ) );
	}

	public List<TestStep> getTestStepList()
	{
		List<TestStep> result = new ArrayList<TestStep>();
		for( TestStep step : testSteps )
			result.add( step );

		return result;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends TestStep> List<T> getTestStepsOfType( Class<T> stepType )
	{
		List<T> result = new ArrayList<T>();
		for( TestStep step : testSteps )
			if( step.getClass().isAssignableFrom( stepType ) )
				result.add( ( T )step );

		return result;
	}

	public WsdlTestStep getTestStepByName( String stepName )
	{
		return ( WsdlTestStep )getWsdlModelItemByName( testSteps, stepName );
	}

	public WsdlLoadTest cloneLoadTest( WsdlLoadTest loadTest, String name )
	{
		loadTest.beforeSave();

		LoadTestConfig loadTestConfig = getConfig().addNewLoadTest();
		loadTestConfig.set( loadTest.getConfig().copy() );

		WsdlLoadTest newLoadTest = new WsdlLoadTest( this, loadTestConfig );
		newLoadTest.setName( name );
		ModelSupport.unsetIds( newLoadTest );
		newLoadTest.afterLoad();
		loadTests.add( newLoadTest );

		( getTestSuite() ).fireLoadTestAdded( newLoadTest );

		return newLoadTest;
	}

	@Override
	public void release()
	{
		super.release();

		for( WsdlTestStep testStep : testSteps )
			testStep.release();

		for( WsdlLoadTest loadTest : loadTests )
			loadTest.release();

		testRunListeners.clear();

		if( setupScriptEngine != null )
			setupScriptEngine.release();

		if( tearDownScriptEngine != null )
			tearDownScriptEngine.release();
	}

	public ActionList getCreateActions()
	{
		return createActions;
	}

	public void resetConfigOnMove( TestCaseConfig testCaseConfig )
	{
		setConfig( testCaseConfig );
		int mod = 0;

		List<TestStepConfig> configs = getConfig().getTestStepList();
		for( int c = 0; c < configs.size(); c++ )
		{
			if( WsdlTestStepRegistry.getInstance().hasFactory( configs.get( c ) ) )
			{
				( testSteps.get( c - mod ) ).resetConfigOnMove( configs.get( c ) );
			}
			else
				mod++ ;
		}

		List<LoadTestConfig> loadTestConfigs = getConfig().getLoadTestList();
		for( int c = 0; c < loadTestConfigs.size(); c++ )
		{
			loadTests.get( c ).resetConfigOnMove( loadTestConfigs.get( c ) );
		}

		setPropertiesConfig( testCaseConfig.getProperties() );
	}

	public List<LoadTest> getLoadTestList()
	{
		List<LoadTest> result = new ArrayList<LoadTest>();
		for( LoadTest loadTest : loadTests )
			result.add( loadTest );

		return result;
	}

	public Object runSetupScript( TestRunContext runContext, TestRunner runner ) throws Exception
	{
		String script = getSetupScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( setupScriptEngine == null )
		{
			setupScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			setupScriptEngine.setScript( script );
		}

		setupScriptEngine.setVariable( "context", runContext );
		setupScriptEngine.setVariable( "testRunner", runner );
		setupScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return setupScriptEngine.run();
	}

	public Object runTearDownScript( TestRunContext runContext, TestRunner runner ) throws Exception
	{
		String script = getTearDownScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( tearDownScriptEngine == null )
		{
			tearDownScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			tearDownScriptEngine.setScript( script );
		}

		tearDownScriptEngine.setVariable( "context", runContext );
		tearDownScriptEngine.setVariable( "testRunner", runner );
		tearDownScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return tearDownScriptEngine.run();
	}

	public List<? extends ModelItem> getChildren()
	{
		List<ModelItem> result = new ArrayList<ModelItem>();
		result.addAll( getTestStepList() );
		result.addAll( getLoadTestList() );
		return result;
	}

	@Override
	public void setName( String name )
	{
		String oldLabel = getLabel();

		super.setName( name );

		String label = getLabel();
		if( oldLabel != null && !oldLabel.equals( label ) )
		{
			notifyPropertyChanged( LABEL_PROPERTY, oldLabel, label );
		}
	}

	public String getLabel()
	{
		String name = getName();
		if( isDisabled() )
			return name + " (disabled)";
		else
			return name;
	}

	public boolean isDisabled()
	{
		return getConfig().getDisabled();
	}

	public void setDisabled( boolean disabled )
	{
		String oldLabel = getLabel();

		boolean oldDisabled = isDisabled();
		if( oldDisabled == disabled )
			return;

		if( disabled )
			getConfig().setDisabled( disabled );
		else if( getConfig().isSetDisabled() )
			getConfig().unsetDisabled();

		notifyPropertyChanged( DISABLED_PROPERTY, oldDisabled, disabled );

		String label = getLabel();
		if( !oldLabel.equals( label ) )
			notifyPropertyChanged( LABEL_PROPERTY, oldLabel, label );
	}

	public long getTimeout()
	{
		return getConfig().getTimeout();
	}

	public void setTimeout( long timeout )
	{
		long old = getTimeout();
		getConfig().setTimeout( timeout );
		notifyPropertyChanged( TIMEOUT_PROPERTY, old, timeout );
	}

	public void exportTestCase( File file )
	{
		try
		{
			this.getConfig().newCursor().save( file );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

	}

	public void afterCopy( WsdlTestSuite oldTestSuite, WsdlTestCase oldTestCase )
	{
		for( WsdlTestStep testStep : testSteps )
			testStep.afterCopy( oldTestSuite, oldTestCase );
	}
}
