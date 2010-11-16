package com.eviware.soapui.security.monitor;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.security.log.SecurityTestLog;

public interface HttpSecurityAnalyser {
	public void analyzeHttpConnection (MessageExchange messageExchange,  SecurityTestLog securityTestLog );
	
	public boolean canRun();
}
