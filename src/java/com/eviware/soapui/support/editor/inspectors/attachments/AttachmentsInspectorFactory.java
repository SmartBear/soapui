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

package com.eviware.soapui.support.editor.inspectors.attachments;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.impl.wsdl.HttpAttachmentPart;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;

public class AttachmentsInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory
{
	public static final String INSPECTOR_ID = "Attachments";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof AbstractHttpRequest<?> )
			return new AttachmentsInspector( ( AttachmentContainer )modelItem );
		else if( modelItem instanceof WsdlMockResponse )
			return new AttachmentsInspector( new MockRequestAttachmentsContainer( ( WsdlMockResponse )modelItem ) );
		else if( modelItem instanceof MessageExchangeModelItem )
			return new AttachmentsInspector( new WsdlMessageExchangeRequestAttachmentsContainer(
					( MessageExchangeModelItem )modelItem ) );

		return null;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof AbstractHttpRequest<?> )
			return new AttachmentsInspector( new ResponseAttachmentsContainer( ( AbstractHttpRequest<?> )modelItem ) );
		else if( modelItem instanceof WsdlMockResponse )
			return new AttachmentsInspector( ( WsdlMockResponse )modelItem );
		else if( modelItem instanceof MessageExchangeModelItem )
			return new AttachmentsInspector( new WsdlMessageExchangeResponseAttachmentsContainer(
					( MessageExchangeModelItem )modelItem ) );

		return null;
	}

	protected static class WsdlMessageExchangeRequestAttachmentsContainer implements AttachmentContainer
	{
		private final MessageExchangeModelItem request;

		public WsdlMessageExchangeRequestAttachmentsContainer( MessageExchangeModelItem request )
		{
			this.request = request;
		}

		public void addAttachmentsChangeListener( PropertyChangeListener listener )
		{
			request.addPropertyChangeListener( listener );
		}

		public Attachment getAttachmentAt( int index )
		{
			return request.getMessageExchange() == null ? null
					: request.getMessageExchange().getRequestAttachments()[index];
		}

		public int getAttachmentCount()
		{
			return request.getMessageExchange() == null ? 0 : request.getMessageExchange().getRequestAttachments().length;
		}

		public HttpAttachmentPart getAttachmentPart( String partName )
		{
			return null;
		}

		public ModelItem getModelItem()
		{
			return request.getParent();
		}

		public Attachment[] getAttachments()
		{
			return request.getMessageExchange() == null ? null : request.getMessageExchange().getRequestAttachments();
		}

		public Attachment[] getAttachmentsForPart( String partName )
		{
			return request.getMessageExchange() == null ? null : request.getMessageExchange()
					.getRequestAttachmentsForPart( partName );
		}

		public HttpAttachmentPart[] getDefinedAttachmentParts()
		{
			if( request.getMessageExchange() == null || request.getMessageExchange().getOperation() == null )
				return new HttpAttachmentPart[0];

			MessagePart[] responseParts = request.getMessageExchange().getOperation().getDefaultRequestParts();

			List<HttpAttachmentPart> result = new ArrayList<HttpAttachmentPart>();

			for( MessagePart part : responseParts )
				if( part instanceof HttpAttachmentPart )
					result.add( ( HttpAttachmentPart )part );

			return result.toArray( new HttpAttachmentPart[result.size()] );
		}

		public boolean isMultipartEnabled()
		{
			return false;
		}

		public void removeAttachmentsChangeListener( PropertyChangeListener listener )
		{
			request.removePropertyChangeListener( listener );
		}
	}

	protected static class WsdlMessageExchangeResponseAttachmentsContainer implements AttachmentContainer
	{
		private final MessageExchangeModelItem response;

		public WsdlMessageExchangeResponseAttachmentsContainer( MessageExchangeModelItem response )
		{
			this.response = response;
		}

		public void addAttachmentsChangeListener( PropertyChangeListener listener )
		{
			response.addPropertyChangeListener( listener );
		}

		public Attachment getAttachmentAt( int index )
		{
			return response.getMessageExchange() == null ? null
					: response.getMessageExchange().getResponseAttachments()[index];
		}

		public int getAttachmentCount()
		{
			return response.getMessageExchange() == null ? 0
					: response.getMessageExchange().getResponseAttachments().length;
		}

		public HttpAttachmentPart getAttachmentPart( String partName )
		{
			return null;
		}

		public ModelItem getModelItem()
		{
			return response.getParent();
		}

		public Attachment[] getAttachments()
		{
			return response.getMessageExchange() == null ? null : response.getMessageExchange().getResponseAttachments();
		}

		public Attachment[] getAttachmentsForPart( String partName )
		{
			return response.getMessageExchange() == null ? null : response.getMessageExchange()
					.getResponseAttachmentsForPart( partName );
		}

		public HttpAttachmentPart[] getDefinedAttachmentParts()
		{
			if( response.getMessageExchange() == null || response.getMessageExchange().getOperation() == null )
				return new HttpAttachmentPart[0];

			MessagePart[] responseParts = response.getMessageExchange().getOperation().getDefaultResponseParts();

			List<HttpAttachmentPart> result = new ArrayList<HttpAttachmentPart>();

			for( MessagePart part : responseParts )
				if( part instanceof HttpAttachmentPart )
					result.add( ( HttpAttachmentPart )part );

			return result.toArray( new HttpAttachmentPart[result.size()] );
		}

		public boolean isMultipartEnabled()
		{
			return false;
		}

		public void removeAttachmentsChangeListener( PropertyChangeListener listener )
		{
			response.removePropertyChangeListener( listener );
		}
	}

	protected static class ResponseAttachmentsContainer implements AttachmentContainer
	{
		private final AbstractHttpRequest<?> request;

		public ResponseAttachmentsContainer( AbstractHttpRequest<?> abstractHttpRequest )
		{
			this.request = abstractHttpRequest;
		}

		public void addAttachmentsChangeListener( PropertyChangeListener listener )
		{
			request.addPropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, listener );
		}

		public Attachment getAttachmentAt( int index )
		{
			return request.getResponse() == null ? null : request.getResponse().getAttachments()[index];
		}

		public int getAttachmentCount()
		{
			return request.getResponse() == null ? 0 : request.getResponse().getAttachments().length;
		}

		public HttpAttachmentPart getAttachmentPart( String partName )
		{
			return null;
		}

		public ModelItem getModelItem()
		{
			return request;
		}

		public Attachment[] getAttachments()
		{
			return request.getResponse() == null ? null : request.getResponse().getAttachments();
		}

		public Attachment[] getAttachmentsForPart( String partName )
		{
			return request.getResponse() == null ? null : request.getResponse().getAttachmentsForPart( partName );
		}

		public HttpAttachmentPart[] getDefinedAttachmentParts()
		{
			MessagePart[] responseParts = request.getResponseParts();

			List<HttpAttachmentPart> result = new ArrayList<HttpAttachmentPart>();

			for( MessagePart part : responseParts )
				if( part instanceof HttpAttachmentPart )
					result.add( ( HttpAttachmentPart )part );

			return result.toArray( new HttpAttachmentPart[result.size()] );
		}

		public boolean isMultipartEnabled()
		{
			return request.isMultipartEnabled();
		}

		public void removeAttachmentsChangeListener( PropertyChangeListener listener )
		{
			request.removePropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, listener );
		}
	}

	protected static class MockRequestAttachmentsContainer implements AttachmentContainer
	{
		private final WsdlMockResponse mockResponse;

		public MockRequestAttachmentsContainer( WsdlMockResponse mockResponse )
		{
			this.mockResponse = mockResponse;
		}

		public void addAttachmentsChangeListener( PropertyChangeListener listener )
		{
			mockResponse.addPropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, listener );
		}

		public Attachment getAttachmentAt( int index )
		{
			return mockResponse.getMockResult() == null ? null : mockResponse.getMockResult().getMockRequest()
					.getRequestAttachments()[index];
		}

		public int getAttachmentCount()
		{
			return mockResponse.getMockResult() == null ? 0 : mockResponse.getMockResult().getMockRequest()
					.getRequestAttachments().length;
		}

		public HttpAttachmentPart getAttachmentPart( String partName )
		{
			return null;
		}

		public Attachment[] getAttachments()
		{
			return mockResponse.getMockResult() == null ? null : mockResponse.getMockResult().getMockRequest()
					.getRequestAttachments();
		}

		public Attachment[] getAttachmentsForPart( String partName )
		{
			return null;
		}

		public ModelItem getModelItem()
		{
			return mockResponse;
		}

		public HttpAttachmentPart[] getDefinedAttachmentParts()
		{
			MessagePart[] responseParts = mockResponse.getRequestParts();

			List<HttpAttachmentPart> result = new ArrayList<HttpAttachmentPart>();

			for( MessagePart part : responseParts )
				if( part instanceof HttpAttachmentPart )
					result.add( ( HttpAttachmentPart )part );

			return result.toArray( new HttpAttachmentPart[result.size()] );
		}

		public boolean isMultipartEnabled()
		{
			return mockResponse.isMultipartEnabled();
		}

		public void removeAttachmentsChangeListener( PropertyChangeListener listener )
		{
			mockResponse.removePropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, listener );
		}
	}
}
