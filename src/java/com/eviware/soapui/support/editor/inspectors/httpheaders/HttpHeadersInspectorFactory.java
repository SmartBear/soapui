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

package com.eviware.soapui.support.editor.inspectors.httpheaders;

import java.beans.PropertyChangeEvent;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorModel.AbstractHeadersModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringMap;

public class HttpHeadersInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory
{
	public static final String INSPECTOR_ID = "HTTP Headers";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{

		if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlRequestHeadersModel(
					( AbstractHttpRequest<?> )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMockResponseRequestHeadersModel(
					( WsdlMockResponse )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}

		else if( modelItem instanceof MessageExchangeModelItem )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMessageExchangeRequestHeadersModel(
					( MessageExchangeModelItem )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		return null;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlRequestResponseHeadersModel(
					( AbstractHttpRequest<?> )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMockResponseHeadersModel(
					( WsdlMockResponse )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		else if( modelItem instanceof MessageExchangeModelItem )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMessageExchangeResponseHeadersModel(
					( MessageExchangeModelItem )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}

		return null;
	}

	private class WsdlMessageExchangeRequestHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public WsdlMessageExchangeRequestHeadersModel( MessageExchangeModelItem request )
		{
			super( true, request, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringMap() : messageExchange.getRequestHeaders();
		}
	}

	private class WsdlMessageExchangeResponseHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public WsdlMessageExchangeResponseHeadersModel( MessageExchangeModelItem response )
		{
			super( true, response, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringMap() : messageExchange.getResponseHeaders();
		}

	}

	private class WsdlRequestHeadersModel extends AbstractHeadersModel<AbstractHttpRequest<?>>
	{
		AbstractHttpRequest<?> request;
		AbstractXmlInspector inspector;

		public WsdlRequestHeadersModel( AbstractHttpRequest<?> abstractHttpRequest )
		{
			super( false, abstractHttpRequest, AbstractHttpRequestInterface.REQUEST_HEADERS_PROPERTY );
			this.request = abstractHttpRequest;
			this.request.addPropertyChangeListener( this );
		}

		public StringToStringMap getHeaders()
		{
			return getModelItem().getRequestHeaders();
		}

		public void setHeaders( StringToStringMap headers )
		{
			getModelItem().setRequestHeaders( headers );
		}

		public void setInspector( AbstractXmlInspector inspector )
		{
			this.inspector = inspector;

		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
			{
				if( inspector != null && request.getEndpoint() != null )
				{
					inspector.setEnabled( !request.getEndpoint().startsWith( JMSUtils.JMS_ENDPIONT_PREFIX ) );
				}
			}
			super.propertyChange( evt );
		}

	}

	private class WsdlMockResponseHeadersModel extends AbstractHeadersModel<WsdlMockResponse>
	{
		public WsdlMockResponseHeadersModel( WsdlMockResponse request )
		{
			super( false, request, WsdlMockResponse.HEADERS_PROPERTY );
		}

		public StringToStringMap getHeaders()
		{
			return getModelItem().getResponseHeaders();
		}

		public void setHeaders( StringToStringMap headers )
		{
			getModelItem().setResponseHeaders( headers );
		}

	}

	private class WsdlRequestResponseHeadersModel extends AbstractHeadersModel<AbstractHttpRequest<?>>
	{

		AbstractHttpRequest<?> request;
		AbstractXmlInspector inspector;

		public WsdlRequestResponseHeadersModel( AbstractHttpRequest<?> request )
		{
			super( true, request, WsdlRequest.RESPONSE_PROPERTY );
			this.request = request;
			this.request.addPropertyChangeListener( this );
		}

		public StringToStringMap getHeaders()
		{
			AbstractHttpRequestInterface<?> request = getModelItem();
			return request.getResponse() == null ? new StringToStringMap() : request.getResponse().getResponseHeaders();
		}

		public void setInspector( AbstractXmlInspector inspector )
		{
			this.inspector = inspector;

		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
			{
				inspector.setEnabled( !request.getEndpoint().startsWith( JMSUtils.JMS_ENDPIONT_PREFIX ) );
			}
			super.propertyChange( evt );
		}
	}

	private class WsdlMockResponseRequestHeadersModel extends AbstractHeadersModel<WsdlMockResponse>
	{
		public WsdlMockResponseRequestHeadersModel( WsdlMockResponse request )
		{
			super( true, request, WsdlMockResponse.MOCKRESULT_PROPERTY );
		}

		public StringToStringMap getHeaders()
		{
			WsdlMockResponse request = getModelItem();
			return request.getMockResult() == null ? new StringToStringMap() : request.getMockResult().getMockRequest()
					.getRequestHeaders();
		}
	}
}
