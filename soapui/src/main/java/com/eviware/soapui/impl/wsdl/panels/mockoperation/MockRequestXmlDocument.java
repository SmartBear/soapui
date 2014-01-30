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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

/**
 * XmlDocument for the last request to a WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class MockRequestXmlDocument extends AbstractXmlDocument implements XmlDocument
{
	private final MockResponse mockResponse;

	public MockRequestXmlDocument( MockResponse response )
	{
		this.mockResponse = response;
	}

	public SchemaTypeSystem getTypeSystem()
	{
		try
		{
			if( mockResponse instanceof WsdlMockResponse )
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
		MockResult mockResult = mockResponse.getMockResult();
		return mockResult == null ? null : mockResult.getMockRequest().getRequestContent();
	}

	public void setXml( String xml )
	{
		MockResult mockResult = mockResponse.getMockResult();
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
