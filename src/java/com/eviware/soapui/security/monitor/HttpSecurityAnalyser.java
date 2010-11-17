package com.eviware.soapui.security.monitor;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.security.log.SecurityTestLog;

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
	public void analyzeHttpConnection (MessageExchange messageExchange,  SecurityTestLog securityTestLog );
	
	/**
	 * Used to confirm if the check an be applied
	 * 
	 * @return true if the check can be used
	 */
	public boolean canRun();
}
