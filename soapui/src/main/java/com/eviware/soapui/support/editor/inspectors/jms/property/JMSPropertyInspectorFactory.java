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

package com.eviware.soapui.support.editor.inspectors.jms.property;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.JMSPropertyConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSEndpoint;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.JMSUtils;
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

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof AbstractHttpRequest<?> )
		{
			JMSPropertyInspector inspector = new JMSPropertyInspector(
					( JMSPropertyInspectorModel )new WsdlRequestJMSPropertiesModel( ( AbstractHttpRequest<?> )modelItem ) );
			inspector.setEnabled( JMSUtils.checkIfJMS( modelItem ) );
			return inspector;
		}
		return null;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{

		return null;
	}

	private class WsdlRequestJMSPropertiesModel extends AbstractJMSPropertyModel<AbstractHttpRequest<?>>
	{
		AbstractHttpRequest<?> request;
		JMSPropertyInspector inspector;

		public WsdlRequestJMSPropertiesModel( AbstractHttpRequest<?> wsdlRequest )
		{
			super( false, wsdlRequest, "jmsProperty" );
			this.request = wsdlRequest;
			request.addPropertyChangeListener( this );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			if( request.getEndpoint() != null && evt.getPropertyName().equals( AbstractHttpRequest.ENDPOINT_PROPERTY ) )
			{
				inspector.setEnabled( request.getEndpoint().startsWith( JMSEndpoint.JMS_ENDPIONT_PREFIX ) );
			}
			super.propertyChange( evt );
		}

		@Override
		public void release()
		{
			super.release();
			request.removePropertyChangeListener( this );
		}

		public StringToStringMap getJMSProperties()
		{
			List<JMSPropertyConfig> propertyList = request.getJMSPropertiesConfig().getJMSProperties();
			StringToStringMap stringToStringMap = new StringToStringMap( propertyList.size() );
			for( JMSPropertyConfig jmsProperty : propertyList )
			{
				stringToStringMap.put( jmsProperty.getName(), jmsProperty.getValue() );
			}
			return stringToStringMap;
		}

		public void setJMSProperties( StringToStringMap stringToStringMap )
		{
			String[] keyList = stringToStringMap.getKeys();
			List<JMSPropertyConfig> propertyList = new ArrayList<JMSPropertyConfig>();
			for( String key : keyList )
			{
				JMSPropertyConfig jmsPropertyConfig = JMSPropertyConfig.Factory.newInstance();
				jmsPropertyConfig.setName( key );
				jmsPropertyConfig.setValue( stringToStringMap.get( key ) );
				propertyList.add( jmsPropertyConfig );
			}
			List<JMSPropertyConfig> propertyList2 = request.getJMSPropertiesConfig().getJMSProperties();
			propertyList2.clear();
			propertyList2.addAll( propertyList );
		}

		public void setInspector( JMSPropertyInspector inspector )
		{
			this.inspector = inspector;
		}
	}

}
