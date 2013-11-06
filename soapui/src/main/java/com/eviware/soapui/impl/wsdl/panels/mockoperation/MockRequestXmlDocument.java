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

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for the last request to a WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class MockRequestXmlDocument extends AbstractXmlDocument implements XmlDocument
{
	private final WsdlMockResponse mockResponse;

	public MockRequestXmlDocument( WsdlMockResponse response )
	{
		this.mockResponse = response;
	}

	public SchemaTypeSystem getTypeSystem()
	{
		try
		{
			WsdlOperation operation = mockResponse.getMockOperation().getOperation();
			if( operation != null )
			{
				WsdlInterface iface = operation.getInterface();
				WsdlContext wsdlContext = iface.getWsdlContext();
				return wsdlContext.getSchemaTypeSystem();
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
		WsdlMockResult mockResult = mockResponse.getMockResult();
		return mockResult == null ? null : mockResult.getMockRequest().getRequestContent();
	}

	public void setXml( String xml )
	{
		WsdlMockResult mockResult = mockResponse.getMockResult();
		if( mockResult != null )
		{
			String oldXml = getXml();
			mockResult.getMockRequest().setRequestContent( xml );
			oldXml = "";
			fireXmlChanged( oldXml, xml );
		}
		else
		{
			fireXmlChanged( null, xml );
		}
	}
}
