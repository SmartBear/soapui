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

package com.eviware.soapui.impl.rest.panels.request.views.content;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;

public class RestRequestContentViewFactory implements RequestEditorViewFactory
{
	public final static String VIEW_ID = "REST Content";

	public EditorView<?> createRequestEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( editor instanceof RestRequestMessageEditor && modelItem instanceof RestRequest )
		{
			return new RestRequestContentView( ( RestRequestMessageEditor )editor, ( RestRequest )modelItem );
		}

		return null;
	}

	public String getViewId()
	{
		return VIEW_ID;
	}
}
