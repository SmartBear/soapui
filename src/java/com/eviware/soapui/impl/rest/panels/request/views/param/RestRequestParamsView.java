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

package com.eviware.soapui.impl.rest.panels.request.views.param;

import javax.swing.JComponent;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestDocument;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.impl.rest.panels.resource.JWadlParamsTable;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

public class RestRequestParamsView extends AbstractXmlEditorView<RestRequestDocument>
{
	private final RestRequest restRequest;

	public RestRequestParamsView(RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super( "REST Params", restRequestMessageEditor, RestRequestParamsViewFactory.VIEW_ID );
		this.restRequest = restRequest;
	}

	@Override
	public void setXml(String xml)
	{
	}

	public boolean saveDocument(boolean validate)
	{
		return false;
	}

	public JComponent getComponent()
	{
		return new JWadlParamsTable( restRequest.getParams() );
	}

	public void setEditable(boolean enabled)
	{
	}
}