/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.support.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RequestXmlDocument extends AbstractXmlDocument implements PropertyChangeListener
{
	private final WsdlRequest request;
	private boolean updating;

	public RequestXmlDocument(WsdlRequest request)
	{
		this.request = request;
		request.addPropertyChangeListener( WsdlRequest.REQUEST_PROPERTY, this );
	}

	public String getXml()
	{
		return request.getRequestContent();
	}

	public void setXml(String xml)
	{
		request.setRequestContent( xml );
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if( !updating )
			fireXmlChanged( (String)evt.getOldValue(), (String)evt.getNewValue() );
	}

	public SchemaTypeSystem getTypeSystem()
	{
		WsdlInterface iface = (WsdlInterface) request.getOperation().getInterface();
		WsdlContext wsdlContext = iface.getWsdlContext();
		try
		{
			return wsdlContext.getSchemaTypeSystem();
		}
		catch (Exception e1)
		{
			SoapUI.logError( e1 );
			return XmlBeans.getBuiltinTypeSystem();
		}
	}
	
	public void release()
	{
		request.removePropertyChangeListener( WsdlRequest.REQUEST_PROPERTY, this );
	}
}