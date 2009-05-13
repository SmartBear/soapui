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

package com.eviware.soapui.impl.support.components;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorViewFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;

/**
 * Base XmlEditor class for editing SOAP Messages
 * 
 * @author ole.matzura
 */

public abstract class ModelItemXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends XmlEditor<T2>
{
	private final T modelItem;

	public ModelItemXmlEditor( T2 xmlDocument, T modelItem )
	{
		super( xmlDocument );
		this.modelItem = modelItem;
	}

	public T getModelItem()
	{
		return modelItem;
	}

	public final XmlSourceEditorView getSourceEditor()
	{
		return ( XmlSourceEditorView )getView( XmlSourceEditorViewFactory.VIEW_ID );
	}
}
