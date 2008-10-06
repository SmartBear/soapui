/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

/*
 *  soapUI Pro, copyright (C) 2007-2008 eviware software ab 
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;

public class WsdlTestMockService extends WsdlMockService
{
	private final WsdlMockResponseTestStep mockResponseStep;

	public WsdlTestMockService( WsdlMockResponseTestStep step, MockServiceConfig config )
	{
		super( step.getTestCase().getTestSuite().getProject(), config );
		this.mockResponseStep = step;
	}

	public WsdlMockResponseTestStep getMockResponseStep()
	{
		return mockResponseStep;
	}
}