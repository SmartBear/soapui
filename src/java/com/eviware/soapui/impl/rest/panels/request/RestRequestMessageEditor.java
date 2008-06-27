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

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.editor.registry.RequestMessageEditor;

/**
 * XmlEditor for the request of a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RestRequestMessageEditor extends RequestMessageEditor<RestDocument, RestRequest>
{
	public static class RestRequestDocument extends RestDocument
	{
		private final RestRequest modelItem;

		public RestRequestDocument(RestRequest modelItem)
		{
			this.modelItem = modelItem;
		}
		
		public RestRequest getRequest()
		{
			return modelItem;
		}
	}

	public RestRequestMessageEditor( RestRequest modelItem  )
	{
		super( new RestRequestDocument( modelItem ), modelItem );
	}
}
