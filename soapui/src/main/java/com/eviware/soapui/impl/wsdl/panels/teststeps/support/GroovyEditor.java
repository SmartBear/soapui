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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JEditorStatusBar.JEditorStatusBarTarget;
import com.eviware.soapui.support.scripting.groovy.GroovyScriptEngineFactory;
import com.eviware.soapui.support.scripting.js.JsScriptEngineFactory;
import com.eviware.soapui.support.xml.actions.EnableLineNumbersAction;
import com.eviware.soapui.support.xml.actions.FormatXmlAction;
import com.eviware.soapui.support.xml.actions.GoToLineAction;
import com.eviware.soapui.ui.support.FindAndReplaceDialogView;

/**
 * Groovy editor wrapper
 * 
 * @author ole.matzura
 */

public class GroovyEditor extends JPanel implements JEditorStatusBarTarget, PropertyChangeListener
{
	private final RSyntaxTextArea editArea;
	private final GoToLineAction goToLineAction;
	private final EnableLineNumbersAction enableLineNumbersAction;
	private FindAndReplaceDialogView findAndReplaceDialog;
	private FormatXmlAction formatXmlAction;
	private GroovyEditorModel model;
	private final InternalSettingsListener settingsListener;
	private final GroovyDocumentListener groovyDocumentListener;
	private final RTextScrollPane scrollPane;
	private boolean updating;

	public GroovyEditor( GroovyEditorModel model )
	{
		super( new BorderLayout() );
		this.model = model;

		model.addPropertyChangeListener( this );

		Settings settings = model.getSettings();
		Font editorFont = UISupport.getEditorFont( settings );

		editArea = new RSyntaxTextArea();
		editArea.restoreDefaultSyntaxScheme();

		String defaultScriptLanguage = ( ( WsdlProject )ModelSupport.getModelItemProject( model.getModelItem() ) )
				.getDefaultScriptLanguage();
		if( defaultScriptLanguage.equals( GroovyScriptEngineFactory.ID ) )
			editArea.setSyntaxEditingStyle( SyntaxConstants.SYNTAX_STYLE_GROOVY );
		else if( defaultScriptLanguage.equals( JsScriptEngineFactory.ID ) )
			editArea.setSyntaxEditingStyle( SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT );

		editArea.setFont( editorFont );
		editArea.setBorder( BorderFactory.createMatteBorder( 0, 2, 0, 0, Color.WHITE ) );

		editArea.setText( model.getScript() );
		editArea.setCaretPosition( 0 );
		editArea.setHighlightCurrentLine( false );
		Action runAction = model.getRunAction();
		if( runAction != null )
		{
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "alt ENTER" ), "run-action" );
			editArea.getActionMap().put( "run-action", runAction );
		}

		groovyDocumentListener = new GroovyDocumentListener();
		editArea.getDocument().addDocumentListener( groovyDocumentListener );

		settingsListener = new InternalSettingsListener();
		settings.addSettingsListener( settingsListener );

		scrollPane = new RTextScrollPane( editArea, true );
		scrollPane.setPreferredSize( new Dimension( 500, 300 ) );
		add( scrollPane );

		UISupport.addPreviewCorner( scrollPane, true );

		addFocusListener( new FocusAdapter()
		{
			public void focusGained( FocusEvent e )
			{
				editArea.requestFocusInWindow();
			}
		} );

		JPopupMenu popup = editArea.getPopupMenu();
		findAndReplaceDialog = new FindAndReplaceDialogView( editArea );
		if( UISupport.isMac() )
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "meta F" ), findAndReplaceDialog );
		else
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "ctrl F" ), findAndReplaceDialog );
		popup.add( findAndReplaceDialog );
		popup.addSeparator();
		goToLineAction = new GoToLineAction( editArea, "Go To Line" );
		enableLineNumbersAction = new EnableLineNumbersAction( scrollPane, "Show Line Numbers" );

		popup.add( goToLineAction );
		popup.add( enableLineNumbersAction );

		if( UISupport.isMac() )
		{
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "control meta L" ), goToLineAction );
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "control L" ), enableLineNumbersAction );
		}
		else
		{
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "control alt L" ), goToLineAction );
			editArea.getInputMap().put( KeyStroke.getKeyStroke( "control L" ), enableLineNumbersAction );
		}
		editArea.setComponentPopupMenu( popup );
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );

		editArea.setEnabled( enabled );
	}

	public RSyntaxTextArea getEditArea()
	{
		return editArea;
	}

	public void release()
	{
		if( model != null )
		{
			model.getSettings().removeSettingsListener( settingsListener );
			model.removePropertyChangeListener( this );
		}

		model = null;
		editArea.getDocument().removeDocumentListener( groovyDocumentListener );
	}

	public void selectError( String message )
	{
		int ix = message == null ? -1 : message.indexOf( "@ line " );
		if( ix >= 0 )
		{
			try
			{
				int ix2 = message.indexOf( ',', ix );
				int line = ix2 == -1 ? Integer.parseInt( message.substring( ix + 6 ).trim() ) : Integer.parseInt( message
						.substring( ix + 6, ix2 ).trim() );
				int column = 0;
				if( ix2 != -1 )
				{
					ix = message.indexOf( "column ", ix2 );
					if( ix >= 0 )
					{
						ix2 = message.indexOf( '.', ix );
						column = ix2 == -1 ? Integer.parseInt( message.substring( ix + 7 ).trim() ) : Integer
								.parseInt( message.substring( ix + 7, ix2 ).trim() );
					}
				}

				editArea.setCaretPosition( editArea.getLineStartOffset( line - 1 ) + column - 1 );
			}
			catch( Exception ex )
			{
			}

			editArea.requestFocus();
		}
	}

	private final class GroovyDocumentListener extends DocumentListenerAdapter
	{
		public void update( Document document )
		{
			if( !updating )
			{
				GroovyEditor.this.model.setScript( editArea.getText() );
			}
		}
	}

	private final class InternalSettingsListener implements SettingsListener
	{
		public void settingChanged( String name, String newValue, String oldValue )
		{
			if( name.equals( UISettings.EDITOR_FONT ) )
			{
				Font newFont = Font.decode( newValue );
				setEditorFont( newFont );
				invalidate();
			}
		}

		@Override
		public void settingsReloaded()
		{
			// TODO Auto-generated method stub

		}
	}

	public void setEditorFont( Font newFont )
	{
		editArea.setFont( newFont );
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

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "script" ) )
		{
			updating = true;
			editArea.setText( String.valueOf( evt.getNewValue() ) );
			updating = false;
		}
	}

}
