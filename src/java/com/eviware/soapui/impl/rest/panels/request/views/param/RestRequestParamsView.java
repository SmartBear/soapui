package com.eviware.soapui.impl.rest.panels.request.views.param;

import javax.swing.JComponent;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.RestDocument;
import com.eviware.soapui.impl.rest.panels.request.RestRequestMessageEditor;
import com.eviware.soapui.impl.rest.panels.resource.JWadlParamsTable;
import com.eviware.soapui.support.editor.support.AbstractEditorView;

public class RestRequestParamsView extends AbstractEditorView<RestDocument>
{
	private final RestRequest restRequest;

	public RestRequestParamsView(RestRequestMessageEditor restRequestMessageEditor, RestRequest restRequest)
	{
		super( "REST Params", restRequestMessageEditor, RestRequestParamsViewFactory.VIEW_ID );
		this.restRequest = restRequest;
	}

	@Override
	public JComponent buildUI()
	{
		return new JWadlParamsTable( restRequest.getParams() );
	}
}