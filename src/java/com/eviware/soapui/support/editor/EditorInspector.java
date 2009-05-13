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

package com.eviware.soapui.support.editor;

import com.eviware.soapui.support.components.Inspector;

/**
 * Inspectors available for the XmlDocument of a XmlEditor
 * 
 * @author ole.matzura
 */

public interface EditorInspector<T extends EditorDocument> extends EditorLocationListener<T>, Inspector
{
	public void init( Editor<T> editor );

	public Editor<T> getEditor();

	public boolean isContentHandler();

	public boolean isEnabledFor( EditorView<T> view );
}
