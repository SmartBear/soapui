/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEditorModel implements EditorModel
{
	private Set<EditorModelListener> listeners = new HashSet<EditorModelListener>();

	public void addEditorModelListener( EditorModelListener editorModelListener )
	{
		listeners.add( editorModelListener );
	}

	public void removeEditorModelListener( EditorModelListener editorModelListener )
	{
		listeners.remove( editorModelListener );
	}

	public void fireEditorTextChanged( String oldText, String newText )
	{
		for( EditorModelListener listener : listeners )
		{
			listener.editorTextChanged( oldText, newText );
		}
	}

	public void release()
	{
		listeners.clear();
	}
}
