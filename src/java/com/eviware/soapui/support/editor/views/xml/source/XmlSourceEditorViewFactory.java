/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.views.xml.source;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wadl.support.WadlValidator;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.AddWsaHeadersToMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.ApplyOutgoingWSSToMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.RemoveAllOutgoingWSSFromMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockresponse.RemoveWsaHeadersFromMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.request.*;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.RestResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.support.ValidationError;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.xml.JXEditTextArea;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.List;

/**
 * Factory for default "XML" source editor view in soapUI
 * 
 * @author ole.matzura
 */

public class XmlSourceEditorViewFactory implements ResponseEditorViewFactory, RequestEditorViewFactory
{
	public static final String VIEW_ID = "Source";

	public XmlEditorView createEditorView(XmlEditor editor)
	{
		return new XmlSourceEditorView( editor );
	}

	public String getViewId()
	{
		return VIEW_ID;
	}

	public EditorView<?> createRequestEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof WsdlRequest )
		{
			return new WsdlRequestXmlSourceEditor( (XmlEditor) editor, ( WsdlRequest ) modelItem );
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			return new WsdlMockRequestXmlSourceEditor( (XmlEditor) editor, ( WsdlMockResponse ) modelItem ); 
		}
		else if( modelItem instanceof MessageExchangeModelItem )
		{
			return new XmlSourceEditorView( (XmlEditor) editor );
		}
		
		return null;
	}
	
	public EditorView<?> createResponseEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof WsdlRequest )
		{
			return new WsdlResponseXmlSourceEditor( (XmlEditor) editor, ( WsdlRequest ) modelItem );
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			return new WsdlMockResponseXmlSourceEditor( (XmlEditor) editor, ( WsdlMockResponse ) modelItem ); 
		}
		else if( modelItem instanceof RestRequest )
		{
			return new RestResponseXmlSourceEditor( (XmlEditor) editor, (RestRequest)modelItem );
		}
		else if( modelItem instanceof MessageExchangeModelItem )
		{
			return new XmlSourceEditorView( (XmlEditor) editor );
		}
		
		return null;
	}
	
	/**
	 * XmlSource editor for a WsdlRequest
	 * 
	 * @author ole.matzura
	 */
	
	public static class WsdlRequestXmlSourceEditor extends XmlSourceEditorView
	{
		private final WsdlRequest request;
		private JMenu applyMenu;
		private JMenu wsaApplyMenu;

		public WsdlRequestXmlSourceEditor(XmlEditor xmlEditor, WsdlRequest request )
		{
			super(xmlEditor);
			this.request = request;
		}

		protected ValidationError[] validateXml( String xml )
		{
			WsdlOperation operation = request.getOperation();
			WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());

			WsdlResponseMessageExchange wsdlResponseMessageExchange = new WsdlResponseMessageExchange( request );
			wsdlResponseMessageExchange.setRequestContent( xml );
			return validator.assertRequest( wsdlResponseMessageExchange, false );
		}
		
		@Override
		protected void buildUI()
		{
			super.buildUI();
			PropertyExpansionPopupListener.enable( getInputArea(), request );
		}

		protected void buildPopup(JPopupMenu inputPopup, JXEditTextArea editArea )
		{
			super.buildPopup(inputPopup, editArea );

			inputPopup.insert( new JSeparator(), 2 );
			inputPopup.insert(new AddWSSUsernameTokenAction(request),3);
			inputPopup.insert(new AddWSTimestampAction(request),4);
			inputPopup.insert( applyMenu = new JMenu( "Outgoing WSS"),5);
			inputPopup.insert( wsaApplyMenu = new JMenu( "WS-A headers"),6 );
			
			inputPopup.addPopupMenuListener( new PopupMenuListener() {

				public void popupMenuCanceled( PopupMenuEvent e )
				{
					
				}

				public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
				{
					
				}

				public void popupMenuWillBecomeVisible( PopupMenuEvent e )
				{
					applyMenu.removeAll();
					DefaultWssContainer wss = request.getOperation().getInterface().getProject().getWssContainer();
					List<OutgoingWss> outgoingWssList = wss.getOutgoingWssList();
					applyMenu.setEnabled( !outgoingWssList.isEmpty() );
					
					for( OutgoingWss outgoing : outgoingWssList )
					{
						applyMenu.add( new ApplyOutgoingWSSToRequestAction( request, outgoing ) );
					}
					applyMenu.add( new RemoveAllOutgoingWSSFromRequestAction( request ) );
					
					wsaApplyMenu.removeAll();
					wsaApplyMenu.add( new AddWsaHeadersToRequestAction(request));
					wsaApplyMenu.add( new RemoveWsaHeadersFromRequestAction(request));
					wsaApplyMenu.setEnabled( request.getWsaConfig().isWsaEnabled() );
				}} );
		}

		public WsdlRequest getRequest()
		{
			return request;
		}
	}
	
	/**
	 * XmlSource editor for a WsdlMockRequest
	 * 
	 * @author ole.matzura
	 */
	
	public static class WsdlMockRequestXmlSourceEditor extends XmlSourceEditorView
	{
		private final WsdlMockResponse mockResponse;

		public WsdlMockRequestXmlSourceEditor(XmlEditor xmlEditor, WsdlMockResponse mockResponse )
		{
			super(xmlEditor);
			this.mockResponse = mockResponse;
		}

		protected ValidationError[] validateXml( String xml )
		{
			WsdlOperation operation = mockResponse.getMockOperation().getOperation();
			
			if( operation == null )
			{
				return new ValidationError[] { new AssertionError( "Missing operation for MockResponse")};
			}
			
			WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());
			return validator.assertRequest( new WsdlMockResultMessageExchange( mockResponse.getMockResult(), mockResponse ), false );
		}

		protected void buildPopup(JPopupMenu inputPopup, JXEditTextArea editArea )
		{
			super.buildPopup(inputPopup, editArea );
//			inputPopup.insert( new JSeparator(), 2 );
		}
	}
	
	/**
	 * XmlSource editor for a WsdlResponse
	 * 
	 * @author ole.matzura
	 */
	
	public static class WsdlResponseXmlSourceEditor extends XmlSourceEditorView
	{
		private final WsdlRequest request;

		public WsdlResponseXmlSourceEditor(XmlEditor xmlEditor, WsdlRequest request )
		{
			super(xmlEditor);
			this.request = request;
		}

		protected ValidationError[] validateXml( String xml )
		{
			if( request instanceof WsdlTestRequest )
			{
				WsdlTestRequest testRequest = (WsdlTestRequest)request;
				testRequest.assertResponse( new WsdlTestRunContext(  testRequest.getTestStep() ));
			}
			
			WsdlOperation operation = request.getOperation();
			WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());

			return validator.assertResponse( new WsdlResponseMessageExchange( request ), false );
		}
	}
	
	/**
	 * XmlSource editor for a WsdlMockResponse
	 * 
	 * @author ole.matzura
	 */
	
	public static class WsdlMockResponseXmlSourceEditor extends XmlSourceEditorView
	{
		private final WsdlMockResponse mockResponse;
		private JMenu applyMenu;
		private JMenu wsaApplyMenu;

		public WsdlMockResponseXmlSourceEditor(XmlEditor xmlEditor, WsdlMockResponse mockResponse)
		{
			super(xmlEditor);
			this.mockResponse = mockResponse;
		}
		
		@Override
		protected void buildUI()
		{
			super.buildUI();
			
			getValidateXmlAction().setEnabled( mockResponse.getMockOperation().getOperation().isBidirectional() );
		}

		protected ValidationError[] validateXml( String xml )
		{
			WsdlOperation operation = mockResponse.getMockOperation().getOperation();
			if( operation == null )
			{
				return new ValidationError[] { new AssertionError( "Missing operation for MockResponse")};
			}
			
			WsdlValidator validator = new WsdlValidator((operation.getInterface()).getWsdlContext());
			return validator.assertResponse( new WsdlMockResponseMessageExchange( mockResponse ), false );
		}

		public WsdlMockResponse getMockResponse()
		{
			return mockResponse;
		}
		
		protected void buildPopup(JPopupMenu inputPopup, JXEditTextArea editArea )
		{
			super.buildPopup(inputPopup, editArea );

			inputPopup.insert( applyMenu = new JMenu( "Outgoing WSS"),2);
			inputPopup.insert( wsaApplyMenu = new JMenu( "WS-A headers"),3 );
			
			inputPopup.addPopupMenuListener( new PopupMenuListener() {

				public void popupMenuCanceled( PopupMenuEvent e )
				{
					
				}

				public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
				{
					
				}

				public void popupMenuWillBecomeVisible( PopupMenuEvent e )
				{
					applyMenu.removeAll();
					DefaultWssContainer wss = mockResponse.getMockOperation().getMockService().getProject().getWssContainer();
					List<OutgoingWss> outgoingWssList = wss.getOutgoingWssList();
					applyMenu.setEnabled( !outgoingWssList.isEmpty() );
					
					for( OutgoingWss outgoing : outgoingWssList )
					{
						applyMenu.add( new ApplyOutgoingWSSToMockResponseAction( mockResponse, outgoing ) );
					}
					applyMenu.add(new RemoveAllOutgoingWSSFromMockResponseAction(mockResponse));
					
					wsaApplyMenu.removeAll();
					wsaApplyMenu.add( new AddWsaHeadersToMockResponseAction(mockResponse));
					wsaApplyMenu.add( new RemoveWsaHeadersFromMockResponseAction(mockResponse));
					wsaApplyMenu.setEnabled(mockResponse.getWsaConfig().isWsaEnabled());
					
				}} );
		}
	}

   private class RestResponseXmlSourceEditor extends XmlSourceEditorView
   {
      private RestRequest restRequest;

      public RestResponseXmlSourceEditor( XmlEditor<XmlDocument> xmlEditor, RestRequest restRequest )
      {
         super( xmlEditor );
         this.restRequest = restRequest;
      }

      protected ValidationError[] validateXml( String xml )
      {
         if( restRequest.getResource() == null )
            return new ValidationError[0];

         WadlValidator validator = new WadlValidator( restRequest.getResource().getService().getWadlContext() );
         return validator.assertResponse( new RestResponseMessageExchange( restRequest ) );
      }
   }
}
