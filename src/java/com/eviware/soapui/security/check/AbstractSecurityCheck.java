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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ExecutionStrategyHolder;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.result.SecurityCheckRequestResult;
import com.eviware.soapui.security.result.SecurityCheckResult;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.support.FailedSecurityMessageExchange;
import com.eviware.soapui.security.support.SecurityTestRunListener;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

/**
 * @author robert
 * 
 */
public abstract class AbstractSecurityCheck extends AbstractWsdlModelItem<SecurityCheckConfig> implements
		ResponseAssertion, SecurityCheck// , RequestAssertion
{
	// configuration of specific request modification
	// private SecurityCheckConfig config;
	private boolean disabled = false;
	private SecurityCheckResult securityCheckResult;
	private SecurityCheckRequestResult securityCheckRequestResult;
	private TestStep testStep;
	protected AssertionsSupport assertionsSupport;

	private AssertionStatus currentStatus;
	private SoapUIScriptEngine setupScriptEngine;
	private SoapUIScriptEngine tearDownScriptEngine;
	private ExecutionStrategyHolder executionStrategy;
	private TestStep originalTestStepClone;
	private SecurityTestRunListener[] securityTestListeners = new SecurityTestRunListener[0];

	public AbstractSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			setConfig( config );
		}

		this.testStep = testStep;

		if( config.getExecutionStrategy() == null )
		{
			config.addNewExecutionStrategy();
			config.getExecutionStrategy().setStrategy( StrategyTypeConfig.ONE_BY_ONE );
			config.getExecutionStrategy().setDelay( 100 );
		}
		else if( config.getExecutionStrategy().getStrategy() == null )
		{
			config.getExecutionStrategy().setStrategy( StrategyTypeConfig.ONE_BY_ONE );
			config.getExecutionStrategy().setDelay( 100 );
		}

		/*
		 * if security check have no strategy ( like large attahments, set its
		 * value to StrategyTypeConfig.NO_STRATEGY.
		 */
		setExecutionStrategy( new ExecutionStrategyHolder( config.getExecutionStrategy() ) );

		if( config.getCheckedPameters() == null )
			config.addNewCheckedPameters();

		initAssertions();
	}

	@Override
	public void copyConfig( SecurityCheckConfig config )
	{
		super.setConfig( config );
		getConfig().setType( config.getType() );
		getConfig().setName( config.getName() );
		getConfig().setConfig( config.getConfig() );
		getConfig().setSetupScript( config.getSetupScript() );
		getConfig().setTearDownScript( config.getTearDownScript() );
		getConfig().setTestStep( config.getTestStep() );

		TestAssertionConfig[] assertions = config.getAssertionList().toArray( new TestAssertionConfig[0] );
		getConfig().setAssertionArray( assertions );
		initAssertions();

		getConfig().setExecutionStrategy( config.getExecutionStrategy() );
		setExecutionStrategy( new ExecutionStrategyHolder( config.getExecutionStrategy() ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#updateSecurityConfig(com
	 * .eviware.soapui.config.SecurityCheckConfig)
	 */
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		setConfig( config );

		assertionsSupport.refresh();

		if( executionStrategy != null && config.getExecutionStrategy() != null )
		{
			executionStrategy.updateConfig( config.getExecutionStrategy() );
		}
	}

	protected void initAssertions()
	{
		assertionsSupport = new AssertionsSupport( this, new AssertableConfig()
		{
			public TestAssertionConfig addNewAssertion()
			{
				return getConfig().addNewAssertion();
			}

			public List<TestAssertionConfig> getAssertionList()
			{
				return getConfig().getAssertionList();
			}

			public void removeAssertion( int ix )
			{
				getConfig().removeAssertion( ix );
			}

			public TestAssertionConfig insertAssertion( TestAssertionConfig source, int ix )
			{
				TestAssertionConfig conf = getConfig().insertNewAssertion( ix );
				conf.set( source );
				return conf;
			}
		} );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#run(com.eviware.soapui
	 * .model.testsuite.TestStep,
	 * com.eviware.soapui.security.SecurityTestRunContext,
	 * com.eviware.soapui.security.SecurityTestRunner)
	 */
	public SecurityCheckResult run( TestStep testStep, SecurityTestRunContext context,
			SecurityTestRunner securityTestRunner )
	{
		securityCheckResult = new SecurityCheckResult( this );
		securityTestListeners = ( ( SecurityTest )getParent() ).getSecurityTestRunListeners();

		// setStatus( Status.INITIALIZED );
		try
		{
			runSetupScript( securityTestRunner, context );
		}
		catch( Exception e )
		{
			SoapUI.log.error( "Exception during Test Execution", e );

			// need fix
			securityCheckResult.setStatus( ResultStatus.FAILED );

		}
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		while( hasNext( testStep, context ) )
		{
			if( ( ( SecurityTestRunnerImpl )securityTestRunner ).isCanceled() )
			{
				// if( securityCheckResult.getStatus().equals( ResultStatus.OK )
				// || securityCheckResult.getStatus().equals( ResultStatus.FAILED )
				// )
				// {
				// securityCheckResult.setExecutionProgressStatus(
				// securityCheckResult.getStatus() );
				// }
				securityCheckResult.setStatus( ResultStatus.CANCELED );
				clear();
				return securityCheckResult;
			}
			securityCheckRequestResult = new SecurityCheckRequestResult( this );
			securityCheckRequestResult.startTimer();
			originalTestStepClone = ( ( SecurityTestRunnerImpl )securityTestRunner )
					.cloneForSecurityCheck( ( WsdlTestStep )this.testStep );
			execute( securityTestRunner, originalTestStepClone, context );
			notifier.notifyChange();
			securityCheckRequestResult.stopTimer();
			// assertRequest( getSecurityCheckRequestResult().getMessageExchange(),
			// context );
			assertResponse( getSecurityCheckRequestResult().getMessageExchange(), context );
			// add to summary result
			securityCheckResult.addSecurityRequestResult( getSecurityCheckRequestResult() );
			for( int i = 0; i < securityTestListeners.length; i++ )
			{
				securityTestListeners[i].afterSecurityCheckRequest( ( SecurityTestRunnerImpl )securityTestRunner, context,
						securityCheckRequestResult );
			}

			try
			{
				Thread.sleep( getExecutionStrategy().getDelay() );
			}
			catch( InterruptedException e )
			{
				SoapUI.logError( e, "Security Check Request Delay Interrupted!" );
			}
		}

		try
		{
			runTearDownScript( securityTestRunner, context );
		}
		catch( Exception e )
		{
			SoapUI.log.error( "Exception during Test Execution", e );

			// need fix
			securityCheckResult.setStatus( ResultStatus.FAILED );

		}
		return securityCheckResult;
	}

	protected void clear()
	{

	}

	/**
	 * should be implemented in every particular check it executes one request,
	 * modified by securityCheck if necessary and internally adds messages for
	 * logging to SecurityCheckRequestResult
	 */
	abstract protected void execute( SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context );

	/**
	 * checks if specific SecurityCheck still has modifications left
	 * 
	 * @param testStep2
	 * @param context
	 */
	abstract protected boolean hasNext( TestStep testStep2, SecurityTestRunContext context );

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#isConfigurable()
	 */

	public boolean isConfigurable()
	{
		return true;
	}

	/**
	 * Overide if Security Check have Optional component
	 */
	@Override
	public JComponent getComponent()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getType()
	 */
	public abstract String getType();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getTestStep()
	 */
	public TestStep getTestStep()
	{
		return testStep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#setTestStep(com.eviware
	 * .soapui.model.testsuite.TestStep)
	 */
	public void setTestStep( TestStep step )
	{
		testStep = step;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#runTearDownScript(com.
	 * eviware.soapui.security.SecurityTestRunner,
	 * com.eviware.soapui.security.SecurityTestRunContext)
	 */
	public Object runTearDownScript( SecurityTestRunner runner, SecurityTestRunContext context ) throws Exception
	{
		String script = getTearDownScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( tearDownScriptEngine == null )
		{
			tearDownScriptEngine = SoapUIScriptEngineRegistry.create( this );
			tearDownScriptEngine.setScript( script );
		}

		tearDownScriptEngine.setVariable( "context", context );
		tearDownScriptEngine.setVariable( "testCase", this );
		tearDownScriptEngine.setVariable( "testRunner", runner );
		tearDownScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return tearDownScriptEngine.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#runSetupScript(com.eviware
	 * .soapui.security.SecurityTestRunner,
	 * com.eviware.soapui.security.SecurityTestRunContext)
	 */
	public Object runSetupScript( SecurityTestRunner runner, SecurityTestRunContext context ) throws Exception
	{
		String script = getSetupScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( setupScriptEngine == null )
		{
			setupScriptEngine = SoapUIScriptEngineRegistry.create( this );
			setupScriptEngine.setScript( script );
		}

		setupScriptEngine.setVariable( "securityCheck", this );
		setupScriptEngine.setVariable( "context", context );
		setupScriptEngine.setVariable( "securityRunner", runner );
		setupScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return setupScriptEngine.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#isDisabled()
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#setDisabled(boolean)
	 */
	public void setDisabled( boolean disabled )
	{
		this.disabled = disabled;

	}

	public static boolean isSecurable( TestStep testStep )
	{
		if( testStep != null && testStep instanceof Securable )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#getExecutionStrategy()
	 */
	public ExecutionStrategyHolder getExecutionStrategy()
	{
		return this.executionStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#setExecutionStrategy(com
	 * .eviware.soapui.security.ExecutionStrategyHolder)
	 */
	public void setExecutionStrategy( ExecutionStrategyHolder executionStrategyHolder )
	{
		this.executionStrategy = executionStrategyHolder;
	}

	protected TestRequest getOriginalResult( SecurityTestRunnerImpl securityRunner, TestStep testStep )
	{
		testStep.run( securityRunner, securityRunner.getRunContext() );

		return getRequest( testStep );
	}

	protected TestRequest getRequest( TestStep testStep )
	{
		if( testStep instanceof SamplerTestStep )
		{
			return ( ( SamplerTestStep )testStep ).getTestRequest();
		}
		return null;
	}

	// private class PropertyChangeNotifier
	// {
	// private AssertionStatus oldStatus;
	//
	// public PropertyChangeNotifier()
	// {
	// oldStatus = getAssertionStatus();
	// }
	//
	// public void notifyChange()
	// {
	// AssertionStatus newStatus = getAssertionStatus();
	//
	// if( oldStatus != newStatus )
	// notifyPropertyChanged( STATUS_PROPERTY, oldStatus, newStatus );
	//
	// oldStatus = newStatus;
	// }
	// }
	private class PropertyChangeNotifier
	{
		private ResultStatus oldStatus;

		public PropertyChangeNotifier()
		{
			oldStatus = getSecurityStatus();
		}

		public void notifyChange()
		{
			ResultStatus newStatus = getSecurityStatus();

			if( oldStatus != newStatus )
				notifyPropertyChanged( STATUS_PROPERTY, oldStatus, newStatus );

			oldStatus = newStatus;
		}
	}

	@Override
	public TestAssertion addAssertion( String label )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();
		try
		{
			WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion( label );
			if( assertion == null )
				return null;

			if( getAssertableContent() != null )
			{
				assertRequests( assertion );
				assertResponses( assertion );
				notifier.notifyChange();
			}

			return assertion;
		}

		catch( Exception e )
		{
			SoapUI.logError( e );
			return null;
		}
	}

	/**
	 * @param assertion
	 *           run all responses against this assertion
	 */
	private void assertResponses( WsdlMessageAssertion assertion )
	{
		if( securityCheckResult != null )
			for( SecurityCheckRequestResult result : securityCheckResult.getSecurityRequestResultList() )
			{
				assertion.assertResponse( result.getMessageExchange(), new WsdlSubmitContext( testStep ) );
			}
	}

	/**
	 * @param assertion
	 *           run all request against this assertion
	 */
	private void assertRequests( WsdlMessageAssertion assertion )
	{
		if( securityCheckResult != null )
			for( SecurityCheckRequestResult result : securityCheckResult.getSecurityRequestResultList() )
			{
				assertion.assertRequest( result.getMessageExchange(), new WsdlSubmitContext( testStep ) );
			}
	}

	@Override
	public void removeAssertion( TestAssertion assertion )
	{
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		try
		{
			assertionsSupport.removeAssertion( ( WsdlMessageAssertion )assertion );

		}
		finally
		{
			( ( WsdlMessageAssertion )assertion ).release();
			notifier.notifyChange();
		}
	}

	@Override
	public TestAssertion moveAssertion( int ix, int offset )
	{
		WsdlMessageAssertion assertion = getAssertionAt( ix );
		PropertyChangeNotifier notifier = new PropertyChangeNotifier();

		try
		{
			return assertionsSupport.moveAssertion( ix, offset );
		}
		finally
		{
			( ( WsdlMessageAssertion )assertion ).release();
			notifier.notifyChange();
		}
	}

	@Override
	public WsdlMessageAssertion getAssertionAt( int c )
	{
		return assertionsSupport.getAssertionAt( c );
	}

	@Override
	public void addAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.addAssertionsListener( listener );
	}

	@Override
	public void removeAssertionsListener( AssertionsListener listener )
	{
		assertionsSupport.removeAssertionsListener( listener );
	}

	@Override
	public int getAssertionCount()
	{
		return assertionsSupport.getAssertionCount();
	}

	@Override
	public AssertionStatus getAssertionStatus()
	{
		int cnt = getAssertionCount();
		if( cnt == 0 )
			return currentStatus;

		if( securityCheckResult != null && securityCheckResult.getStatus() == ResultStatus.OK )
			currentStatus = AssertionStatus.VALID;
		else
			currentStatus = AssertionStatus.FAILED;

		return currentStatus;
	}

	public ResultStatus getSecurityStatus()
	{
		return securityCheckResult != null ? securityCheckResult.getStatus() : ResultStatus.UNKNOWN;
	}

	@Override
	public String getAssertableContent()
	{
		if( testStep instanceof Assertable )
			return ( ( Assertable )testStep ).getAssertableContent();

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.model.testsuite.Assertable#getAssertableType()
	 * 
	 * Decided to go with assertions on request and response so we can implement
	 * "men in the middle" attacks using monitor.
	 */
	@Override
	public AssertableType getAssertableType()
	{
		return AssertableType.BOTH;
	}

	@Override
	public TestAssertion getAssertionByName( String name )
	{
		return assertionsSupport.getAssertionByName( name );
	}

	@Override
	public List<TestAssertion> getAssertionList()
	{
		return new ArrayList<TestAssertion>( assertionsSupport.getAssertionList() );
	}

	@Override
	public Map<String, TestAssertion> getAssertions()
	{
		return assertionsSupport.getAssertions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#getAssertionsSupport()
	 */
	public AssertionsSupport getAssertionsSupport()
	{
		return assertionsSupport;
	}

	@Override
	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return assertionsSupport.cloneAssertion( source, name );
	}

	@Override
	public String getDefaultAssertableContent()
	{
		if( testStep instanceof Assertable )
			return ( ( Assertable )testStep ).getDefaultAssertableContent();

		return null;
	}

	@Override
	public Interface getInterface()
	{
		if( testStep instanceof WsdlTestRequest )
			return ( ( WsdlTestRequest )testStep ).getInterface();

		return null;
	}

	@Override
	public ModelItem getModelItem()
	{
		return this;
	}

	// @Override
	// public AssertionStatus assertRequest( MessageExchange messageExchange,
	// SubmitContext context )
	// {
	// AssertionStatus result = null;
	//
	// try
	// {
	// PropertyChangeNotifier notifier = new PropertyChangeNotifier();
	//
	// if( messageExchange != null )
	// {
	// context.setProperty( SECURITY_CHECK_REQUEST_RESULT,
	// getSecurityCheckRequestResult() );
	//
	// for( WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()
	// )
	// {
	// result = assertion.assertRequest( messageExchange, context );
	// setStatus( result, assertion );
	// }
	//
	// notifier.notifyChange();
	// }
	// }
	// catch( Exception e )
	// {
	// e.printStackTrace();
	// }
	// return result;
	// }

	public AssertionStatus assertResponse( MessageExchange messageExchange, SubmitContext context )
	{
		AssertionStatus finalResult = null;

		try
		{
			PropertyChangeNotifier notifier = new PropertyChangeNotifier();

			if( messageExchange != null )
			{
				context.setProperty( SECURITY_CHECK_REQUEST_RESULT, getSecurityCheckRequestResult() );

				for( WsdlMessageAssertion assertion : assertionsSupport.getAssertionList() )
				{
					AssertionStatus currentResult = assertion.assertResponse( messageExchange, context );
					updateMessages( currentResult, assertion );

					if( finalResult == null || finalResult != AssertionStatus.FAILED )
					{
						finalResult = currentResult;
					}
				}

				setStatus( finalResult );

				notifier.notifyChange();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return finalResult;
	}

	/**
	 * Sets SecurityCheckStatus based on the status of all assertions added
	 * 
	 * @param result
	 * @param assertion
	 */
	private void setStatus( AssertionStatus result )
	{
		if( result == AssertionStatus.FAILED )
		{
			getSecurityCheckRequestResult().setStatus( ResultStatus.FAILED );
		}
		else if( result == AssertionStatus.VALID )
		{
			getSecurityCheckRequestResult().setStatus( ResultStatus.OK );

		}
		else if( result == AssertionStatus.UNKNOWN )
		{
			getSecurityCheckRequestResult().setStatus( ResultStatus.UNKNOWN );
		}
	}

	private void updateMessages( AssertionStatus result, WsdlMessageAssertion assertion )
	{
		if( result == AssertionStatus.FAILED )
		{
			for( AssertionError error : assertion.getErrors() )
				getSecurityCheckRequestResult().addMessage( error.getMessage() );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getSetupScript()
	 */
	public String getSetupScript()
	{
		if( getConfig().getSetupScript() == null )
			getConfig().addNewSetupScript();
		return getConfig().getSetupScript().getStringValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#setSetupScript(java.lang
	 * .String)
	 */
	public void setSetupScript( String text )
	{
		if( getConfig().getSetupScript() == null )
			getConfig().addNewSetupScript();
		getConfig().getSetupScript().setStringValue( text );

		if( setupScriptEngine != null )
			setupScriptEngine.setScript( text );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getTearDownScript()
	 */
	public String getTearDownScript()
	{
		if( getConfig().getTearDownScript() == null )
			getConfig().addNewTearDownScript();
		return getConfig().getTearDownScript().getStringValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#setTearDownScript(java
	 * .lang.String)
	 */
	public void setTearDownScript( String text )
	{
		if( getConfig().getTearDownScript() == null )
			getConfig().addNewTearDownScript();
		getConfig().getTearDownScript().setStringValue( text );

		if( tearDownScriptEngine != null )
			tearDownScriptEngine.setScript( text );
	}

	// name used in configuration panel
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getConfigName()
	 */
	public abstract String getConfigName();

	// description usd in configuration panel
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eviware.soapui.security.check.SecurityCheck#getConfigDescription()
	 */
	public abstract String getConfigDescription();

	// help url used for configuration panel ( help for this check )
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eviware.soapui.security.check.SecurityCheck#getHelpURL()
	 */
	public abstract String getHelpURL();

	private void setSecurityCheckRequestResult( SecurityCheckRequestResult securityCheckRequestResult )
	{
		this.securityCheckRequestResult = securityCheckRequestResult;
	}

	protected SecurityCheckRequestResult getSecurityCheckRequestResult()
	{
		return securityCheckRequestResult;
	}

	/**
	 * Overide if Security Check needs advanced settings
	 */
	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		return null;
	}

	@Override
	public SecurityCheckResult getSecurityCheckResult()
	{
		return securityCheckResult;
	}

	/**
	 * @param message
	 * @param testStep
	 */
	protected void reportSecurityCheckException( String message )
	{
		getSecurityCheckRequestResult().setMessageExchange( new FailedSecurityMessageExchange() );
		getSecurityCheckRequestResult().setStatus( ResultStatus.FAILED );
		getSecurityCheckRequestResult().addMessage( message );
	}

	@Override
	public void addWsdlAssertion( String assertionLabel )
	{
		assertionsSupport.addWsdlAssertion( assertionLabel );
	}
}
