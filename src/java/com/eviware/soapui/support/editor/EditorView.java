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

import javax.swing.JComponent;

import com.eviware.soapui.support.PropertyChangeNotifier;

/**
 * Views available for the XmlDocument of a XmlEditor
 * 
 * @author ole.matzura
 */

public interface EditorView<T extends EditorDocument> extends PropertyChangeNotifier, EditorLocationListener<T>
{
	public final static String TITLE_PROPERTY = EditorView.class.getName() + "@title";

	public Editor<T> getEditor();

	public String getTitle();

	public JComponent getComponent();

	public boolean deactivate();

	public boolean activate( EditorLocation<T> location );

	public EditorLocation<T> getEditorLocation();

	public void setLocation( EditorLocation<T> location );

	public void setDocument( T document );

	public T getDocument();

	public void addLocationListener( EditorLocationListener<T> listener );

	public void removeLocationListener( EditorLocationListener<T> listener );

	public void release();

	public void setEditable( boolean enabled );

	public String getViewId();

	public void requestFocus();
}
