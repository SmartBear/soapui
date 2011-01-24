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

import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

/**
 * SecurityCheck
 * 
 * @author soapUI team
 */
public abstract class SecurityCheck extends AbstractWsdlModelItem<SecurityCheckConfig>
{
	private final Securable securable;
	private TestStep testStep;

	protected SecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, Securable requestToCheck )
	{
		super( config, parent, icon );
		securable = requestToCheck;
	}

	public abstract SecurityCheckConfig getConfig();

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
	public abstract SecurityCheckRequestResult run( TestStep testStep, SecurityTestRunContext context, SecurityTestLogModel securityTestLog );

	/**
	 * Analyses the specified TestStep
	 * 
	 * @param testStep
	 * @param context
	 * @param securityTestLog
	 * @param securityCheckResult TODO
	 * @return TODO
	 */
	public abstract SecurityCheckRequestResult analyze( TestStep testStep, SecurityTestRunContext context, SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult );

	/**
	 * Checks if this securityCheck is applicable to the specified TestStep
	 * 
	 * @param testStep
	 * @return
	 */
	public abstract boolean acceptsTestStep( TestStep testStep );

	/**
	 * Checks if the test is disabled
	 * 
	 * @return true if disabled
	 */
	public abstract boolean isDisabled();

	/**
	 * Disables or Enables the check
	 * 
	 * @param disabled
	 */
	public abstract void setDisabled( boolean disabled );

	/**
	 * Gets desktop configuration for specific SecurityCheck
	 * 
	 * @param TestStep
	 *           the TestStep to create the config for, could be null for
	 *           HttpMonitor checks
	 * 
	 * @return
	 */
	public abstract SecurityCheckConfigPanel getComponent();

	/**
	 * The type of this check
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * The title of this check
	 * 
	 * @return
	 */
	public abstract String getTitle();

	public Securable getSecurable()
	{
		return securable;
	}

	public boolean configure()
	{
		return true;
	}

	public boolean isConfigurable()
	{
		return true;
	}

	public TestStep getTestStep()
	{
		return testStep;
	}

	public void setTestStep( TestStep step )
	{
		testStep = step;
	}

	public abstract List<String> getParamsToCheck();

	/**
	 * List of log entries from specific check this should reflect success of the
	 * check to be used in progress bar of specific test step
	 * 
	 * @return
	 */
	public abstract List<SecurityTestLogMessageEntry> getSecurityTestLogEntries();

}
