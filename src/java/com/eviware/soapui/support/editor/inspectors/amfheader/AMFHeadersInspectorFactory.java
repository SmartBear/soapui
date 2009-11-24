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

package com.eviware.soapui.support.editor.inspectors.amfheader;

import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
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
			inspector.setEnabled( true);
			return inspector;
		}else if( modelItem instanceof MessageExchangeModelItem )
		{
			AMFHeadersInspector inspector = new AMFHeadersInspector( new MessageExchangeRequestAMFHeadersModel(
					( MessageExchangeModelItem )modelItem ) );
			inspector.setEnabled( true );
			return inspector;
		}
		return null;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{
		

		return null;
	}
	
//TODO : this is just a skeleton
	private class MessageExchangeRequestAMFHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public MessageExchangeRequestAMFHeadersModel( MessageExchangeModelItem request )
		{
			super( true, request, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringMap() : messageExchange.getRequestHeaders();
		}
	}

	//TODO : this is just a skeleton
	private class MessageExchangeResponseAMFHeadersModel extends AbstractHeadersModel<MessageExchangeModelItem>
	{
		public MessageExchangeResponseAMFHeadersModel( MessageExchangeModelItem response )
		{
			super( true, response, MessageExchangeModelItem.MESSAGE_EXCHANGE );
		}

		public StringToStringMap getHeaders()
		{
			MessageExchange messageExchange = getModelItem().getMessageExchange();
			return messageExchange == null ? new StringToStringMap() : messageExchange.getResponseHeaders();
		}

	}

	private class AMFRequestHeadersModel extends AbstractHeadersModel<AMFRequestTestStep>
	{
		public AMFRequestHeadersModel( AMFRequestTestStep request )
		{
			super( false, request, AMFRequestTestStep.AMF_HEADERS_PROPERTY );
		}

		public StringToStringMap getHeaders()
		{
			return getModelItem().getAmfHeaders();
		}

		public void setHeaders( StringToStringMap headers )
		{
			getModelItem().setAmfHeaders( headers );
		}

	}

	
}
