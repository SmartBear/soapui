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
import com.eviware.soapui.support.editor.registry.ResponseEditorViewFactory;
import com.eviware.soapui.support.editor.registry.ResponseInspectorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/**
 * XmlEditor for a response-message to a WsdlRequest
 * 
 * @author ole.matzura
 */

public class ResponseMessageXmlEditor<T extends ModelItem, T2 extends XmlDocument> extends ModelItemXmlEditor<T, T2>
{
	@SuppressWarnings( "unchecked" )
	public ResponseMessageXmlEditor( T2 xmlDocument, T modelItem )
	{
		super( xmlDocument, modelItem );

		EditorViewFactory[] editorFactories = EditorViewFactoryRegistry.getInstance().getFactoriesOfType(
				ResponseEditorViewFactory.class );

		for( EditorViewFactory factory : editorFactories )
		{
			ResponseEditorViewFactory f = ( ResponseEditorViewFactory )factory;
			XmlEditorView editorView = ( XmlEditorView )f.createResponseEditorView( this, modelItem );
			if( editorView != null )
				addEditorView( ( EditorView<T2> )editorView );
		}

		InspectorFactory[] inspectorFactories = InspectorRegistry.getInstance().getFactoriesOfType(
				ResponseInspectorFactory.class );

		for( InspectorFactory factory : inspectorFactories )
		{
			ResponseInspectorFactory f = ( ResponseInspectorFactory )factory;
			XmlInspector inspector = ( XmlInspector )f.createResponseInspector( this, modelItem );
			if( inspector != null )
				addInspector( ( EditorInspector<T2> )inspector );
		}
	}
}
