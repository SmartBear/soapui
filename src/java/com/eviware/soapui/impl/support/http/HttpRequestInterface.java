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
package com.eviware.soapui.impl.support.http;

import com.eviware.soapui.config.AbstractRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface.RequestMethod;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.MutableAttachmentContainer;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;

public interface HttpRequestInterface<T extends AbstractRequestConfig> extends AbstractHttpRequestInterface<T>,
		Request, MutableTestPropertyHolder, PropertyExpansionContainer, MutableAttachmentContainer
{

	public String getMediaType();

	public void setMethod( RequestMethod method );

	public boolean hasRequestBody();

	public RestParamsPropertyHolder getParams();

	public boolean isPostQueryString();

	public void setMediaType( String mediaType );

	public void setPostQueryString( boolean b );

	public String getResponseContentAsXml();

	public void updateConfig( T request );

	public String getPath();

}