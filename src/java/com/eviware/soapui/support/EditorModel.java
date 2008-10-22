/*
 * soapUI, copyright (C) 2004-2008 eviware.com
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

public interface EditorModel
{
   public Settings getSettings();

   public String getEditorText();

   public void setEditorText( String text );

//   public void addEditorModelListener( EditorModelListener editorModelListener );
//
//   public void removeEditorModelListener( EditorModelListener editorModelListener );
//
//   public interface EditorModelListener
//   {
//      public void editorTextChanged( String oldText, String newText );
//   }
}
