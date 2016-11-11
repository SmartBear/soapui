/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.SubmitContext;

public interface HttpTestRequestInterface<T extends AbstractRequestConfig> extends TestRequest, HttpRequestInterface<T> {
    public static final String RESPONSE_PROPERTY = HttpTestRequestInterface.class.getName() + "@response";
    public static final String STATUS_PROPERTY = HttpTestRequestInterface.class.getName() + "@status";

    public void assertResponse(SubmitContext context);

    public String getResponseContentAsString();

    public void updateConfig(T request);

    public WsdlTestStep getTestStep();

    public WsdlTestCase getTestCase();
}
