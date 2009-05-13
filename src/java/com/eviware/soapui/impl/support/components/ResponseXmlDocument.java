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

package com.eviware.soapui.impl.support.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.editor.xml.support.AbstractXmlDocument;

/**
 * XmlDocument for the response to a WsdlRequest
 * 
 * @author ole.matzura
 */

public class ResponseXmlDocument extends AbstractXmlDocument implements PropertyChangeListener
{
	private final WsdlRequest request;
	private boolean settingResponse;

	public ResponseXmlDocument( WsdlRequest request )
	{
		this.request = request;
		request.addPropertyChangeListener( this );
	}

	public String getXml()
	{
		Response response = request.getResponse();
		return response == null ? null : response.getContentAsString();
	}

	public void setXml( String xml )
	{
		HttpResponse response = ( HttpResponse )request.getResponse();
		if( response != null )
		{
			try
			{
				settingResponse = true;
				response.setResponseContent( xml );
			}
			finally
			{
				settingResponse = false;
			}
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( settingResponse )
			return;

		if( evt.getPropertyName().equals( WsdlRequest.RESPONSE_PROPERTY ) )
		{
			Response oldResponse = ( Response )evt.getOldValue();
			Response newResponse = ( Response )evt.getNewValue();

			fireXmlChanged( oldResponse == null ? null : oldResponse.getContentAsString(), newResponse == null ? null
					: newResponse.getContentAsString() );
		}

		if( evt.getPropertyName().equals( WsdlRequest.RESPONSE_CONTENT_PROPERTY ) )
		{
			String oldResponse = ( String )evt.getOldValue();
			String newResponse = ( String )evt.getNewValue();

			fireXmlChanged( oldResponse, newResponse );
		}
	}

	public SchemaTypeSystem getTypeSystem()
	{
		WsdlInterface iface = ( WsdlInterface )request.getOperation().getInterface();
		WsdlContext wsdlContext = iface.getWsdlContext();
		try
		{
			return wsdlContext.getSchemaTypeSystem();
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
			return XmlBeans.getBuiltinTypeSystem();
		}
	}

	public void release()
	{
		request.removePropertyChangeListener( this );
	}
}