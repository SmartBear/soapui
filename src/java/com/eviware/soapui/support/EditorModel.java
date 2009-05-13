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

import com.eviware.soapui.model.settings.Settings;

/**
 * Basic EditorModel for soapUI editors
 */

public interface EditorModel
{
	/**
	 * Return the settings to use for storing customizations (line-numbers, etc)
	 * 
	 * @return the settings to use
	 */

	public Settings getSettings();

	/**
	 * Returns the text to display in the editor
	 * 
	 * @return the text to display in the editor
	 */

	public String getEditorText();

	/**
	 * Save the text in the editor, usually called when the contents of the
	 * editor have changed
	 * 
	 * @param text
	 *           the editor text to save
	 */

	public void setEditorText( String text );

	/**
	 * Adds a listener for text changes
	 * 
	 * @param editorModelListener
	 */

	public void addEditorModelListener( EditorModelListener editorModelListener );

	/**
	 * Removes a listener for text changes
	 * 
	 * @param editorModelListener
	 */

	public void removeEditorModelListener( EditorModelListener editorModelListener );

	/**
	 * Interface for listeners to editor text changes
	 */

	public interface EditorModelListener
	{
		/**
		 * Notification that should be sent by EditorModel to all registered
		 * listeners if the text changes by some external method (ie not via
		 * EditorModel.setEditorText() )
		 * 
		 * @param oldText
		 *           the old text value
		 * @param newText
		 *           the new text value
		 */

		public void editorTextChanged( String oldText, String newText );
	}
}
