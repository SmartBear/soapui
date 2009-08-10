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

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.SubmitContext;

public interface HttpTestRequestInterface<T extends AbstractRequestConfig> extends TestRequest, HttpRequestInterface<T>
{
	public static final String RESPONSE_PROPERTY = HttpTestRequestInterface.class.getName() + "@response";
	public static final String STATUS_PROPERTY = HttpTestRequestInterface.class.getName() + "@status";

	public void assertResponse( SubmitContext context );

	public String getResponseContentAsString();

	public void updateConfig( T request );

	public WsdlTestStep getTestStep();

	public WsdlTestCase getTestCase();
}