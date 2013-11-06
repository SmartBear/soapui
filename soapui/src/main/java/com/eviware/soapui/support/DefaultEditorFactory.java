/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.support.EditorModel.EditorModelListener;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;

public class DefaultEditorFactory implements EditorFactory
{
	public JComponent buildXPathEditor( EditorModel editorModel )
	{
		JUndoableTextArea textArea = new JUndoableTextArea();
		textArea.setText( editorModel.getEditorText() );
		textArea.getDocument()
				.addDocumentListener( new JTextComponentEditorModelDocumentListener( editorModel, textArea ) );
		return new JScrollPane( textArea );
	}

	public JComponent buildXmlEditor( EditorModel editorModel )
	{
		RSyntaxTextArea xmlEditor = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
		RTextScrollPane scrollPane = new RTextScrollPane( xmlEditor );
		xmlEditor = SyntaxEditorUtil.addDefaultActions( xmlEditor, scrollPane, false );
		xmlEditor.setText( editorModel.getEditorText() );
		xmlEditor.getDocument().addDocumentListener( new EditorModelDocumentListener( editorModel, xmlEditor ) );
		UISupport.addPreviewCorner( scrollPane, false );
		return scrollPane;
	}

	public JComponent buildGroovyEditor( GroovyEditorModel editorModel )
	{
		return new GroovyEditor( editorModel );
	}

	private static class EditorModelDocumentListener extends DocumentListenerAdapter implements EditorModelListener
	{
		private EditorModel editorModel;
		private final RSyntaxTextArea xmlEditor;

		public EditorModelDocumentListener( EditorModel editorModel, RSyntaxTextArea xmlEditor )
		{
			this.editorModel = editorModel;
			this.xmlEditor = xmlEditor;

			editorModel.addEditorModelListener( this );
		}

		public void update( Document document )
		{
			editorModel.setEditorText( getText( document ) );
		}

		public void editorTextChanged( String oldText, String newText )
		{
			xmlEditor.getDocument().removeDocumentListener( this );
			xmlEditor.setText( newText );
			xmlEditor.getDocument().addDocumentListener( this );
		}
	}

	private static class JTextComponentEditorModelDocumentListener extends DocumentListenerAdapter implements
			EditorModelListener
	{
		private final JTextComponent textField;
		private final EditorModel editorModel;

		public JTextComponentEditorModelDocumentListener( EditorModel editorModel, JTextComponent textField )
		{
			this.editorModel = editorModel;
			editorModel.addEditorModelListener( this );
			this.textField = textField;
		}

		public void editorTextChanged( String oldText, String newText )
		{
			textField.getDocument().removeDocumentListener( this );
			textField.setText( newText );
			textField.getDocument().addDocumentListener( this );
		}

		public void update( Document document )
		{
			editorModel.setEditorText( getText( document ) );
		}
	}
}
