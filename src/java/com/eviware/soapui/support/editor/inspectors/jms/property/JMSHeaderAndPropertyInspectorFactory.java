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

package com.eviware.soapui.support.editor.inspectors.jms.property;

import javax.jms.Message;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSHeaderAndPropertyInspectorModel.AbstractJMSHeaderAndPropertyModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringMap;

public class JMSHeaderAndPropertyInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory
{
	public static final String INSPECTOR_ID = "JMS Headers and Properties";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector(Editor<?> editor, ModelItem modelItem)
	{
		return null;
	}

	public EditorInspector<?> createResponseInspector(Editor<?> editor, ModelItem modelItem)
	{

		if (modelItem instanceof WsdlRequest)
			return new JMSHeaderAndPropertyInspector((JMSHeaderAndPropertyInspectorModel) new ResponseJMSHeaderAndPropertiesModel(
					(WsdlRequest) modelItem));

		return null;
	}

	

	private class ResponseJMSHeaderAndPropertiesModel extends AbstractJMSHeaderAndPropertyModel<WsdlRequest> implements SubmitListener
	{
		WsdlRequest request;
		JMSHeaderAndPropertyInspector inspector;
		StringToStringMap headersAndProperties;
			
		public ResponseJMSHeaderAndPropertiesModel(WsdlRequest wsdlRequest)
		{
			super(true, wsdlRequest, "jmsHeaderAndProperties");
			this.request = wsdlRequest;
			request.addSubmitListener( this );
		}

		public StringToStringMap getJMSHeadersAndProperties()
		{
//			StringToStringMap stringToStringMap = new StringToStringMap();
//			if ((request.getResponse()) instanceof JMSResponse)
//			{
//				Message message = ((JMSResponse) request.getSubmitListeners()Response()).getMessage();
//				if (message != null)
//					stringToStringMap.putAll(JMSHeader.getReceivedMessageHeaders(message));
//			}
			return headersAndProperties;
		}
		
		public void afterSubmit(Submit submit, SubmitContext context)
		{
			 headersAndProperties = new StringToStringMap();
			JMSResponse jmsResponse= (JMSResponse)context.getProperty(HermesJmsRequestTransport.JMS_RESPONSE);
			if (jmsResponse instanceof JMSResponse)
			{
				Message message = jmsResponse.getMessage();
				if (message != null)
					headersAndProperties.putAll(JMSHeader.getReceivedMessageHeaders(message));
			}
			inspector.getHeadersTableModel().setData(headersAndProperties);
			
		}

		public boolean beforeSubmit(Submit submit, SubmitContext context)
		{
			return true;
		}

		public void setInspector(JMSHeaderAndPropertyInspector inspector)
		{
			this.inspector=inspector;
		}	
	}

}
