package com.eviware.soapui.model.security;

import javax.swing.JComponent;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ExecutionStrategyHolder;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;

public interface SecurityCheck
{

	public static final String SECURITY_CHECK_REQUEST_RESULT = "SecurityCheckRequestResult";
	public static final String SECURITY_CHECK_RESPONSE_RESULT = "SecurityCheckResponseResult";
	public static final String STATUS_PROPERTY = SecurityCheck.class.getName() + "@status";

	void updateSecurityConfig( SecurityCheckConfig config );

	SecurityCheckResult run( TestStep testStep, SecurityTestRunContext context, SecurityTestRunner securityTestRunner );

	boolean isConfigurable();

	/**
	 * Gets desktop configuration for specific SecurityCheck
	 * 
	 * @param TestStep
	 *           the TestStep to create the config for, could be null for
	 *           HttpMonitor checks
	 * 
	 * @return
	 */
	JComponent getComponent();

	/**
	 * The type of this check
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Checks if this securityCheck is applicable to the specified TestStep
	 * 
	 * @param testStep
	 * @return
	 */
	boolean acceptsTestStep( TestStep testStep );

	TestStep getTestStep();

	void setTestStep( TestStep step );

	Object runTearDownScript( SecurityTestRunner runner, SecurityTestRunContext context ) throws Exception;

	Object runSetupScript( SecurityTestRunner runner, SecurityTestRunContext context ) throws Exception;

	/**
	 * Checks if the test is disabled
	 * 
	 * @return true if disabled
	 */
	boolean isDisabled();

	/**
	 * Disables or Enables the check
	 * 
	 * @param disabled
	 */
	void setDisabled( boolean disabled );

	ExecutionStrategyHolder getExecutionStrategy();

	void setExecutionStrategy( ExecutionStrategyHolder executionStrategyHolder );

	AssertionsSupport getAssertionsSupport();

	String getSetupScript();

	void setSetupScript( String text );

	String getTearDownScript();

	void setTearDownScript( String text );

	// name used in configuration panel
	String getConfigName();

	// description usd in configuration panel
	String getConfigDescription();

	// help url used for configuration panel ( help for this check )
	String getHelpURL();

	String getName();

	/**
	 * Advanced setting panel for configuration
	 * @return
	 */
	JComponent getAdvancedSettingsPanel();

}