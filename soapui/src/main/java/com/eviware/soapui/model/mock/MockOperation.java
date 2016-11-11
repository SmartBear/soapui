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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.Releasable;
import com.eviware.soapui.model.iface.Operation;

import java.util.List;

/**
 * A MockOperation for mocking an Interfaces Operation and returning a
 * MockResponse
 *
 * @author ole.matzura
 */

public interface MockOperation extends ModelItem, Releasable {
    public MockService getMockService();

    public int getMockResponseCount();

    public MockResponse getMockResponseAt(int index);

    public MockResponse getMockResponseByName(String name);

    public MockResponse addNewMockResponse(String name);

    public Operation getOperation();

    public MockResult getLastMockResult();

    public List<MockResponse> getMockResponses();

    public void removeMockResponse(MockResponse mockResponse);

    /**
     * This is a container used by dispatcher to save script, xpath expressions etc
     *
     * @return script or xpath
     */
    public String getScript();

    /**
     * @param script this is a String that might be needed by the dispatch style used in this mock operation.
     */
    public void setScript(String script);

    String getScriptHelpUrl();
}
