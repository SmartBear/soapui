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

package com.eviware.soapui.impl.rest.mock;

import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RestMockResponseTest {

    private RestMockResponse restMockResponse;

    @Before
    public void setUp() throws SoapUIException {
        RestMockAction restMockAction = ModelItemFactory.makeRestMockAction();
        restMockAction.getMockService().setName("REST Mock Service");
        restMockResponse = restMockAction.addNewMockResponse("REST Mock Response");
    }

    @Test
    public void getsCorrectEncodingValue() throws Exception {
        String contentType = "application/atom+xml; charset=UTF-8";
        String[] parameters = contentType.split(";");
        assertThat(restMockResponse.getEncodingValue(parameters), is("UTF-8"));
    }

    @Test
    public void getsEncodingValueFromMultipleParameters() throws Exception {
        String contentType = "application/atom+xml; charset=UTF-8; type=feed";
        String[] parameters = contentType.split(";");
        assertThat(restMockResponse.getEncodingValue(parameters), is("UTF-8"));
    }

    @Test
    public void getsEncodingValueWhenEncodingValueIsLastParam() throws Exception {
        String contentType = "application/atom+xml; type=feed; charset=UTF-8; ";
        String[] parameters = contentType.split(";");
        assertThat(restMockResponse.getEncodingValue(parameters), is("UTF-8"));
    }

    @Test
    public void doesNoGetAnyEncodingValueWhenContentTypeDoesNotHaveEncoding() throws Exception {
        String contentType = "application/atom+xml; type=feed; boo=false";
        String[] parameters = contentType.split(";");
        assertNull(restMockResponse.getEncodingValue(parameters));
    }

}
