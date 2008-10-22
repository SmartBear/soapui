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

import com.eviware.soapui.support.components.JUndoableTextArea;

import javax.swing.*;
import javax.swing.text.Document;

public class DefaultEditorFactory implements EditorFactory
{
   public JComponent buildXPathEditor( EditorModel editorModel )
   {
      JUndoableTextArea textArea = new JUndoableTextArea( );
      textArea.setText( editorModel.getEditorText() );
      textArea.getDocument().addDocumentListener( new EditorModelDocumentListener( editorModel ) );
      return new JScrollPane( textArea );
   }

   public JComponent buildXmlEditor( EditorModel editorModel )
   {
      JUndoableTextArea textArea = new JUndoableTextArea( );
      textArea.setText( editorModel.getEditorText() );
      textArea.getDocument().addDocumentListener( new EditorModelDocumentListener( editorModel ) );
      return new JScrollPane( textArea );
   }

   private static class EditorModelDocumentListener extends DocumentListenerAdapter
   {
      private EditorModel editorModel;

      public EditorModelDocumentListener( EditorModel editorModel )
      {
         this.editorModel = editorModel;
      }

      public void update( Document document )
      {
         editorModel.setEditorText( getText( document ));
      }
   }
}
