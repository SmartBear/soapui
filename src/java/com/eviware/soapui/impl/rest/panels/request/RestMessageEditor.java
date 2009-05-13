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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;

public class RestMessageEditor<T extends ModelItem> extends Editor<RestDocument>
{
	private final T modelItem;

	public RestMessageEditor( RestDocument document, T modelItem )
	{
		super( document );
		this.modelItem = modelItem;
	}

	public T getModelItem()
	{
		return modelItem;
	}
}
