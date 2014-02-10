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

public class XmlSourceEditorViewFactoryTest
{

	XmlSourceEditorViewFactory xmlSourceEditorViewFactory;
	XmlEditor<MockResponseXmlDocument> editor;
	MockResponse mockResponse;

	@Before
	public void setUp() throws Exception
	{
		xmlSourceEditorViewFactory = new XmlSourceEditorViewFactory();

		editor = ( XmlEditor<MockResponseXmlDocument> )mock( XmlEditor.class );
	}

	@Test
	public void testCreateRequestEditorViewForWsdlMockResponse() throws Exception
	{
		mockResponse = mock( WsdlMockResponse.class );
		EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createRequestEditorView( editor, mockResponse );
		assertEditorView( requestEditorView );
	}

	@Test
	public void testCreateRequestEditorViewForRestMockResponse() throws Exception
	{
		mockResponse = mock( RestMockResponse.class );
		EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createRequestEditorView( editor, mockResponse );
		assertEditorView( requestEditorView );
	}

	@Test
	public void testCreateResponseEditorViewForWsdlMockResponse() throws Exception
	{
		mockResponse = mock( WsdlMockResponse.class );
		EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createResponseEditorView( editor, mockResponse );
		assertEditorView( requestEditorView );
	}

	@Test
	public void testCreateResponseEditorViewForRestMockResponse() throws Exception
	{
		mockResponse = mock( RestMockResponse.class );
		EditorView<?> requestEditorView = xmlSourceEditorViewFactory.createResponseEditorView( editor, mockResponse );
		assertEditorView( requestEditorView );
	}

	private void assertEditorView( EditorView<?> requestEditorView )
	{
		assertNotNull( requestEditorView );
		assertEquals( "Source", requestEditorView.getViewId() );
		assertEquals( "XML", requestEditorView.getTitle() );
	}

}
