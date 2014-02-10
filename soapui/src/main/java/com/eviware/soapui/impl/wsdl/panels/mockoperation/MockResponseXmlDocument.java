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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.model.mock.MockResponse;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for a WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class MockResponseXmlDocument extends AbstractXmlDocument implements PropertyChangeListener
{
	private final MockResponse mockResponse;

	public MockResponseXmlDocument( MockResponse response )
	{
		this.mockResponse = response;

		mockResponse.addPropertyChangeListener( WsdlMockResponse.RESPONSE_CONTENT_PROPERTY, this );
	}

	public SchemaTypeSystem getTypeSystem()
	{
		try
		{
			if( mockResponse instanceof WsdlMockResponse)
			{
				WsdlOperation operation = ( WsdlOperation )mockResponse.getMockOperation().getOperation();
				if( operation != null )
				{
					WsdlInterface iface = operation.getInterface();
					WsdlContext wsdlContext = iface.getWsdlContext();
					return wsdlContext.getSchemaTypeSystem();
				}
			}
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

		return XmlBeans.getBuiltinTypeSystem();
	}

	public String getXml()
	{
		return mockResponse.getResponseContent();
	}

	public void setXml( String xml )
	{
		mockResponse.setResponseContent( xml );
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		fireXmlChanged( ( String )arg0.getOldValue(), ( String )arg0.getNewValue() );
	}

	@Override
	public void release()
	{
		mockResponse.removePropertyChangeListener( WsdlMockResponse.RESPONSE_CONTENT_PROPERTY, this );
		super.release();
	}
}
