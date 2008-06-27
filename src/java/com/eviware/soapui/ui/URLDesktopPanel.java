/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
 
package com.eviware.soapui.ui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.support.DefaultDesktopPanel;

public class URLDesktopPanel extends DefaultDesktopPanel
{
	private JEditorPane editorPane;

	public URLDesktopPanel(String title, String description, String url)
	{
		super(title, description, new JPanel( new BorderLayout() ) );
		
		JPanel panel = (JPanel) getComponent();
		
		editorPane = new JEditorPane();
		editorPane.setEditorKit( new HTMLEditorKit() );
		editorPane.setEditable( false );
		try
		{
			editorPane.setPage( new URL( url ) );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		editorPane.addHyperlinkListener( new DefaultHyperlinkListener( editorPane ));
		
		JScrollPane scrollPane = new JScrollPane( editorPane );
		UISupport.addPreviewCorner( scrollPane, true );

		panel.add( scrollPane, BorderLayout.CENTER );
	}
}
