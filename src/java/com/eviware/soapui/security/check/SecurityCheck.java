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
package com.eviware.soapui.security.check;

import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestContext;
import com.eviware.soapui.security.log.SecurityTestLog;
import com.eviware.soapui.security.log.SecurityTestLogEntry;

/**
 * SecurityCheck
 * 
 * @author soapUI team
 */
public abstract class SecurityCheck extends AbstractWsdlModelItem<SecurityCheckConfig>
{
	protected SecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		// TODO Auto-generated constructor stub
	}

	public abstract SecurityCheckConfig getConfig();

	// internaly calls analyze
	public abstract void run( TestStep testStep, SecurityTestContext context, SecurityTestLog securityTestLog );

	// used in monitor,
	public abstract void analyze( TestStep testStep, SecurityTestContext context, SecurityTestLog securityTestLog );

	// possibly to be done through registry
	public abstract boolean isMonitorApplicable();

	public abstract boolean acceptsTestStep( TestStep testStep );

	public abstract boolean isDisabled();

	/**
	 * 
	 * @param disabled
	 */
	public abstract void setDisabled( boolean disabled );

	/**
	 * Gets desktop configuration for specific SecurityCheck
	 * 
	 * @return
	 */
	public abstract JComponent getComponent();

}
