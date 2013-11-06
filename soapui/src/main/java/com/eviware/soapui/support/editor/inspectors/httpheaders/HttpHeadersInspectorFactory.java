/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.httpheaders;

import java.beans.PropertyChangeEvent;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequest;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.AMFTestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspectorModel.AbstractHeadersModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringsMap;

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
			if( ( ( MessageExchangeModelItem )modelItem ).getMessageExchange() instanceof AMFTestStepResult )
			{
				HttpHeadersInspector inspector = new HttpHeadersInspector( new AMFMessageExchangeRequestHTTPHeadersModel(
						( MessageExchangeModelItem )modelItem ) );
				inspector.setEnabled( true );
				return inspector;
			}
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMessageExchangeRequestHeadersModel(
					( MessageExchangeModelItem )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		else if( modelItem instanceof AMFRequestTestStep )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new AMFRequestHeadersModel(
					( AMFRequestTestStep )modelItem ) );
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
			if( ( ( MessageExchangeModelItem )modelItem ).getMessageExchange() instanceof AMFTestStepResult )
			{
				HttpHeadersInspector inspector = new HttpHeadersInspector( new AMFMessageExchangeResponseHTTPHeadersModel(
						( MessageExchangeModelItem )modelItem ) );
				inspector.setEnabled( true );
				return inspector;
			}
			HttpHeadersInspector inspector = new HttpHeadersInspector( new WsdlMessageExchangeResponseHeadersModel(
					( MessageExchangeModelItem )modelItem ) );
			inspector.setEnabled( !JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		else if( modelItem instanceof AMFRequestTestStep )
		{
			HttpHeadersInspector inspector = new HttpHeadersInspector( new AMFResponseHeadersModel(
					( AMFRequestTestStep )modelItem ) );
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

		public StringToStringsMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringsMap() : messageExchange.getRequestHeaders();
		}
	}

	private class AMFMessageExchangeRequestHTTPHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public AMFMessageExchangeRequestHTTPHeadersModel( MessageExchangeModelItem request )
		{
			super( true, request, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringsMap getHeaders()
		{
			if( getModelItem().getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFTestStepResult messageExchange = ( AMFTestStepResult )getModelItem().getMessageExchange();
				return ( ( AMFRequestTestStep )messageExchange.getTestStep() ).getHttpHeaders();
			}
			return new StringToStringsMap();
		}
	}

	private class AMFMessageExchangeResponseHTTPHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public AMFMessageExchangeResponseHTTPHeadersModel( MessageExchangeModelItem modelItem )
		{
			super( true, modelItem, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringsMap getHeaders()
		{
			if( getModelItem().getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFTestStepResult messageExchange = ( AMFTestStepResult )getModelItem().getMessageExchange();
				return ( ( AMFResponse )messageExchange.getResponse() ).getResponseHeaders();
			}
			return new StringToStringsMap();
		}
	}

	private class WsdlMessageExchangeResponseHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public WsdlMessageExchangeResponseHeadersModel( MessageExchangeModelItem response )
		{
			super( true, response, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringsMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringsMap() : messageExchange.getResponseHeaders();
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

		public StringToStringsMap getHeaders()
		{
			return getModelItem().getRequestHeaders();
		}

		public void setHeaders( StringToStringsMap headers )
		{
			getModelItem().setRequestHeaders( headers );
		}

		public void setInspector( AbstractXmlInspector inspector )
		{
			this.inspector = inspector;
		}

		@Override
		public void release()
		{
			super.release();
			request.removePropertyChangeListener( this );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
			{
				if( inspector != null && request.getEndpoint() != null )
				{
					inspector.setEnabled( !request.getEndpoint().startsWith( JMSEndpoint.JMS_ENDPIONT_PREFIX ) );
				}
			}
			super.propertyChange( evt );
		}

	}

	private class AMFRequestHeadersModel extends AbstractHeadersModel<AMFRequestTestStep>
	{
		public AMFRequestHeadersModel( AMFRequestTestStep testStep )
		{
			super( false, testStep, AMFRequest.AMF_REQUEST );
		}

		public StringToStringsMap getHeaders()
		{
			return getModelItem().getHttpHeaders();
		}

		public void setHeaders( StringToStringsMap headers )
		{
			getModelItem().setHttpHeaders( headers );
		}

		@Override
		public void release()
		{
			super.release();
		}

	}

	private class AMFResponseHeadersModel extends AbstractHeadersModel<AMFRequestTestStep>
	{
		AMFRequestTestStep testStep;

		public AMFResponseHeadersModel( AMFRequestTestStep testStep )
		{
			super( true, testStep, AMFResponse.AMF_RESPONSE_HEADERS );
			this.testStep = testStep;
			this.testStep.getAMFRequest().addPropertyChangeListener( AMFRequest.AMF_RESPONSE_PROPERTY, this );
		}

		public StringToStringsMap getHeaders()
		{
			if( testStep.getAMFRequest().getResponse() != null )
			{
				AMFResponse response = testStep.getAMFRequest().getResponse();
				return response.getResponseHeaders();
			}
			else
				return new StringToStringsMap();
		}

		@Override
		public void release()
		{
			super.release();
			testStep.getAMFRequest().removePropertyChangeListener( AMFRequest.AMF_RESPONSE_PROPERTY, this );
		}
	}

	private class WsdlMockResponseHeadersModel extends AbstractHeadersModel<WsdlMockResponse>
	{
		public WsdlMockResponseHeadersModel( WsdlMockResponse request )
		{
			super( false, request, WsdlMockResponse.HEADERS_PROPERTY );
		}

		public StringToStringsMap getHeaders()
		{
			return getModelItem().getResponseHeaders();
		}

		public void setHeaders( StringToStringsMap headers )
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

		public StringToStringsMap getHeaders()
		{
			AbstractHttpRequestInterface<?> request = getModelItem();
			return request.getResponse() == null ? new StringToStringsMap() : request.getResponse().getResponseHeaders();
		}

		public void setInspector( AbstractXmlInspector inspector )
		{
			this.inspector = inspector;
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( request.getEndpoint() != null && evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
			{
				inspector.setEnabled( !request.getEndpoint().startsWith( JMSEndpoint.JMS_ENDPIONT_PREFIX ) );
			}
			super.propertyChange( evt );
		}

		@Override
		public void release()
		{
			super.release();

			request.removePropertyChangeListener( this );
		}
	}

	private class WsdlMockResponseRequestHeadersModel extends AbstractHeadersModel<WsdlMockResponse>
	{
		public WsdlMockResponseRequestHeadersModel( WsdlMockResponse request )
		{
			super( true, request, WsdlMockResponse.MOCKRESULT_PROPERTY );
		}

		public StringToStringsMap getHeaders()
		{
			WsdlMockResponse request = getModelItem();
			return request.getMockResult() == null ? new StringToStringsMap() : request.getMockResult().getMockRequest()
					.getRequestHeaders();
		}
	}
}
