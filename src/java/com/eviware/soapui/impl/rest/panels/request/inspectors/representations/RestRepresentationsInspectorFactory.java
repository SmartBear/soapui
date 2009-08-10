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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;

public class RestRepresentationsInspectorFactory implements ResponseInspectorFactory, RequestInspectorFactory
{
	public static final String INSPECTOR_ID = "Rep";

	public String getInspectorId()
	{
		return INSPECTOR_ID;
	}

	public EditorInspector<?> createResponseInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof RestRequestInterface )
			return new RestResponseRepresentationsInspector( ( RestRequest )modelItem );

		return null;
	}

	public EditorInspector<?> createRequestInspector( Editor<?> editor, ModelItem modelItem )
	{
		if( modelItem instanceof RestRequestInterface )
			return new RestRequestRepresentationsInspector( ( RestRequest )modelItem );

		return null;
	}
}
