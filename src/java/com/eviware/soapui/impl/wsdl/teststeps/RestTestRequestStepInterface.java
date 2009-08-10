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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.OperationTestStep;

public interface RestTestRequestStepInterface extends HttpTestRequestStepInterface, OperationTestStep, Assertable
{
	public RestRequestStepConfig getRequestStepConfig();

	public String getService();

	public String getResourcePath();

	public RestMethod getRestMethod();

	public RestResource getResource();
}