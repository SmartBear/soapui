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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.LoadTestConfig;
import com.eviware.soapui.config.RunTestCaseRunModeTypeConfig;
import com.eviware.soapui.config.RunTestCaseStepConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.RunTestCaseRunModeTypeConfig.Enum;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder.PropertiesStepProperty;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ChooseAnotherTestCase;
import com.eviware.soapui.support.resolver.CreateNewEmptyTestCase;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.RunTestCaseRemoveResolver;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.types.StringToObjectMap;

public class WsdlRunTestCaseTestStep extends WsdlTestStep
{
	public static final String TARGET_TESTCASE = WsdlRunTestCaseTestStep.class.getName() + "@target_testcase";

	private RunTestCaseStepConfig stepConfig;
	private WsdlTestCaseRunner testCaseRunner;
	private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;
	private String currentLabel;
	private WsdlTestCase targetTestCase;
	private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();
	private InternalTestRunListener testRunListener = new InternalTestRunListener();
	private InternalTestPropertyListener testPropertyListener = new InternalTestPropertyListener();
	private Set<TestRunListener> testRunListeners = new HashSet<TestRunListener>();
	private WsdlTestCase runningTestCase;

	public WsdlRunTestCaseTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( config.getConfig() == null )
		{
			stepConfig = ( RunTestCaseStepConfig )config.addNewConfig().changeType( RunTestCaseStepConfig.type );
			stepConfig.addNewProperties();
			stepConfig.addNewReturnProperties();
		}
		else
		{
			stepConfig = ( RunTestCaseStepConfig )config.getConfig().changeType( RunTestCaseStepConfig.type );
		}

		if( stepConfig.getRunMode() == null )
		{
			stepConfig.setRunMode( RunTestCaseRunModeTypeConfig.PARALLELL );
		}

		setIcon( UISupport.createImageIcon( "/run_testcase_step.gif" ) );

		propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder( this, stepConfig.getProperties() );
	}

	/**
	 * We need to check that we are not pointing at testcase in original
	 * testsuite
	 */

	public void afterCopy( WsdlTestSuite oldTestSuite, WsdlTestCase oldTestCase )
	{
		super.afterCopy( oldTestSuite, oldTestCase );

		if( targetTestCase != null && oldTestSuite == targetTestCase.getTestSuite() )
		{
			setTargetTestCase( getTestCase().getTestSuite().getTestCaseByName( targetTestCase.getName() ) );
		}
	}

	@Override
	public void afterLoad()
	{
		setTargetTestCase( findTargetTestCase() );

		super.afterLoad();
	}

	private void syncProperties()
	{
		for( String name : propertyHolderSupport.getPropertyNames() )
		{
			if( !targetTestCase.hasProperty( name ) )
				propertyHolderSupport.removeProperty( name );
		}

		for( String name : targetTestCase.getPropertyNames() )
		{
			if( !propertyHolderSupport.hasProperty( name ) )
				propertyHolderSupport.addProperty( name );
		}
	}

	private WsdlTestCase findTargetTestCase()
	{
		return ModelSupport.findModelItemById( getTestCaseId(), getTestCase().getTestSuite().getProject() );
	}

	public StringList getReturnProperties()
	{
		return new StringList( stepConfig.getReturnProperties().getEntryList() );
	}

	public void setReturnProperties( StringList returnProperties )
	{
		stepConfig.getReturnProperties().setEntryArray( returnProperties.toStringArray() );
	}

	public TestStepResult run( TestCaseRunner testRunner, TestCaseRunContext testRunContext )
	{
		WsdlMessageExchangeTestStepResult result = new WsdlMessageExchangeTestStepResult( this );

		testCaseRunner = null;

		if( targetTestCase != null )
		{
			Enum runMode = getRunMode();

			if( runMode == RunTestCaseRunModeTypeConfig.PARALLELL )
			{
				runningTestCase = createTestCase( targetTestCase );
			}
			else
			{
				runningTestCase = targetTestCase;
				
				TestCaseRunner targetTestRunner = SoapUI.getTestMonitor().getTestRunner( targetTestCase );
				if( targetTestRunner != null && targetTestRunner.getStatus() == TestRunner.Status.RUNNING )
				{
					if( runMode == RunTestCaseRunModeTypeConfig.SINGLETON_AND_FAIL )
					{
						result.setStatus( TestStepStatus.FAILED );
						result.addMessage( "Target TestCase is already running" );
						result.stopTimer();
						runningTestCase = null;
					}
					else
					{
						targetTestRunner.waitUntilFinished();
					}
				}
			}

			if( runningTestCase != null )
			{
				synchronized( runningTestCase )
				{
					for( TestRunListener listener : testRunListeners )
						runningTestCase.addTestRunListener( listener );

					StringList returnProperties = getReturnProperties();
					Map<String, TestProperty> props = getProperties();
					for( String key : props.keySet() )
					{
						if( runningTestCase.hasProperty( key ) && !returnProperties.contains( key ) )
						{
							String value = props.get( key ).getValue();
							runningTestCase.setPropertyValue( key, PropertyExpander.expandProperties( testRunContext, value ) );
						}
					}

					currentLabel = getLabel();
					runningTestCase.addTestRunListener( testRunListener );

					// StringToObjectMap properties = new StringToObjectMap();
					// for( String name : testRunContext.getPropertyNames() )
					// properties.put( name, testRunContext.getProperty( name ));

					result.startTimer();
					testCaseRunner = runningTestCase.run( new StringToObjectMap(), true );
					testCaseRunner.waitUntilFinished();
					result.stopTimer();

					for( String key : returnProperties )
					{
						if( runningTestCase.hasProperty( key ) )
							setPropertyValue( key, runningTestCase.getPropertyValue( key ) );
					}

					// aggregate results
					for( TestStepResult testStepResult : testCaseRunner.getResults() )
					{
						result.addMessage( testStepResult.getTestStep().getName() + " - " + testStepResult.getStatus() + " - " + testStepResult.getTimeTaken());
						for( String msg : testStepResult.getMessages())
						{
							result.addMessage( "- " + msg );
						}

						if( testStepResult instanceof MessageExchangeTestStepResult )
						{
							result.addMessages( ( ( MessageExchangeTestStepResult )testStepResult ).getMessageExchanges() );
						}
					}

					switch( testCaseRunner.getStatus() )
					{
					case CANCELED :
						result.setStatus( TestStepStatus.CANCELED );
						break;
					case FAILED :
						result.setStatus( TestStepStatus.FAILED );
						break;
					case FINISHED :
						result.setStatus( TestStepStatus.OK );
						break;
					default :
						result.setStatus( TestStepStatus.UNKNOWN );
						break;
					}

					for( TestRunListener listener : testRunListeners )
						runningTestCase.removeTestRunListener( listener );

					if( runMode == RunTestCaseRunModeTypeConfig.PARALLELL )
						runningTestCase.release();

					runningTestCase = null;
					testCaseRunner = null;
				}
			}
		}
		else
		{
			result.setStatus( TestStepStatus.FAILED );
			result.addMessage( "Missing testCase in project" );
			result.stopTimer();
		}

		return result;
	}

	@Override
	public String getLabel()
	{
		String name = getName();

		if( testCaseRunner != null )
		{
			name += " - [" + testCaseRunner.getStatus() + "]";
		}

		if( isDisabled() )
			return name + " (disabled)";
		else
			return name;
	}

	@Override
	public boolean cancel()
	{
		if( testCaseRunner != null )
		{
			testCaseRunner.cancel( "Canceled by calling TestCase" );
		}

		return true;
	}

	private String getTestCaseId()
	{
		return stepConfig.getTargetTestCase();
	}

	public void setTargetTestCase( WsdlTestCase testCase )
	{
		if( targetTestCase != null )
		{
			targetTestCase.getTestSuite().removeTestSuiteListener( testSuiteListener );
			targetTestCase.removeTestPropertyListener( testPropertyListener );
		}

		WsdlTestCase oldTestCase = this.targetTestCase;
		this.targetTestCase = testCase;

		if( testCase != null )
		{
			stepConfig.setTargetTestCase( testCase.getId() );

			targetTestCase.getTestSuite().addTestSuiteListener( testSuiteListener );
			targetTestCase.addTestPropertyListener( testPropertyListener );

			syncProperties();
		}

		notifyPropertyChanged( TARGET_TESTCASE, oldTestCase, testCase );
	}

	/**
	 * Creates a copy of the underlying WsdlTestCase with all LoadTests removed
	 * and configured for LoadTesting
	 */

	private WsdlTestCase createTestCase( WsdlTestCase testCase )
	{
		// clone config and remove and loadtests
		testCase.beforeSave();

		try
		{
			TestCaseConfig config = TestCaseConfig.Factory.parse( testCase.getConfig().xmlText() );
			config.setLoadTestArray( new LoadTestConfig[0] );

			// clone entire testCase
			WsdlTestCase wsdlTestCase = testCase.getTestSuite().buildTestCase( config, true );
			wsdlTestCase.afterLoad();
			return wsdlTestCase;
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.addTestPropertyListener( listener );
	}

	public Map<String, TestProperty> getProperties()
	{
		return propertyHolderSupport.getProperties();
	}

	public PropertiesStepProperty getProperty( String name )
	{
		return propertyHolderSupport.getProperty( name );
	}

	public String[] getPropertyNames()
	{
		return propertyHolderSupport.getPropertyNames();
	}

	public List<TestProperty> getPropertyList()
	{
		return propertyHolderSupport.getPropertyList();
	}

	public String getPropertyValue( String name )
	{
		return propertyHolderSupport.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return propertyHolderSupport.hasProperty( name );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		propertyHolderSupport.removeTestPropertyListener( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		propertyHolderSupport.setPropertyValue( name, value );
	}

	private void updateLabelDuringRun()
	{
		notifyPropertyChanged( WsdlTestStep.LABEL_PROPERTY, currentLabel, getLabel() );
		currentLabel = getLabel();
	}

	private final class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		@Override
		public void propertyAdded( String name )
		{
			propertyHolderSupport.addProperty( name );
		}

		@Override
		public void propertyRemoved( String name )
		{
			propertyHolderSupport.removeProperty( name );
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			propertyHolderSupport.renameProperty( oldName, newName );
		}

		@Override
		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			propertyHolderSupport.moveProperty( name, newIndex );
		}
	}

	private final class InternalTestRunListener extends TestRunListenerAdapter
	{
		@Override
		public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			updateLabelDuringRun();
		}

		@Override
		public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
		{
			updateLabelDuringRun();
		}

		@Override
		public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
		{
			updateLabelDuringRun();
		}

		@Override
		public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
		{
			updateLabelDuringRun();
		}
	}

	@Override
	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		stepConfig = ( RunTestCaseStepConfig )config.getConfig().changeType( RunTestCaseStepConfig.type );
		propertyHolderSupport.resetPropertiesConfig( stepConfig.getProperties() );
	}

	@Override
	public void release()
	{
		if( targetTestCase != null )
		{
			targetTestCase.getTestSuite().removeTestSuiteListener( testSuiteListener );
			targetTestCase.removeTestPropertyListener( testPropertyListener );
		}

		super.release();
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		@Override
		public void testCaseRemoved( TestCase testCase )
		{
			setTargetTestCase( findTargetTestCase() );
		}
	}

	public WsdlTestCase getTargetTestCase()
	{
		return targetTestCase;
	}

	public void addTestRunListener( TestRunListener listener )
	{
		testRunListeners.add( listener );
	}

	public void removeTestRunListener( TestRunListener listener )
	{
		testRunListeners.remove( listener );
	}

	public WsdlTestCase getRunningTestCase()
	{
		return runningTestCase;
	}

	public WsdlTestCaseRunner getTestCaseRunner()
	{
		return testCaseRunner;
	}

	public RunTestCaseRunModeTypeConfig.Enum getRunMode()
	{
		return stepConfig.getRunMode();
	}

	public void setRunMode( RunTestCaseRunModeTypeConfig.Enum runMode )
	{
		stepConfig.setRunMode( runMode );
	}

	public TestProperty getPropertyAt( int index )
	{
		return propertyHolderSupport.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return propertyHolderSupport.getPropertyCount();
	}

	@Override
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		if( targetTestCase == null )
		{
			if( context.hasThisModelItem( this, "Missing Test Case", getTestStepTitle() + "/"
					+ stepConfig.getTargetTestCase() ) )
				return;
			context
					.addPathToResolve( this, "Missing Test Case", getTestStepTitle() + "/" + stepConfig.getTargetTestCase() )
					.addResolvers( new RunTestCaseRemoveResolver( this ), new ChooseAnotherTestCase( this ),
							new CreateNewEmptyTestCase( this ) );
		}
		else
		{
			targetTestCase.resolve( context );
			if( context.hasThisModelItem( this, "Missing Test Case", getTestStepTitle() + "/"
					+ stepConfig.getTargetTestCase() ) )
			{
				context.getPath( this, "Missing Test Case", getTestStepTitle() + "/" + stepConfig.getTargetTestCase() )
						.setSolved( true );
			}
		}
	}
}
