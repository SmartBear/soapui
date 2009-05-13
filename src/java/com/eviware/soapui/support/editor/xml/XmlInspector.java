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
import com.eviware.soapui.support.editor.EditorInspector;
import com.eviware.soapui.support.editor.EditorLocationListener;
import com.eviware.soapui.support.editor.EditorView;

/**
 * Inspectors available for the XmlDocument of a XmlEditor
 * 
 * @author ole.matzura
 */

public interface XmlInspector extends EditorLocationListener<XmlDocument>, EditorInspector<XmlDocument>
{
	public void init( Editor<XmlDocument> editor );

	public Editor<XmlDocument> getEditor();

	public boolean isContentHandler();

	public boolean isEnabledFor( EditorView<XmlDocument> view );
}
