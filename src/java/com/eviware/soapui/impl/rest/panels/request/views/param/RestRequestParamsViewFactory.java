package com.eviware.soapui.impl.rest.panels.request.views.param;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestRequestMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;

public class RestRequestParamsViewFactory implements RequestEditorViewFactory
{
	public final static String VIEW_ID = "REST Params";
	
	public EditorView<?> createRequestEditorView(Editor<?> editor, ModelItem modelItem)
	{
		if( editor instanceof RestRequestMessageEditor && modelItem instanceof RestRequest )
		{
			return new RestRequestParamsView( (RestRequestMessageEditor) editor, (RestRequest) modelItem );
		}
		
		return null;
	}

	public String getViewId()
	{
		return VIEW_ID;
	}
}
