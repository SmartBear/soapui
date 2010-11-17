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
	public void analyzeHttpConnection (MessageExchange messageExchange,  SecurityTestLog securityTestLog );
	
	/**
	 * @return
	 */
	public boolean canRun();
}
