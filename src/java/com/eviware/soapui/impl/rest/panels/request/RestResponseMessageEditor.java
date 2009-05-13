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

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.editor.registry.RequestMessageEditor;

/**
 * XmlEditor for the request of a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RestResponseMessageEditor extends RequestMessageEditor<RestDocument, RestRequest>
{
	public static class RestResponseDocument extends RestDocument
	{
		private final RestRequest modelItem;

		public RestResponseDocument( RestRequest modelItem )
		{
			this.modelItem = modelItem;
		}

		public HttpResponse getResponse()
		{
			return modelItem.getResponse();
		}
	}

	public RestResponseMessageEditor( RestRequest modelItem )
	{
		super( new RestResponseDocument( modelItem ), modelItem );
	}
}
