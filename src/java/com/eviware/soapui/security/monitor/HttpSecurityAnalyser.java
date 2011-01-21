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

package com.eviware.soapui.security.monitor;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.security.log.JSecurityTestRunLog;

/**
 * An interface that all SecurityChecks that can be used in the security monitor must implement
 * 
 * @author nenad.ristic
 *
 */
public interface HttpSecurityAnalyser {
	/**
	 * Analyse a message exchange
	 * 
	 * @param messageExchange A completed MessageExchange from the HttpMoitor
	 * @param securityTestLog - The log to record the values to.
	 */
	public void analyzeHttpConnection (MessageExchange messageExchange,  JSecurityTestRunLog securityTestLog );
	
	/**
	 * Used to confirm if the check an be applied
	 * 
	 * @return true if the check can be used
	 */
	public boolean canRun();
}
