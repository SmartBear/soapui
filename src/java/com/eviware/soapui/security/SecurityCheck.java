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
package com.eviware.soapui.security;

import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.log.SecurityTestLogEntry;

/**
 * SecurityCheck
 * 
 * @author soapUI team
 */
public abstract class SecurityCheck extends AbstractModelItem
{
	// private int order;
	public abstract List<SecurityTestLogEntry> getResults();

	public abstract SecurityCheckConfig getConfig();

	// internaly calles analyze
	public abstract void run( TestStep testStep );

	// used in monitor,
	public abstract void analyze( TestStep testStep );

	// possibly to be done through registry
	public abstract boolean isMonitorApplicable();
}
