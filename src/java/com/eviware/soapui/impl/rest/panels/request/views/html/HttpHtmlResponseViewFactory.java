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

package com.eviware.soapui.impl.rest.panels.request.views.html;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;

public class HttpHtmlResponseViewFactory implements ResponseEditorViewFactory
{
	public final static String VIEW_ID = "HTML Response";

	@SuppressWarnings( "unchecked" )
	public EditorView<?> createResponseEditorView( Editor<?> editor, ModelItem modelItem )
	{
		if( editor instanceof HttpResponseMessageEditor && modelItem instanceof HttpRequestInterface<?> )
		{
			return new HttpHtmlResponseView( ( HttpResponseMessageEditor )editor, ( HttpRequestInterface<?> )modelItem );
		}

		return null;
	}

	public String getViewId()
	{
		return VIEW_ID;
	}
}
