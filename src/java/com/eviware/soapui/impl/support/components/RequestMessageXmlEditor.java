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
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.registry.EditorViewFactory;
import com.eviware.soapui.support.editor.registry.EditorViewFactoryRegistry;
import com.eviware.soapui.support.editor.registry.InspectorFactory;
import com.eviware.soapui.support.editor.registry.InspectorRegistry;
import com.eviware.soapui.support.editor.registry.RequestEditorViewFactory;
import com.eviware.soapui.support.editor.registry.RequestInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/**
 * XmlEditor for the request of a WsdlRequest
 * 
 * @author ole.matzura
 */

public class RequestMessageXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends ModelItemXmlEditor<T, T2>
{
	@SuppressWarnings( "unchecked" )
	public RequestMessageXmlEditor( T2 xmlDocument, T modelItem )
	{
		super( xmlDocument, modelItem );

		EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
				RequestEditorViewFactory.class );

		for( EditorViewFactory factory : editorFactories )
		{
			RequestEditorViewFactory f = ( RequestEditorViewFactory )factory;
			XmlEditorView editorView = ( XmlEditorView )f.createRequestEditorView( this, modelItem );
			if( editorView != null )
				addEditorView( ( EditorView<T2> )editorView );
		}

		InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
				RequestInspectorFactory.class );

		for( InspectorFactory factory : inspectorFactories )
		{
			RequestInspectorFactory f = ( RequestInspectorFactory )factory;
			XmlInspector inspector = ( XmlInspector )f.createRequestInspector( this, modelItem );
			if( inspector != null )
				addInspector( ( EditorInspector<T2> )inspector );
		}
	}
}
