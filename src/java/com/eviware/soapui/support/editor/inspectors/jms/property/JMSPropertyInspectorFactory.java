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

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.JMSPropertyConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.inspectors.jms.property.JMSPropertyInspectorModel.AbstractJMSPropertyModel;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.types.StringToStringMap;

public class JMSPropertyInspectorFactory implements RequestInspectorFactory, ResponseInspectorFactory
{
	public static final String INSPECTOR_ID = "JMS Properties";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createRequestInspector(Editor<?> editor, ModelItem modelItem)
	{
		if (modelItem instanceof WsdlRequest)
			return new JMSPropertyInspector((JMSPropertyInspectorModel) new WsdlRequestJMSPropertiesModel(
					(WsdlRequest) modelItem));

		return null;
	}

	public EditorInspector<?> createResponseInspector(Editor<?> editor, ModelItem modelItem)
	{

//		if (modelItem instanceof WsdlRequest)
//			return new JMSPropertyInspector((JMSPropertyInspectorModel) new WsdlRequestJMSHeaderAndPropertiesModel(
//					(WsdlRequest) modelItem));

		return null;
	}

	private class WsdlRequestJMSPropertiesModel extends AbstractJMSPropertyModel<WsdlRequest>
	{
		WsdlRequest request;

		public WsdlRequestJMSPropertiesModel(WsdlRequest wsdlRequest)
		{
			super(false, wsdlRequest, "jmsProperty");
			this.request = wsdlRequest;
		}

		public StringToStringMap getJMSProperties()
		{
			List<JMSPropertyConfig> propertyList = request.getJMSPropertiesConfig().getJMSProperties();
			StringToStringMap stringToStringMap = new StringToStringMap(propertyList.size());
			for (JMSPropertyConfig jmsProperty : propertyList)
			{
				stringToStringMap.put(jmsProperty.getName(), jmsProperty.getValue());
			}
			return stringToStringMap;
		}

		public void setJMSProperties(StringToStringMap stringToStringMap)
		{
			String[] keyList = stringToStringMap.getKeys();
			List<JMSPropertyConfig> propertyList = new ArrayList<JMSPropertyConfig>();
			for (String key : keyList)
			{
				JMSPropertyConfig jmsPropertyConfig = JMSPropertyConfig.Factory.newInstance();
				jmsPropertyConfig.setName(key);
				jmsPropertyConfig.setValue(stringToStringMap.get(key));
				propertyList.add(jmsPropertyConfig);
			}
			List<JMSPropertyConfig> propertyList2 = request.getJMSPropertiesConfig().getJMSProperties();
			propertyList2.clear();
			propertyList2.addAll(propertyList);

		}
	}

	private class WsdlRequestJMSHeaderAndPropertiesModel extends AbstractJMSPropertyModel<WsdlRequest>
	{
		WsdlRequest request;

		public WsdlRequestJMSHeaderAndPropertiesModel(WsdlRequest wsdlRequest)
		{
			super(false, wsdlRequest, "jmsHeaderAndProperties");
			this.request = wsdlRequest;
		}

		public StringToStringMap getJMSProperties()
		{
			StringToStringMap stringToStringMap = new StringToStringMap();
			if ((request.getResponse()) instanceof JMSResponse)
			{
				Message message = ((JMSResponse) request.getResponse()).getMessage();
				if (message != null)
					stringToStringMap.putAll(JMSHeader.getReceivedMessageHeaders(message));
			}
			return stringToStringMap;
		}

		 public void setJMSProperties(StringToStringMap stringToStringMap2)
		{
			StringToStringMap stringToStringMap =stringToStringMap2;
			if ((request.getResponse()) instanceof JMSResponse)
			{

				Message message = ((JMSResponse) request.getResponse()).getMessage();
				stringToStringMap.putAll(JMSHeader.getReceivedMessageHeaders(message));

			}
			String[] keyList = stringToStringMap.getKeys();
			List<JMSPropertyConfig> propertyList = new ArrayList<JMSPropertyConfig>();
			for (String key : keyList)
			{
				JMSPropertyConfig jmsPropertyConfig = JMSPropertyConfig.Factory.newInstance();
				jmsPropertyConfig.setName(key);
				jmsPropertyConfig.setValue(stringToStringMap.get(key));
				propertyList.add(jmsPropertyConfig);
			}
			List<JMSPropertyConfig> propertyList2 = request.getJMSPropertiesConfig().getJMSProperties();
			propertyList2.clear();
			propertyList2.addAll(propertyList);

		}
	}

}
