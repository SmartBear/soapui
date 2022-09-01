package com.eviware.soapui.impl.support;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AbstractMockRequestTest {

    private static final String ENCRYPTED_VALUE = "<element>ENCRYPTED_VALUE</element>";
    private static final String CLEAR_TEXT_VALUE = "<element>CLEAR_TEXT_VALUE</element>";

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private WsdlMockRunContext context;

    private WsdlMockRequest mockRequest;

    @Before
    public void setUp() throws Exception {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.singletonList("testElement")));
        mockRequest = new WsdlMockRequest(request, response, context);
    }

    @Test
    public void testRefreshRequestXmlObject_whenContentIsNotEmpty_thenReplaceRequestXmlObject() throws XmlException {
        mockRequest.setRequestContent(CLEAR_TEXT_VALUE);
        mockRequest.setRequestXmlObject(createEncryptedXmlObject());

        mockRequest.refreshRequestXmlObject();

        XmlObject requestXmlObject = mockRequest.getRequestXmlObject();
        assertEquals(requestXmlObject.toString(), CLEAR_TEXT_VALUE);
    }

    @Test
    public void testRefreshRequestXmlObject_whenContentIsEmpty_thenNotReplaceRequestXmlObject() throws XmlException {
        mockRequest.setRequestContent(StringUtils.EMPTY);
        mockRequest.setRequestXmlObject(createEncryptedXmlObject());

        mockRequest.refreshRequestXmlObject();

        XmlObject requestXmlObject = mockRequest.getRequestXmlObject();
        assertEquals(requestXmlObject.toString(), ENCRYPTED_VALUE);
    }

    private XmlObject createEncryptedXmlObject() throws XmlException {
        return XmlUtils.createXmlObject(ENCRYPTED_VALUE);
    }

}