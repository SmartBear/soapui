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

package com.eviware.soapui.impl.wsdl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestCaseDocumentConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig.Enum;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

/**
 * TestSuite implementation for WSDL projects.
 * 
 * @author Ole.Matzura
 */

public class WsdlTestSuite extends AbstractTestPropertyHolderWsdlModelItem<TestSuiteConfig> implements TestSuite
{
	public final static String SETUP_SCRIPT_PROPERTY = WsdlTestSuite.class.getName() + "@setupScript";
	public final static String TEARDOWN_SCRIPT_PROPERTY = WsdlTestSuite.class.getName() + "@tearDownScript";

	private final WsdlProject project;
	private List<WsdlTestCase> testCases = new ArrayList<WsdlTestCase>();
	private Set<TestSuiteListener> testSuiteListeners = new HashSet<TestSuiteListener>();
	private SoapUIScriptEngine setupScriptEngine;
	private SoapUIScriptEngine tearDownScriptEngine;

	public WsdlTestSuite( WsdlProject project, TestSuiteConfig config )
	{
		super( config, project, "/testSuite.gif" );
		this.project = project;

		List<TestCaseConfig> testCaseConfigs = config.getTestCaseList();
		for( int i = 0; i < testCaseConfigs.size(); i++ )
		{
			testCases.add( new WsdlTestCase( this, testCaseConfigs.get( i ), false ) );
		}

		if( !config.isSetRunType() )
			config.setRunType( TestSuiteRunTypesConfig.SEQUENTIAL );

		for( TestSuiteListener listener : SoapUI.getListenerRegistry().getListeners( TestSuiteListener.class ) )
		{
			addTestSuiteListener( listener );
		}

		if( !config.isSetProperties() )
			config.addNewProperties();

		setPropertiesConfig( config.getProperties() );
	}

	public TestSuiteRunType getRunType()
	{
		Enum runType = getConfig().getRunType();

		if( runType.equals( TestSuiteRunTypesConfig.PARALLELL ) )
			return TestSuiteRunType.PARALLEL;
		else
			return TestSuiteRunType.SEQUENTIAL;
	}

	public void setRunType( TestSuiteRunType runType )
	{
		TestSuiteRunType oldRunType = getRunType();

		if( runType == TestSuiteRunType.PARALLEL && oldRunType != TestSuiteRunType.PARALLEL )
		{
			getConfig().setRunType( TestSuiteRunTypesConfig.PARALLELL );
			notifyPropertyChanged( RUNTYPE_PROPERTY, oldRunType, runType );
		}
		else if( runType == TestSuiteRunType.SEQUENTIAL && oldRunType != TestSuiteRunType.SEQUENTIAL )
		{
			getConfig().setRunType( TestSuiteRunTypesConfig.SEQUENTIAL );
			notifyPropertyChanged( RUNTYPE_PROPERTY, oldRunType, runType );
		}
	}

	public WsdlProject getProject()
	{
		return project;
	}

	public int getTestCaseCount()
	{
		return testCases.size();
	}

	public WsdlTestCase getTestCaseAt( int index )
	{
		return testCases.get( index );
	}

	public WsdlTestCase getTestCaseByName( String testCaseName )
	{
		return ( WsdlTestCase )getWsdlModelItemByName( testCases, testCaseName );
	}

	public WsdlTestCase cloneTestCase( WsdlTestCase testCase, String name )
	{
		testCase.beforeSave();
		TestCaseConfig newTestCase = getConfig().addNewTestCase();
		newTestCase.set( testCase.getConfig() );
		newTestCase.setName( name );
		WsdlTestCase newWsdlTestCase = new WsdlTestCase( this, newTestCase, false );
		ModelSupport.unsetIds( newWsdlTestCase );
		newWsdlTestCase.afterLoad();

		testCases.add( newWsdlTestCase );
		fireTestCaseAdded( newWsdlTestCase );

		return newWsdlTestCase;
	}

	public WsdlTestCase addNewTestCase( String name )
	{
		WsdlTestCase testCase = new WsdlTestCase( this, getConfig().addNewTestCase(), false );
		testCase.setName( name );
		testCase.setFailOnError( true );
		testCase.setSearchProperties( true );
		testCases.add( testCase );
		fireTestCaseAdded( testCase );

		return testCase;
	}

	public WsdlTestCase importTestCase( WsdlTestCase testCase, String name, int index, boolean includeLoadTests,
			boolean createCopy )
	{
		testCase.beforeSave();

		if( index >= testCases.size() )
			index = -1;

		TestCaseConfig testCaseConfig = index == -1 ? ( TestCaseConfig )getConfig().addNewTestCase().set(
				testCase.getConfig().copy() ) : ( TestCaseConfig )getConfig().insertNewTestCase( index ).set(
				testCase.getConfig().copy() );
		testCaseConfig.setName( name );

		if( !includeLoadTests )
			testCaseConfig.setLoadTestArray( new LoadTestConfig[0] );

		WsdlTestCase oldTestCase = testCase;
		testCase = new WsdlTestCase( this, testCaseConfig, false );

		if( createCopy )
		{
			ModelSupport.unsetIds( testCase );
		}

		if( index == -1 )
			testCases.add( testCase );
		else
			testCases.add( index, testCase );

		testCase.afterLoad();

		if( createCopy )
		{
			testCase.afterCopy( null, oldTestCase );
		}

		fireTestCaseAdded( testCase );
		resolveImportedTestCase( testCase );

		return testCase;
	}

	public void removeTestCase( WsdlTestCase testCase )
	{
		int ix = testCases.indexOf( testCase );

		testCases.remove( ix );
		try
		{
			fireTestCaseRemoved( testCase );
		}
		finally
		{
			testCase.release();
			getConfig().removeTestCase( ix );
		}
	}

	public void fireTestCaseAdded( WsdlTestCase testCase )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testCaseAdded( testCase );
		}
	}

	public void fireTestCaseRemoved( WsdlTestCase testCase )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testCaseRemoved( testCase );
		}
	}

	private void fireTestCaseMoved( WsdlTestCase testCase, int ix, int offset )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testCaseMoved( testCase, ix, offset );
		}
	}

	public void fireTestStepAdded( WsdlTestStep testStep, int index )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testStepAdded( testStep, index );
		}
	}

	public void fireTestStepRemoved( WsdlTestStep testStep, int ix )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testStepRemoved( testStep, ix );
		}
	}

	public void fireTestStepMoved( WsdlTestStep testStep, int ix, int offset )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testStepMoved( testStep, ix, offset );
		}
	}

	public void fireLoadTestAdded( WsdlLoadTest loadTest )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].loadTestAdded( loadTest );
		}
	}

	public void fireLoadTestRemoved( WsdlLoadTest loadTest )
	{
		TestSuiteListener[] a = testSuiteListeners.toArray( new TestSuiteListener[testSuiteListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].loadTestRemoved( loadTest );
		}
	}

	public void addTestSuiteListener( TestSuiteListener listener )
	{
		testSuiteListeners.add( listener );
	}

	public void removeTestSuiteListener( TestSuiteListener listener )
	{
		testSuiteListeners.remove( listener );
	}

	public int getTestCaseIndex( TestCase testCase )
	{
		return testCases.indexOf( testCase );
	}

	@Override
	public void release()
	{
		super.release();

		for( WsdlTestCase testCase : testCases )
			testCase.release();

		testSuiteListeners.clear();

		if( setupScriptEngine != null )
			setupScriptEngine.release();

		if( tearDownScriptEngine != null )
			tearDownScriptEngine.release();
	}

	public List<TestCase> getTestCaseList()
	{
		List<TestCase> result = new ArrayList<TestCase>();
		for( WsdlTestCase testCase : testCases )
			result.add( testCase );

		return result;
	}

	public Map<String, TestCase> getTestCases()
	{
		Map<String, TestCase> result = new HashMap<String, TestCase>();
		for( TestCase testCase : testCases )
			result.put( testCase.getName(), testCase );

		return result;
	}

	/**
	 * Moves a testcase by the specified offset, a bit awkward since xmlbeans
	 * doesn't support reordering of arrays, we need to create copies of the
	 * contained XmlObjects
	 * 
	 * @param ix
	 * @param offset
	 */

	public WsdlTestCase moveTestCase( int ix, int offset )
	{
		WsdlTestCase testCase = testCases.get( ix );

		if( offset == 0 )
			return testCase;

		testCases.remove( ix );
		testCases.add( ix + offset, testCase );

		TestCaseConfig[] configs = new TestCaseConfig[testCases.size()];

		for( int c = 0; c < testCases.size(); c++ )
		{
			if( offset > 0 )
			{
				if( c < ix )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c ).copy();
				else if( c < ( ix + offset ) )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c + 1 ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( ix ).copy();
				else
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c ).copy();
			}
			else
			{
				if( c < ix + offset )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( ix ).copy();
				else if( c <= ix )
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c - 1 ).copy();
				else
					configs[c] = ( TestCaseConfig )getConfig().getTestCaseArray( c ).copy();
			}
		}

		getConfig().setTestCaseArray( configs );
		for( int c = 0; c < configs.length; c++ )
		{
			testCases.get( c ).resetConfigOnMove( getConfig().getTestCaseArray( c ) );
		}

		fireTestCaseMoved( testCase, ix, offset );
		return testCase;
	}

	public int getIndexOfTestCase( TestCase testCase )
	{
		return testCases.indexOf( testCase );
	}

	public List<? extends ModelItem> getChildren()
	{
		return getTestCaseList();
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

	public Object runSetupScript( PropertyExpansionContext context ) throws Exception
	{
		String script = getSetupScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( setupScriptEngine == null )
		{
			setupScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			setupScriptEngine.setScript( script );
		}

		if( context == null )
			context = new DefaultPropertyExpansionContext( this );

		setupScriptEngine.setVariable( "context", context );
		setupScriptEngine.setVariable( "testSuite", this );
		setupScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return setupScriptEngine.run();
	}

	public Object runTearDownScript( PropertyExpansionContext context ) throws Exception
	{
		String script = getTearDownScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( tearDownScriptEngine == null )
		{
			tearDownScriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
			tearDownScriptEngine.setScript( script );
		}

		if( context == null )
			context = new DefaultPropertyExpansionContext( this );

		tearDownScriptEngine.setVariable( "context", context );
		tearDownScriptEngine.setVariable( "testSuite", this );
		tearDownScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return tearDownScriptEngine.run();
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

	public void replace( WsdlTestCase testCase, TestCaseConfig newTestCase )
	{

		int ix = testCases.indexOf( testCase );

		testCases.remove( ix );
		try
		{
			fireTestCaseRemoved( testCase );
		}
		finally
		{
			testCase.release();
			getConfig().removeTestCase( ix );
		}

		TestCaseConfig newConfig = ( TestCaseConfig )getConfig().insertNewTestCase( ix ).set( newTestCase ).changeType(
				TestCaseConfig.type );
		testCase = new WsdlTestCase( this, newConfig, false );
		testCases.add( ix, testCase );
		testCase.afterLoad();
		fireTestCaseAdded( testCase );

		resolveImportedTestCase( testCase );
	}

	public void importTestCase( File file )
	{
		TestCaseConfig testCaseNewConfig = null;

		if( !file.exists() )
		{
			UISupport.showErrorMessage( "Error loading test case " );
			return;
		}

		try
		{
			testCaseNewConfig = TestCaseDocumentConfig.Factory.parse( file ).getTestCase();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		if( testCaseNewConfig != null )
		{
			TestCaseConfig newConfig = ( TestCaseConfig )getConfig().addNewTestCase().set( testCaseNewConfig ).changeType(
					TestCaseConfig.type );
			WsdlTestCase newTestCase = new WsdlTestCase( this, newConfig, false );
			ModelSupport.unsetIds( newTestCase );
			newTestCase.afterLoad();
			testCases.add( newTestCase );
			fireTestCaseAdded( newTestCase );

			resolveImportedTestCase( newTestCase );
		}
		else
		{
			UISupport.showErrorMessage( "Not valild test case xml" );
		}
	}

	private void resolveImportedTestCase( WsdlTestCase newTestCase )
	{
		ResolveDialog resolver = new ResolveDialog( "Validate TestCase", "Checks TestCase for inconsistencies", null );
		resolver.setShowOkMessage( false );
		resolver.resolve( newTestCase );
	}

	public void export( File file )
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

	public void afterCopy( WsdlTestSuite oldTestSuite )
	{
		for( WsdlTestCase testCase : testCases )
			testCase.afterCopy( oldTestSuite, null );
	}
}
