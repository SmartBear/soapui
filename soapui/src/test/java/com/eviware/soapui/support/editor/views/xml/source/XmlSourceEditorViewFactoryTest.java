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

package com.eviware.soapui.support.editor.views.xml.source;

import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.MockResponseXmlDocument;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class XmlSourceEditorViewFactoryTest {

    XmlSourceEditorViewFactory xmlSourceEditorViewFactory;
    XmlEditor<MockResponseXmlDocument> editor;
    MockResponse mockResponse;

    @Before
    public void setUp() throws Exception {
        xmlSourceEditorViewFactory = new XmlSourceEditorViewFactory();

        editor = (XmlEditor<MockResponseXmlDocument>) mock(XmlEditor.class);
    }

    @Test
    public void testCreateRequestEditorViewForWsdlMockResponse() throws Exception {
        mockResponse = mock(WsdlMockResponse.class);
        EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createRequestEditorView(editor, mockResponse);
        assertEditorView(requestEditorView);
    }

    @Test
    public void testCreateRequestEditorViewForRestMockResponse() throws Exception {
        mockResponse = mock(RestMockResponse.class);
        EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createRequestEditorView(editor, mockResponse);
        assertEditorView(requestEditorView);
    }

    @Test
    public void testCreateResponseEditorViewForWsdlMockResponse() throws Exception {
        mockResponse = mock(WsdlMockResponse.class);
        EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createResponseEditorView(editor, mockResponse);
        assertEditorView(requestEditorView);
    }

    @Test
    public void testCreateResponseEditorViewForRestMockResponse() throws Exception {
        mockResponse = mock(RestMockResponse.class);
        EditorView<?> responseEditorView = xmlSourceEditorViewFactory.createResponseEditorView(editor, mockResponse);
        assertEditorView(responseEditorView, "Editor");
    }

    private void assertEditorView(EditorView<?> requestEditorView) {
        assertEditorView(requestEditorView, "XML");
    }

    private void assertEditorView(EditorView<?> requestEditorView, String xml) {
        assertNotNull(requestEditorView);
        assertEquals("Source", requestEditorView.getViewId());
        assertEquals(xml, requestEditorView.getTitle());
    }

}
