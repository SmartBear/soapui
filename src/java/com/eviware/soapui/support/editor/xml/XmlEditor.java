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

package com.eviware.soapui.support.editor.xml;

import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.views.xml.source.XmlSourceEditorView;

/**
 * Editor-framework for Xml Documents
 * 
 * @author ole.matzura
 */

@SuppressWarnings( "serial" )
public abstract class XmlEditor<T extends XmlDocument> extends Editor<T>
{
	public XmlEditor( T xmlDocument )
	{
		super( xmlDocument );
	}

	public boolean saveDocument( boolean validate )
	{
		XmlEditorView<?> currentView = ( XmlEditorView<?> )getCurrentView();
		return currentView == null ? true : currentView.saveDocument( validate );
	}

	public abstract XmlSourceEditorView<?> getSourceEditor();
}
