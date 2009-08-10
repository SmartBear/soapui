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
package com.eviware.soapui.impl.rest;

import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.SubmitContext;

public interface RestRequestInterface extends HttpRequestInterface<RestRequestConfig>, PropertyChangeListener
{

	public enum RequestMethod
	{
		GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE;

		public static String[] getMethodsAsStringArray()
		{
			return new String[] { GET.toString(), POST.toString(), PUT.toString(), DELETE.toString(), HEAD.toString(),
					OPTIONS.toString(), TRACE.toString() };
		}

		public static RequestMethod[] getMethods()
		{
			return new RequestMethod[] { GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE };
		}
	}

	public final static Logger log = Logger.getLogger( RestRequest.class );
	public static final String DEFAULT_MEDIATYPE = "application/xml";
	public static final String REST_XML_REQUEST = "restXmlRequest";

	public abstract RestMethod getRestMethod();

	public abstract RestRepresentation[] getRepresentations();

	public abstract RestRepresentation[] getRepresentations( RestRepresentation.Type type );

	public abstract RestRepresentation[] getRepresentations( RestRepresentation.Type type, String mediaType );

	public abstract String getAccept();

	public abstract void setAccept( String acceptEncoding );

	public abstract String[] getResponseMediaTypes();

	public abstract RestResource getResource();

	public abstract void setPath( String fullPath );

	public abstract void setResponse( HttpResponse response, SubmitContext context );

	public abstract void release();

	public abstract boolean hasEndpoint();

}