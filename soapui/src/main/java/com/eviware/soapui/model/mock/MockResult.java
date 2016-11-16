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

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.io.IOException;

/**
 * Resulting MessageExchange for a request to a MockService
 *
 * @author ole.matzura
 */

public interface MockResult {
    public MockRequest getMockRequest();

    public StringToStringsMap getResponseHeaders();

    public String getResponseContent();

    public MockResponse getMockResponse();

    public MockOperation getMockOperation();

    public ActionList getActions();

    public long getTimeTaken();

    public long getTimestamp();

    public void finish();

    public byte[] getRawResponseData();

    public void addHeader(String name, String value);

    public boolean isCommitted();

    public void setResponseContent(String responseContent);

    public void setContentType(String contentTypeHttpHeader);

    public void writeRawResponseData(byte[] data) throws IOException;
}
