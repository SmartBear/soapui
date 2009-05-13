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

package com.eviware.soapui.support.editor.views.xml.raw;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;

public abstract class RawXmlEditor<T extends XmlDocument> extends AbstractXmlEditorView<T>
{
	private JTextArea textArea;
	private JScrollPane scrollPane;

	public RawXmlEditor( String title, XmlEditor<T> xmlEditor, String tooltip )
	{
		super( title, xmlEditor, RawXmlEditorFactory.VIEW_ID );

		textArea = new JTextArea();
		textArea.setEditable( false );
		textArea.setToolTipText( tooltip );
		scrollPane = new JScrollPane( textArea );
		UISupport.addPreviewCorner( scrollPane, true );
	}

	@Override
	public void setXml( String xml )
	{
		textArea.setText( getContent() );
		textArea.setCaretPosition( 0 );
	}

	public abstract String getContent();

	public JComponent getComponent()
	{
		return scrollPane;
	}

	public boolean isInspectable()
	{
		return false;
	}

	public boolean saveDocument( boolean validate )
	{
		return true;
	}

	public void setEditable( boolean enabled )
	{

	}

}
