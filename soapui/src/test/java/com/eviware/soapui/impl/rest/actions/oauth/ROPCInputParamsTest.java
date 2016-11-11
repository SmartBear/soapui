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

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.support.SoapUIException;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class ROPCInputParamsTest {
    OAuthClientRequest clientRequest;
    private OAuth2TokenExtractor oAuth2TokenExtractor;
    private OAuth2Parameters parameters;

    @Before
    public void setUp() throws SoapUIException {
        parameters = new OAuth2Parameters(OAuth2TestUtils.getOAuthProfileForROPC());
        oAuth2TokenExtractor = new OAuth2TokenExtractor();
    }

    @Test
    public void testParams (){
        try {
            clientRequest = oAuth2TokenExtractor.getClientRequestForROPC(parameters);
        } catch (OAuthSystemException e) {
            Assert.assertTrue("Failed to extract client request for Resource owner password credentials grant flow", false);
            e.printStackTrace();
        }

        HashMap<String, Integer> fields = new HashMap<>();
        fields.put("username", 0);
        fields.put("password", 0);
        fields.put("client_secret", 0);
        fields.put("client_id", 0);
        fields.put("grant_type", 0);
        fields.put("scope", 0);

        String[] bodyContent = clientRequest.getBody().split("\\&");
        assertTrue("Incorrect number of params in body message", (bodyContent.length == 5) || (bodyContent.length == 6)/*if Scope exists*/);
        for (String mes: bodyContent){
            String [] values = mes.split("=");
            assertTrue("Unknown body param " + values[0], fields.containsKey(values[0]));
            assertTrue("Too many the same params \"" + values[0] + "\"", fields.get(values[0]) < 1 && fields.get(values[0]) >= 0);
            fields.put(values[0], fields.get(values[0]) + 1);
            assertTrue("Resource owner password credentials grant flow incorrect param " + values[0] + " has value " + values[1], values[1].length() > 0);
        }
    }
}
