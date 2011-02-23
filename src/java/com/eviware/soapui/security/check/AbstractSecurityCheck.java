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
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;

/**
 * @author robert
 * 
 */
public abstract class AbstractSecurityCheck extends AbstractWsdlModelItem<SecurityCheckConfig> implements
		XPathReferenceContainer, Assertable, RequestAssertion, ResponseAssertion
{
	public static final String SECURITY_CHECK_REQUEST_RESULT = "SecurityCheckRequestResult";
	public static final String SECURITY_CHECK_RESPONSE_RESULT = "SecurityCheckResponseResult";

	public static final String STATUS_PROPERTY = AbstractSecurityCheck.class.getName() + "@status";

	public static final String SINGLE_REQUEST_STRATEGY = "A single request with all the parameters";
	public static final String SEPARATE_REQUEST_STRATEGY = "Seperate request for each parameter";

	// configuration of specific request modification
	private SecurityCheckConfig config;
	private String startupScript;
	private String tearDownScript;
	private SoapUIScriptEngine scriptEngine;
	private boolean disabled = false;
	private Status status;
	private SecurityCheckConfigPanel contentPanel;
	private SecurityCheckResult securityCheckResult;
	private SecurityCheckRequestResult securityCheckRequestResult;
	private TestStep testStep;
	private AssertionsSupport assertionsSupport;

	private AssertionStatus currentStatus;
	private SecurityCheckedParameterHolder parameterHolder;

	public AbstractSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
		}
		this.testStep = testStep;
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config.getTearDownScript().getStringValue() : "";
		setScriptEngine( SoapUIScriptEngineRegistry.create( this ) );
		if( config.getExecutionStrategy() == null )
			config.setExecutionStrategy( SEPARATE_REQUEST_STRATEGY );

		if( config.getChekedPameters() == null )
			config.addNewChekedPameters();

		setParameterHolder( new SecurityCheckedParameterHolder( this, config.getChekedPameters() ) );
		initAssertions();

	}

	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		this.config = config;

		List<TestAssertionConfig> assertionList = config.getAssertionList();
		for( int c = 0; c < assertionList.size(); c++ )
		{
			assertionsSupport.getAssertionAt( c ).updateConfig( assertionList.get( c ) );
		}

		if( getParameterHolder() != null )
		{
			getParameterHolder().updateConfig( config.getChekedPameters() );
		}
	}

	public SecurityCheckedParameterHolder getParameterHolder()
	{
		return this.parameterHolder;
	}

	private void initAssertions()
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

	/*************************************
	 * START OF NEWLY REFACTORED
	 **************************************/
	/**
	 * Runs the test (internaly calls analyze)
	 * 
	 * @param testStep
	 *           The TestStep that the check will be applied to
	 * @param context
	 *           The context to run the test in
	 * @param securityTestLog
	 *           The security log to write to
	 */
	public SecurityCheckResult run( TestStep testStep, SecurityTestRunContext context )
	{
		securityCheckResult = new SecurityCheckResult( this );

		// setStatus( Status.INITIALIZED );
		runStartupScript( testStep );

		while( hasNext() )
		{
			setSecurityCheckRequestResult( new SecurityCheckRequestResult( this ) );
			execute( testStep, context );
			assertRequest( getSecurityCheckRequestResult().getMessageExchange(), context );
			assertResponse( getSecurityCheckRequestResult().getMessageExchange(), context );
			// add to summary result
			securityCheckResult.addSecurityRequestResult( getSecurityCheckRequestResult() );

		}

		runTearDownScript( testStep );

		return securityCheckResult;
	}

	/**
	 * should be implemented in every particular check it executes one request,
	 * modified by securityCheck if necessary and internally adds messages for
	 * logging to SecurityCheckRequestResult
	 */
	abstract protected void execute( TestStep testStep, SecurityTestRunContext context );

	/**
	 * checks if specific SecurityCheck still has modifications left TODO needs
	 * to be abstract and implemented in every check
	 */
	abstract protected boolean hasNext();

	/*************************************
	 * END OF NEWLY REFACTORED
	 **************************************/

	public boolean isConfigurable()
	{
		return true;
	}

	/**
	 * Gets desktop configuration for specific SecurityCheck
	 * 
	 * @param TestStep
	 *           the TestStep to create the config for, could be null for
	 *           HttpMonitor checks
	 * 
	 * @return
	 */
	public abstract JComponent getComponent();

	/**
	 * The type of this check
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * Checks if this securityCheck is applicable to the specified TestStep
	 * 
	 * @param testStep
	 * @return
	 */
	public abstract boolean acceptsTestStep( TestStep testStep );

	public TestStep getTestStep()
	{
		return testStep;
	}

	public void setTestStep( TestStep step )
	{
		testStep = step;
	}

	// XXX: need to be fixed.
	public void runTearDownScript( TestStep testStep )
	{
		getScriptEngine().setScript( tearDownScript );
		getScriptEngine().setVariable( "testStep", testStep );
		getScriptEngine().setVariable( "log", SoapUI.ensureGroovyLog() );

		try
		{
			getScriptEngine().run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			getScriptEngine().clearVariables();
		}

	}

	public void runStartupScript( TestStep testStep )
	{
		getScriptEngine().setScript( startupScript );
		getScriptEngine().setVariable( "testStep", testStep );
		getScriptEngine().setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			getScriptEngine().run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			getScriptEngine().clearVariables();
		}
	}

	@Override
	public SecurityCheckConfig getConfig()
	{
		return config;
	}

	@Override
	public String getDescription()
	{
		return config.getDescription();
	}

	@Override
	public String getName()
	{
		return config.getName();
	}

	@Override
	public void setName( String arg0 )
	{
		config.setName( arg0 );
	}

	/**
	 * Checks if the test is disabled
	 * 
	 * @return true if disabled
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * Disables or Enables the check
	 * 
	 * @param disabled
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

	public String getExecutionStrategy()
	{
		return config.getExecutionStrategy();
	}

	public void setExecutionStrategy( String strategy )
	{
		config.setExecutionStrategy( strategy );
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

	public XPathReference[] getXPathReferences()
	{
		List<XPathReference> result = new ArrayList<XPathReference>();

		for( SecurityCheckedParameter param : getParameterHolder().getParameterList() )
		{
			TestStep t = getTestStep();
			if( t instanceof WsdlTestRequestStep )
			{
				if( param != null )
					result.add( new XPathReferenceImpl( "SecurityCheck Parameter " + param.getName(),
							( ( WsdlTestRequestStep )t ).getOperation(), true, param, "xPath" ) );
			}
		}

		return result.toArray( new XPathReference[result.size()] );
	}

	private class PropertyChangeNotifier
	{
		private AssertionStatus oldStatus;

		public PropertyChangeNotifier()
		{
			oldStatus = getAssertionStatus();
		}

		public void notifyChange()
		{
			AssertionStatus newStatus = getAssertionStatus();

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

		if( securityCheckResult != null && securityCheckResult.getStatus() == SecurityStatus.OK )
			currentStatus = AssertionStatus.VALID;
		else
			currentStatus = AssertionStatus.FAILED;

		return currentStatus;
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

	@Override
	public AssertionStatus assertRequest( MessageExchange messageExchange, SubmitContext context )
	{
		AssertionStatus result = null;

		try
		{
			PropertyChangeNotifier notifier = new PropertyChangeNotifier();

			if( messageExchange != null )
			{
				context.setProperty( SECURITY_CHECK_REQUEST_RESULT, getSecurityCheckRequestResult() );

				for( WsdlMessageAssertion assertion : assertionsSupport.getAssertionList() )
				{
					result = assertion.assertRequest( messageExchange, context );
					if( result == AssertionStatus.FAILED )
					{
						for( AssertionError error : assertion.getErrors() )
							getSecurityCheckRequestResult().addMessage( error.getMessage() );
						getSecurityCheckRequestResult().setStatus( SecurityStatus.FAILED );
					}
				}

				notifier.notifyChange();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return result;
	}

	public AssertionStatus assertResponse( MessageExchange messageExchange, SubmitContext context )
	{
		AssertionStatus result = null;

		try
		{
			PropertyChangeNotifier notifier = new PropertyChangeNotifier();

			if( messageExchange != null )
			{
				context.setProperty( SECURITY_CHECK_REQUEST_RESULT, getSecurityCheckRequestResult() );

				for( WsdlMessageAssertion assertion : assertionsSupport.getAssertionList() )
				{
					result = assertion.assertResponse( messageExchange, context );
					if( result == AssertionStatus.FAILED )
					{
						for( AssertionError error : assertion.getErrors() )
							getSecurityCheckRequestResult().addMessage( error.getMessage() );
						getSecurityCheckRequestResult().setStatus( SecurityStatus.FAILED );
					}
				}

				notifier.notifyChange();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return result;
	}

	public String getSetupScript()
	{
		if( config.getSetupScript() == null )
			config.addNewSetupScript();
		return config.getSetupScript().getStringValue();
	}

	public void setSetupScript( String text )
	{
		if( config.getSetupScript() == null )
			config.addNewSetupScript();
		config.getSetupScript().setStringValue( text );
	}

	public String getTearDownScript()
	{
		if( config.getTearDownScript() == null )
			config.addNewTearDownScript();
		return config.getTearDownScript().getStringValue();
	}

	public void setTearDownScript( String text )
	{
		if( config.getTearDownScript() == null )
			config.addNewTearDownScript();
		config.getTearDownScript().setStringValue( text );
	}

	// name used in configuration panel
	public abstract String getConfigName();

	// description usd in configuration panel
	public abstract String getConfigDescription();

	// help url used for configuration panel ( help for this check )
	public abstract String getHelpURL();

	protected void setSecurityCheckRequestResult( SecurityCheckRequestResult securityCheckRequestResult )
	{
		this.securityCheckRequestResult = securityCheckRequestResult;
	}

	protected SecurityCheckRequestResult getSecurityCheckRequestResult()
	{
		return securityCheckRequestResult;
	}

	protected void setParameterHolder( SecurityCheckedParameterHolder parameterHolder )
	{
		this.parameterHolder = parameterHolder;
	}

	protected void setScriptEngine( SoapUIScriptEngine scriptEngine )
	{
		this.scriptEngine = scriptEngine;
	}

	protected SoapUIScriptEngine getScriptEngine()
	{
		return scriptEngine;
	}

}
