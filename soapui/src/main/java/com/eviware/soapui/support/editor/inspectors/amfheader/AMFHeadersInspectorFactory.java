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

package com.eviware.soapui.support.editor.inspectors.amfheader;

import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFRequest;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.AMFTestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.amfheader.AMFHeadersInspectorModel.AbstractHeadersModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringMap;

public class AMFHeadersInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory
{
	public static final String INSPECTOR_ID = "AMF Headers";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof AMFRequestTestStep )
		{
			AMFHeadersInspector inspector = new AMFHeadersInspector( new AMFRequestHeadersModel(
					( AMFRequestTestStep )modelItem ) );
			inspector.setEnabled( true );
			return inspector;
		}
		else if( modelItem instanceof MessageExchangeModelItem )
		{
			if( ( ( MessageExchangeModelItem )modelItem ).getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFHeadersInspector inspector = new AMFHeadersInspector( new MessageExchangeRequestAMFHeadersModel(
						( MessageExchangeModelItem )modelItem ) );
				inspector.setEnabled( true );
				return inspector;
			}
		}
		return null;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{

		if( modelItem instanceof AMFRequestTestStep )
		{
			AMFHeadersInspector inspector = new AMFHeadersInspector( new AMFResponseHeadersModel(
					( AMFRequestTestStep )modelItem ) );
			inspector.setEnabled( true );
			return inspector;
		}
		else if( modelItem instanceof MessageExchangeModelItem )
		{
			if( ( ( MessageExchangeModelItem )modelItem ).getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFHeadersInspector inspector = new AMFHeadersInspector( new MessageExchangeResponseAMFHeadersModel(
						( MessageExchangeModelItem )modelItem ) );
				inspector.setEnabled( true );
				return inspector;
			}
		}
		return null;
	}

	private class MessageExchangeRequestAMFHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public MessageExchangeRequestAMFHeadersModel( MessageExchangeModelItem request )
		{
			super( true, request, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			if( getModelItem().getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFTestStepResult messageExchange = ( AMFTestStepResult )getModelItem().getMessageExchange();
				return ( ( AMFRequestTestStep )messageExchange.getTestStep() ).getAmfHeaders();
			}
			return new StringToStringMap();
		}
	}

	private class MessageExchangeResponseAMFHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public MessageExchangeResponseAMFHeadersModel( MessageExchangeModelItem messageExchange )
		{
			super( true, messageExchange, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			if( getModelItem().getMessageExchange() instanceof AMFTestStepResult )
			{
				AMFTestStepResult messageExchange = ( AMFTestStepResult )getModelItem().getMessageExchange();
				if( ( ( AMFRequestTestStep )messageExchange.getTestStep() ).getAMFRequest().getResponse() != null )
				{
					return ( ( AMFRequestTestStep )messageExchange.getTestStep() ).getAMFRequest().getResponse()
							.getResponseAMFHeaders();
				}
				else
					return new StringToStringMap();

			}
			return new StringToStringMap();
		}
	}

	private class AMFRequestHeadersModel extends AbstractHeadersModel<AMFRequestTestStep>
	{
		public AMFRequestHeadersModel( AMFRequestTestStep testStep )
		{
			super( false, testStep, AMFRequestTestStep.AMF_HEADERS_PROPERTY );
		}

		public StringToStringMap getHeaders()
		{
			return getModelItem().getAmfHeaders();
		}

		public void setHeaders( StringToStringMap headers )
		{
			getModelItem().setAmfHeaders( headers );
		}

		@Override
		public void release()
		{
			getModelItem().removePropertyChangeListener( AMFRequestTestStep.AMF_HEADERS_PROPERTY, this );
			super.release();
		}

	}

	private class AMFResponseHeadersModel extends AbstractHeadersModel<AMFRequestTestStep>
	{
		AMFRequest request;

		public AMFResponseHeadersModel( AMFRequestTestStep testStep )
		{
			super( true, testStep, AMFRequestTestStep.AMF_HEADERS_PROPERTY );
			this.request = testStep.getAMFRequest();
			this.request.addPropertyChangeListener( AMFRequest.AMF_RESPONSE_PROPERTY, this );
		}

		public StringToStringMap getHeaders()
		{
			if( getModelItem().getAMFRequest().getResponse() != null )
			{
				return getModelItem().getAMFRequest().getResponse().getResponseAMFHeaders();
			}
			else
			{
				return new StringToStringMap();
			}
		}

		@Override
		public void release()
		{
			request.removePropertyChangeListener( AMFRequest.AMF_RESPONSE_PROPERTY, this );
			super.release();
		}

	}
}
