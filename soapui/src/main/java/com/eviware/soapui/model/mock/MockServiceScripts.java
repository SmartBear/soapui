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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;

public interface MockServiceScripts {
    public Object runStartScript(WsdlMockRunContext mockContext, MockRunner wsdlMockRunner) throws Exception;

    public String getStartScript();

    public void setStartScript(String script);

    public String getStopScript();

    public void setStopScript(String script);

    public Object runStopScript(WsdlMockRunContext mockContext, MockRunner mockRunner) throws Exception;

    public String getOnRequestScript();

    public void setOnRequestScript(String text);

    public Object runOnRequestScript(WsdlMockRunContext context, MockRequest request) throws Exception;

    public String getAfterRequestScript();

    public void setAfterRequestScript(String text);

    public Object runAfterRequestScript(WsdlMockRunContext context, MockResult request) throws Exception;
}
