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

package com.eviware.soapui.support.editor.views.xml.raw;

import java.beans.PropertyChangeEvent;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.types.StringToStringMap;

public class RawXmlEditorFactory implements ResponseEditorViewFactory, RequestEditorViewFactory
{
	public static final String VIEW_ID = "Raw";

	public String getViewId()
	{
		return VIEW_ID;
	}

	@SuppressWarnings( "unchecked" )
	public EditorView<?> createResponseEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof MessageExchangeModelItem )
		{
			return new WsdlMessageExchangeResponseRawXmlEditor( ( MessageExchangeModelItem )modelItem, ( XmlEditor )editor );
		}
		else if( modelItem instanceof AbstractHttpRequest )
		{
			return new HttpResponseRawXmlEditor( ( AbstractHttpRequest )modelItem, ( XmlEditor )editor );
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			return new WsdlMockResponseRawXmlEditor( ( WsdlMockResponse )modelItem, ( XmlEditor )editor );
		}

		return null;
	}

	@SuppressWarnings( "unchecked" )
	public EditorView<XmlDocument> createRequestEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof MessageExchangeModelItem )
		{
			return new WsdlMessageExchangeRequestRawXmlEditor( ( MessageExchangeModelItem )modelItem, ( XmlEditor )editor );
		}
		else if( modelItem instanceof AbstractHttpRequest )
		{
			return new HttpRequestRawXmlEditor( ( AbstractHttpRequest )modelItem, ( XmlEditor )editor );
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			return new WsdlMockRequestRawXmlEditor( ( WsdlMockResponse )modelItem, ( XmlEditor )editor );
		}

		return null;
	}

	private static class HttpRequestRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final AbstractHttpRequest request;

		public HttpRequestRawXmlEditor( AbstractHttpRequest request, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The actual content of the last submitted request" );
			this.request = request;

			request.addPropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlRequest.RESPONSE_PROPERTY ) )
			{
				setXml( "" );
			}
		}

		@Override
		public String getContent()
		{
			if( request.getResponse() == null || request.getResponse().getRawRequestData() == null )
				return "";
			else
				return new String( request.getResponse().getRawRequestData() );
		}

		@Override
		public void release()
		{
			request.removePropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
			super.release();
		}
	}

	private static class HttpResponseRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final AbstractHttpRequest request;

		public HttpResponseRawXmlEditor( AbstractHttpRequest request, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The actual content of the last received response" );
			this.request = request;

			request.addPropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlRequest.RESPONSE_PROPERTY ) )
			{
				setXml( "" );
			}
		}

		@Override
		public String getContent()
		{
			if( request.getResponse() == null )
				return "<missing response>";

			return new String( request.getResponse().getRawResponseData() );
		}

		@Override
		public void release()
		{
			request.removePropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
			super.release();
		}
	}

	private static class WsdlMockRequestRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final WsdlMockResponse request;

		public WsdlMockRequestRawXmlEditor( WsdlMockResponse response, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The actual content of the last received mock request" );
			this.request = response;

			response.addPropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, this );
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlMockResponse.MOCKRESULT_PROPERTY ) )
			{
				setXml( "" );
			}
		}

		@Override
		public String getContent()
		{
			if( request.getMockResult() == null )
				return "<missing request>";

			return buildRawContent( request.getMockResult().getMockRequest().getRequestHeaders(), request.getMockResult()
					.getMockRequest().getRawRequestData() );
		}

		@Override
		public void release()
		{
			request.removePropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, this );
			super.release();
		}
	}

	private static class WsdlMockResponseRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final WsdlMockResponse request;

		public WsdlMockResponseRawXmlEditor( WsdlMockResponse response, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The actual content of the last returned Mock Response" );
			this.request = response;

			response.addPropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, this );
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( WsdlMockResponse.MOCKRESULT_PROPERTY ) )
			{
				setXml( "" );
			}
		}

		@Override
		public String getContent()
		{
			if( request.getMockResult() == null )
				return "<missing response>";

			StringToStringMap headers = request.getMockResult().getResponseHeaders();
			byte[] data = request.getMockResult().getRawResponseData();

			return buildRawContent( headers, data );
		}

		@Override
		public void release()
		{
			request.removePropertyChangeListener( WsdlMockResponse.MOCKRESULT_PROPERTY, this );
			super.release();
		}
	}

	private static String buildRawContent( StringToStringMap headers, byte[] data )
	{
		StringBuffer result = new StringBuffer();
		String status = headers.get( "#status#" );
		if( status != null )
			result.append( status ).append( '\n' );

		for( String header : headers.keySet() )
		{
			if( header.equals( "#status#" ) )
				continue;

			result.append( header ).append( ": " ).append( headers.get( header ) ).append( '\n' );
		}
		result.append( '\n' );

		if( data != null )
			result.append( new String( data ).trim() );

		return result.toString().trim();
	}

	private static class WsdlMessageExchangeResponseRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final MessageExchangeModelItem response;

		public WsdlMessageExchangeResponseRawXmlEditor( MessageExchangeModelItem response, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The raw response data" );
			this.response = response;
		}

		@Override
		public String getContent()
		{
			MessageExchange me = response.getMessageExchange();
			return me == null || me.getRawResponseData() == null ? "" : new String( me.getRawResponseData() );
		}
	}

	private static class WsdlMessageExchangeRequestRawXmlEditor extends RawXmlEditor<XmlDocument>
	{
		private final MessageExchangeModelItem request;

		public WsdlMessageExchangeRequestRawXmlEditor( MessageExchangeModelItem request, XmlEditor<XmlDocument> editor )
		{
			super( "Raw", editor, "The raw request data" );
			this.request = request;
		}

		@Override
		public String getContent()
		{
			MessageExchange me = request.getMessageExchange();
			return me == null || me.getRawRequestData() == null ? "" : new String( me.getRawRequestData() );
		}
	}
}
