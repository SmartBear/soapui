package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;

public class RestMessageEditor<T extends ModelItem> extends Editor<RestDocument> 
{
	private final T modelItem;

	public RestMessageEditor(RestDocument document, T modelItem )
	{
		super(document);
		this.modelItem = modelItem;
	}

	public T getModelItem()
	{
		return modelItem;
	}
}
