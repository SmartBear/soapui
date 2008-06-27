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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Document;

import org.syntax.jedit.KeywordMap;
import org.syntax.jedit.tokenmarker.CTokenMarker;
import org.syntax.jedit.tokenmarker.GroovyTokenMarker;
import org.syntax.jedit.tokenmarker.Token;

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JEditorStatusBar.JEditorStatusBarTarget;
import com.eviware.soapui.support.swing.JXEditAreaPopupMenu;
import com.eviware.soapui.support.xml.JXEditTextArea;

/**
 * Groovy editor wrapper
 * 
 * @author ole.matzura
 */

public class GroovyEditor extends JPanel implements JEditorStatusBarTarget
{
	private JXEditTextArea editArea;
	private GroovyEditorModel model;
	private InternalSettingsListener settingsListener;
	private GroovyDocumentListener groovyDocumentListener;
	private JCheckBoxMenuItem toggleLineNumbersMenuItem;
	private JPanel lineNumbersPanel;

	public GroovyEditor( GroovyEditorModel model )
	{
		super( new BorderLayout() );
		this.model = model;
		
		editArea = new JXEditTextArea( new CTokenMarker( false, initKeywords() ) );
		editArea.setBorder( BorderFactory.createMatteBorder( 0, 2, 0, 0, Color.WHITE) );
		
		Settings settings = model.getSettings();
		editArea.setFont(UISupport.getEditorFont( settings ));

		editArea.setText( model.getScript() );
		editArea.setCaretPosition(  0 );
		ActionListener runAction = model.getRunAction();
		if( runAction != null )
			editArea.getInputHandler().addKeyBinding( "A+ENTER", runAction );
		
		groovyDocumentListener = new GroovyDocumentListener();
		editArea.getDocument().addDocumentListener( groovyDocumentListener );
		
		settingsListener = new InternalSettingsListener();
		settings.addSettingsListener( settingsListener );
		
		add( editArea );
		add( buildLineNumbers(), BorderLayout.WEST );
		
		lineNumbersPanel.setVisible( settings.getBoolean( UISettings.SHOW_GROOVY_LINE_NUMBERS ) );
		
		addFocusListener( new FocusAdapter() {

			public void focusGained( FocusEvent e )
			{
				editArea.requestFocusInWindow();
			}}
		);
		
		JXEditAreaPopupMenu popup = JXEditAreaPopupMenu.add( editArea );
		popup.add( editArea.getFindAndReplaceAction());
		popup.addSeparator();
		popup.add( editArea.getGoToLineAction() );
		toggleLineNumbersMenuItem = new JCheckBoxMenuItem( "Show Line Numbers", lineNumbersPanel.isVisible() );
		toggleLineNumbersMenuItem.setAccelerator( UISupport.getKeyStroke( "alt L" ) );
		toggleLineNumbersMenuItem.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e )
			{
				lineNumbersPanel.setVisible( toggleLineNumbersMenuItem.isSelected() );
			}} );
		
		popup.add( toggleLineNumbersMenuItem );
	}
	
	private JComponent buildLineNumbers()
	{
		editArea.getInputHandler().addKeyBinding( "A+L", new ActionListener() {

			public void actionPerformed( ActionEvent e )
			{
				lineNumbersPanel.setVisible( !lineNumbersPanel.isVisible() );
				toggleLineNumbersMenuItem.setSelected( lineNumbersPanel.isVisible() );
			}} );
		
		lineNumbersPanel = new LineNumbersPanel( editArea );
		return lineNumbersPanel;
	}
	
	public JXEditTextArea getEditArea()
	{
		return editArea;
	}

	public void release()
	{
		model.getSettings().removeSettingsListener( settingsListener );
		model = null;
		editArea.getDocument().removeDocumentListener( groovyDocumentListener );
		editArea.getInputHandler().removeAllKeyBindings();
		editArea.getRightClickPopup().removeAll();
		for( PopupMenuListener listener : editArea.getRightClickPopup().getPopupMenuListeners() )
		{
			editArea.getRightClickPopup().removePopupMenuListener( listener );
		}
	}

	public void selectError(String message)
	{
		int ix = message == null ? -1 : message.indexOf( "@ line " );
		if( ix >= 0 )
		{
			try
			{
				int ix2 = message.indexOf(',', ix);
				int line = ix2 == -1 ? Integer.parseInt(message.substring(ix + 6).trim()) : Integer.parseInt(message
						.substring(ix + 6, ix2).trim());
				int column = 0;
				if (ix2 != -1)
				{
					ix = message.indexOf("column ", ix2);
					if (ix >= 0)
					{
						ix2 = message.indexOf('.', ix);
						column = ix2 == -1 ? Integer.parseInt(message.substring(ix + 7).trim()) : Integer
								.parseInt(message.substring(ix + 7, ix2).trim());
					}
				}
				
				editArea.setCaretPosition(editArea.getLineStartOffset(line - 1) + column - 1);
			}
			catch (Exception ex)
			{
			}					
			
			editArea.requestFocus();
		}
	}
	
	private KeywordMap initKeywords()
	{
		KeywordMap keywords = GroovyTokenMarker.getKeywords();
		
		String[] kw = model.getKeywords();
		if( kw != null )
		{
			for( String keyword : kw )
				keywords.add(keyword,Token.KEYWORD2);
		}
		
		return keywords;
	}
	
	private final class GroovyDocumentListener extends DocumentListenerAdapter
	{
		public void update(Document document)
		{
			GroovyEditor.this.model.setScript( editArea.getText() );
		}
	}

	private final class InternalSettingsListener implements SettingsListener
	{
		public void settingChanged(String name, String newValue, String oldValue)
		{
			if( name.equals( UISettings.EDITOR_FONT ))
			{
				editArea.setFont( Font.decode( newValue ));
				invalidate();
			}
		}
	}

	public void addCaretListener( CaretListener listener )
	{
		editArea.addCaretListener( listener );
	}

	public int getCaretPosition()
	{
		return editArea.getCaretPosition();
	}

	public int getLineOfOffset( int offset ) throws Exception
	{
		return editArea.getLineOfOffset( offset );
	}

	public int getLineStartOffset( int line ) throws Exception
	{
		return editArea.getLineStartOffset( line );
	}

	public void removeCaretListener( CaretListener listener )
	{
		editArea.removeCaretListener( listener );
	}
}
