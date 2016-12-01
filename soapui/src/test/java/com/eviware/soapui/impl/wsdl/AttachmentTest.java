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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttachmentTest {

    @Test
    public void shouldHaveAttachments() throws Exception {

        String wsdlUrl = AttachmentTest.class.getResource("/attachment-test.wsdl").toString();
        WsdlProject project = new WsdlProject();
        WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project, wsdlUrl, false)[0];

        WsdlOperation operation = iface.getOperationByName("SendClaim");
        WsdlRequest request = operation.addNewRequest("Test");

        request.setRequestContent(operation.createRequest(true));

        System.out.println(request.getRequestContent());

        HttpAttachmentPart[] definedAttachmentParts = request.getDefinedAttachmentParts();

        assertEquals(definedAttachmentParts.length, 4);
        assertEquals(definedAttachmentParts[0].getName(), "ClaimPhoto");
    }
}
