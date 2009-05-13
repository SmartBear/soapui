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

package com.eviware.soapui.support.editor.registry;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorView;

/**
 * XmlEditor for the request of a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RequestMessageEditor<T1 extends EditorDocument, T2 extends ModelItem> extends Editor<T1>
{
	private final T2 modelItem;

	@SuppressWarnings( "unchecked" )
	public RequestMessageEditor( T1 xmlDocument, T2 modelItem )
	{
		super( xmlDocument );
		this.modelItem = modelItem;

		EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
				RequestEditorViewFactory.class );

		for( EditorViewFactory factory : editorFactories )
		{
			RequestEditorViewFactory f = ( RequestEditorViewFactory )factory;
			EditorView<T1> editorView = ( EditorView<T1> )f.createRequestEditorView( this, modelItem );
			if( editorView != null )
				addEditorView( editorView );
		}

		InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
				RequestInspectorFactory.class );

		for( InspectorFactory factory : inspectorFactories )
		{
			RequestInspectorFactory f = ( RequestInspectorFactory )factory;
			EditorInspector<T1> inspector = ( EditorInspector<T1> )f.createRequestInspector( this, modelItem );
			if( inspector != null )
				addInspector( inspector );
		}
	}

	public T2 getModelItem()
	{
		return modelItem;
	}
}
