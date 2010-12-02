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

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.log.SecurityTestLogModel;

/**
 * represents interface for all security checks that are changing requests, so that response can be different and therefore 
 * it must be checked against sensitive information exposure
 * @author nebojsa.tasic
 *
 */

public interface SensitiveInformationCheckable
{
	public void checkForSensitiveInformationExposure(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog);
}
