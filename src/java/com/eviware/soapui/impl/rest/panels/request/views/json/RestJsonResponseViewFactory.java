package com.eviware.soapui.impl.rest.panels.request.views.json;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.panels.request.AbstractRestRequestDesktopPanel.RestResponseMessageEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;

public class RestJsonResponseViewFactory implements ResponseEditorViewFactory
{
	public final static String VIEW_ID = "JSON Response";
	
	public EditorView<?> createResponseEditorView(Editor<?> editor, ModelItem modelItem)
	{
		if( editor instanceof RestResponseMessageEditor && modelItem instanceof RestRequest )
		{
			return new RestJsonResponseView( (RestResponseMessageEditor) editor, (RestRequest) modelItem );
		}
		
		return null;
	}

	public String getViewId()
	{
		return VIEW_ID;
	}
}
