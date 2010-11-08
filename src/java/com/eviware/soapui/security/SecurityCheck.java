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
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;

public interface SecurityCheck extends ModelItem
{
	// private int order;
	public List<SecurityTestLogEntry> getResults();

	public SecurityCheckConfig getConfig();

	// internaly calles analyze
	public void execute( TestStep testStep );

	// used in monitor,
	public void analyze( TestStep testStep );

	// possibly to be done through registry
	public boolean isMonitorApplicable();
}
