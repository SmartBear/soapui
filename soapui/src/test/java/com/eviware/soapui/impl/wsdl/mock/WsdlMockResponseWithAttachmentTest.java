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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.MessageXmlObject;
import com.eviware.soapui.impl.wsdl.support.MessageXmlPart;
import com.eviware.soapui.impl.wsdl.support.MockFileAttachment;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import java.io.File;
import java.io.IOException;

import static com.eviware.soapui.utils.MockedServlet.mockHttpServletRequest;
import static com.eviware.soapui.utils.MockedServlet.mockHttpServletResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsdlMockResponseWithAttachmentTest {

    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;

    @Before
    public void setUp() throws IOException {
        servletRequest = mockHttpServletRequest();
        servletResponse = mockHttpServletResponse();
    }

    @Test
    public void shouldWriteAttachements() throws Exception {
        WsdlMockResponse mockResponse = createWsdlMockResponse();
        MockResult mockResult = createMockResult();
        String content = "<content>piles of content</content>";
        mockResponse.writeResponse(mockResult, content);
    }

    public WsdlMockResponse createWsdlMockResponse() throws Exception {
        WsdlMockOperation mockOperation = createWsdlMockOperation();
        MockResponseConfig responseConfig = MockResponseConfig.Factory.newInstance();
        WsdlMockResponse mockResponse = new MockedWsdlMockResponse(mockOperation, responseConfig);

        addAttachment(mockResponse);
        return mockResponse;
    }

    public void addAttachment(WsdlMockResponse mockResponse) throws IOException {
        Attachment attachment = new MockFileAttachment(File.createTempFile("attach", "file"), false, mockResponse);
        mockResponse.addAttachment(attachment);
    }

    public WsdlMockOperation createWsdlMockOperation() throws Exception {
        WsdlMockOperation mockOperation = ModelItemFactory.makeWsdlMockOperation();
        mockOperation.setOperation(createWsdlOperation());
        return mockOperation;
    }

    public WsdlOperation createWsdlOperation() throws Exception {
        WsdlOperation operation = mock(WsdlOperation.class);

        BindingOperation bindingOperation = mock(BindingOperation.class);
        when(operation.getBindingOperation()).thenReturn(bindingOperation);

        WsdlInterface wsdlInterface = createWsdlInterface();
        when(operation.getInterface()).thenReturn(wsdlInterface);
        return operation;
    }

    public WsdlInterface createWsdlInterface() throws Exception {
        WsdlInterface wsdlInterface = mock(WsdlInterface.class);
        when(wsdlInterface.getSoapVersion()).thenReturn(SoapVersion.Soap12);

        WsdlContext wsdlContext = createWsdlContext();
        when(wsdlInterface.getWsdlContext()).thenReturn(wsdlContext);

        return wsdlInterface;
    }

    public WsdlContext createWsdlContext() throws Exception {
        WsdlContext wsdlContext = mock(WsdlContext.class);
        when(wsdlContext.hasSchemaTypes()).thenReturn(true);

        Definition definition = mock(Definition.class);
        when(wsdlContext.getDefinition()).thenReturn(definition);

        return wsdlContext;
    }

    public MockResult createMockResult() throws Exception {
        WsdlMockRunContext runContext = mock(WsdlMockRunContext.class);
        return new WsdlMockResult(createMockRequest(runContext));
    }

    public WsdlMockRequest createMockRequest(WsdlMockRunContext runContext) throws Exception {
        return new WsdlMockRequest(servletRequest, servletResponse, runContext);
    }
}

class MockedWsdlMockResponse extends WsdlMockResponse {

    public MockedWsdlMockResponse(WsdlMockOperation operation, MockResponseConfig config) {
        super(operation, config);
    }

    public MessageXmlObject createMessageXmlObject(String responseContent, WsdlOperation wsdlOperation) {
        try {
            MessageXmlPart onePart = mock(MessageXmlPart.class);
            MessageXmlPart[] messageXmlParts = new MessageXmlPart[]{onePart};

            MessageXmlObject messageXmlObject = mock(MessageXmlObject.class);
            when(messageXmlObject.getMessageParts()).thenReturn(messageXmlParts);
            when(messageXmlObject.getMessageContent()).thenReturn(responseContent);
            return messageXmlObject;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean prepareMessagePart(MimeMultipart mp, StringToStringMap contentIds, MessageXmlPart requestPart) throws Exception {
        return true;
    }
}
