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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;

public interface RestRequestInterface extends HttpRequestInterface<RestRequestConfig>, PropertyChangeListener
{

	/**
	 * Each value in this enumeration represents an officially supported HTTP method ("verb").
	 */
	enum HttpMethod
	{
		GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH;

		public static String[] getMethodsAsStringArray()
		{
			return new String[] { GET.toString(), POST.toString(), PUT.toString(), DELETE.toString(), HEAD.toString(),
					OPTIONS.toString(), TRACE.toString(), PATCH.toString() };
		}

		public static HttpMethod[] getMethods()
		{
			return new HttpMethod[] { GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH };
		}
	}

	public final static Logger log = Logger.getLogger( RestRequest.class );
	public static final String DEFAULT_MEDIATYPE = "application/xml";
	public static final String REST_XML_REQUEST = "restXmlRequest";

	RestMethod getRestMethod();

	RestRepresentation[] getRepresentations();

	RestRepresentation[] getRepresentations( RestRepresentation.Type type );

	RestRepresentation[] getRepresentations( RestRepresentation.Type type, String mediaType );

	String getAccept();

	void setAccept( String acceptEncoding );

	String[] getResponseMediaTypes();

	RestResource getResource();

	void setPath( String fullPath );

	void setResponse( HttpResponse response, SubmitContext context );

	void release();

	boolean hasEndpoint();

}
