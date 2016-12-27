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

package com.eviware.soapui.support.editor.inspectors.httpheaders;

import com.eviware.soapui.impl.support.HasHelpUrl;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.types.StringToStringsMap;

public class MockResponseHeadersModel extends HttpHeadersInspectorModel.AbstractHeadersModel<MockResponse> implements HasHelpUrl {
    public MockResponseHeadersModel(MockResponse mockResponse) {
        super(false, mockResponse, WsdlMockResponse.HEADERS_PROPERTY);
    }

    public StringToStringsMap getHeaders() {
        return getModelItem().getResponseHeaders();
    }

    public void setHeaders(StringToStringsMap headers) {
        getModelItem().setResponseHeaders(headers);
    }

    @Override
    public String getHelpUrl() {
        return HelpUrls.REST_MOCK_RESPONSE_EDITOR_HEADER;
    }

}
