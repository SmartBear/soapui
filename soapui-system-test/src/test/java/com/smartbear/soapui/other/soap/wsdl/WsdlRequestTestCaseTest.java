/*
 * Copyright 2004-2014 SmartBear Software
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

package com.smartbear.soapui.other.soap.wsdl;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.*;
import com.eviware.soapui.model.iface.Response;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.jetty.JettyTestCaseBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class WsdlRequestTestCaseTest extends JettyTestCaseBase {

    @Test
    public void testRequest() throws Exception {
        replaceInFile("wsdls/test1/TestService.wsdl", "8082", "" + getPort());
        replaceInFile("wsdls/test1/TestService.wsdl", "www.eviware.com", "localhost:" + getPort());

        // create new project
        WsdlProject project = new WsdlProject();

        // import amazon wsdl
        WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project, "http://localhost:" + getPort() + "/wsdls/test1/TestService.wsdl",
                true)[0];

        // get "Help" operation
        WsdlOperation operation = (WsdlOperation) iface.getOperationByName("GetPage");

        // create a new empty request for that operation
        WsdlRequest request = operation.addNewRequest("My request");

        // generate the request content from the schema
        request.setRequestContent(operation.createRequest(true));

        // submit the request
        WsdlSubmit submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(request), false);

        // wait for the response
        Response response = submit.getResponse();

        // print the response
        String content = response.getContentAsString();
        // System.out.println( content );
        assertNotNull(content);
    }
}