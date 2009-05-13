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

package com.eviware.soapui.support.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.syntax.jedit.InputHandler;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.SyntaxStyle;
import org.syntax.jedit.tokenmarker.GroovyTokenMarker;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;
import org.syntax.jedit.tokenmarker.Token;
import org.syntax.jedit.tokenmarker.TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.FindAndReplaceDialog;
import com.eviware.soapui.support.actions.FindAndReplaceable;
import com.eviware.soapui.support.components.JEditorStatusBar.JEditorStatusBarTarget;
import com.eviware.soapui.support.xml.actions.FormatXmlAction;
import com.eviware.soapui.support.xml.actions.LoadXmlTextAreaAction;
import com.eviware.soapui.support.xml.actions.SaveXmlTextAreaAction;

/**
 * JEditTextArea extension targeted specifically at XML-editing.
 * 
 * //@todo move font handling to subclass
 * 
 * @author Ole.Matzura
 */

public class JXEditTextArea extends JEditTextArea implements UndoableEditListener, FocusListener, FindAndReplaceable,
		JEditorStatusBarTarget
{
	public static final int UNDO_LIMIT = 1500;
	private UndoManager undoManager;
	private UndoAction undoAction;
	private RedoAction redoAction;
	private FindAndReplaceDialog findAndReplaceAction;
	private boolean discardEditsOnSet = true;
	private GoToLineAction goToLineAction;

	public static JXEditTextArea createXmlEditor( boolean addPopup )
	{
		JXEditTextArea editArea = new JXEditTextArea( new XMLTokenMarker() );

		if( addPopup )
		{
			JPopupMenu inputPopup = new JPopupMenu();

			inputPopup.add( new FormatXmlAction( editArea ) );
			inputPopup.addSeparator();
			inputPopup.add( editArea.getUndoAction() );
			inputPopup.add( editArea.getRedoAction() );
			inputPopup.add( editArea.createCopyAction() );
			inputPopup.add( editArea.createCutAction() );
			inputPopup.add( editArea.createPasteAction() );
			inputPopup.addSeparator();
			inputPopup.add( editArea.getFindAndReplaceAction() );
			inputPopup.addSeparator();
			inputPopup.add( editArea.getGoToLineAction() );

			inputPopup.addSeparator();
			inputPopup.add( new SaveXmlTextAreaAction( editArea, "Save Editor Content" ) );
			inputPopup.add( new LoadXmlTextAreaAction( editArea, "Load Editor Content" ) );

			editArea.setRightClickPopup( inputPopup );
		}

		return editArea;
	}

	public static JXEditTextArea createGroovyEditor()
	{
		return new JXEditTextArea( new GroovyTokenMarker() );
	}

	public static JXEditTextArea createJavaScriptEditor()
	{
		return new JXEditTextArea( new JavaScriptTokenMarker() );
	}

	public static JXEditTextArea createSqlEditor()
	{
		return new JXEditTextArea( new TSQLTokenMarker() );
	}

	public JXEditTextArea( TokenMarker tokenMarker )
	{
		getPainter().setFont( UISupport.getEditorFont() );
		getPainter().setLineHighlightColor( new Color( 240, 240, 180 ) );
		getPainter().setStyles( createXmlStyles() );
		setTokenMarker( tokenMarker );
		setBorder( BorderFactory.createEtchedBorder() );
		addFocusListener( this );

		undoAction = new UndoAction();
		getInputHandler().addKeyBinding( "C+Z", undoAction );
		redoAction = new RedoAction();
		getInputHandler().addKeyBinding( "C+Y", redoAction );
		findAndReplaceAction = new FindAndReplaceDialog( this );
		getInputHandler().addKeyBinding( "C+F", findAndReplaceAction );
		getInputHandler().addKeyBinding( "F3", findAndReplaceAction );
		getInputHandler().addKeyBinding( "A+RIGHT", new NextElementValueAction() );
		getInputHandler().addKeyBinding( "A+LEFT", new PreviousElementValueAction() );
		getInputHandler().addKeyBinding( "C+D", new DeleteLineAction() );
		getInputHandler().addKeyBinding( "S+INSERT", createPasteAction() );
		getInputHandler().addKeyBinding( "S+DELETE", createCutAction() );

		goToLineAction = new GoToLineAction();
		getInputHandler().addKeyBinding( "CA+L", goToLineAction );

		setMinimumSize( new Dimension( 50, 50 ) );
		new InternalSettingsListener( this );
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		getDocument().addUndoableEditListener( this );
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getDocument().removeUndoableEditListener( this );
	}

	public Action getFindAndReplaceAction()
	{
		return findAndReplaceAction;
	}

	public Action getGoToLineAction()
	{
		return goToLineAction;
	}

	public Action getRedoAction()
	{
		return redoAction;
	}

	public Action getUndoAction()
	{
		return undoAction;
	}

	public void setText( String text )
	{
		if( text != null && text.equals( getText() ) )
			return;

		super.setText( text == null ? "" : text );

		if( discardEditsOnSet && undoManager != null )
			undoManager.discardAllEdits();
	}

	public boolean isDiscardEditsOnSet()
	{
		return discardEditsOnSet;
	}

	public void setDiscardEditsOnSet( boolean discardEditsOnSet )
	{
		this.discardEditsOnSet = discardEditsOnSet;
	}

	public UndoManager getUndoManager()
	{
		return undoManager;
	}

	public SyntaxStyle[] createXmlStyles()
	{
		SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

		styles[Token.COMMENT1] = new SyntaxStyle( Color.black, true, false );
		styles[Token.COMMENT2] = new SyntaxStyle( new Color( 0x990033 ), true, false );
		styles[Token.KEYWORD1] = new SyntaxStyle( Color.blue, false, false );
		styles[Token.KEYWORD2] = new SyntaxStyle( Color.magenta, false, false );
		styles[Token.KEYWORD3] = new SyntaxStyle( new Color( 0x009600 ), false, false );
		styles[Token.LITERAL1] = new SyntaxStyle( new Color( 0x650099 ), false, false );
		styles[Token.LITERAL2] = new SyntaxStyle( new Color( 0x650099 ), false, true );
		styles[Token.LABEL] = new SyntaxStyle( new Color( 0x990033 ), false, true );
		styles[Token.OPERATOR] = new SyntaxStyle( Color.black, false, true );
		styles[Token.INVALID] = new SyntaxStyle( Color.red, false, true );

		return styles;
	}

	private void createUndoMananger()
	{
		undoManager = new UndoManager();
		undoManager.setLimit( UNDO_LIMIT );
	}

	/*
	 * 
	 * private void removeUndoMananger() { if (undoManager == null) return;
	 * undoManager.end(); undoManager = null; }
	 */

	public void focusGained( FocusEvent fe )
	{
		if( isEditable() && undoManager == null )
			createUndoMananger();
	}

	public void setEnabledAndEditable( boolean flag )
	{
		super.setEnabled( flag );
		setEditable( flag );
	}

	public void setEditable( boolean enabled )
	{
		super.setEditable( enabled );
		setCaretVisible( enabled );
		getPainter().setLineHighlightEnabled( enabled );

		// getPainter().setBackground( enabled ? Color.WHITE : new Color(238, 238,
		// 238) );
		repaint();
	}

	public void focusLost( FocusEvent fe )
	{
		// removeUndoMananger();
	}

	public void undoableEditHappened( UndoableEditEvent e )
	{
		if( undoManager != null )
			undoManager.addEdit( e.getEdit() );
	}

	private static ReferenceQueue<JXEditTextArea> testQueue = new ReferenceQueue<JXEditTextArea>();
	private static Map<WeakReference<JXEditTextArea>, InternalSettingsListener> testMap = new HashMap<WeakReference<JXEditTextArea>, InternalSettingsListener>();

	static
	{
		new Thread( new Runnable()
		{

			public void run()
			{
				while( true )
				{
					// System.out.println(
					// "Waiting for weak references to be released.." );

					try
					{
						Reference<? extends JXEditTextArea> ref = testQueue.remove();
						// System.out.println( "Got ref to clear" );
						InternalSettingsListener listener = testMap.remove( ref );
						if( listener != null )
						{
							// System.out.println( "Releasing listener" );
							listener.release();
						}
						else
						{
							// System.out.println( "Listener not found" );
						}
					}
					catch( InterruptedException e )
					{
						SoapUI.logError( e );
					}
				}

			}
		}, "ReferenceQueueMonitor" ).start();
	}

	private static final class InternalSettingsListener implements SettingsListener
	{
		private WeakReference<JXEditTextArea> textArea;

		public InternalSettingsListener( JXEditTextArea area )
		{
			// System.out.println( "Creating weakreference for textarea" );
			textArea = new WeakReference<JXEditTextArea>( area, testQueue );
			testMap.put( textArea, this );
			SoapUI.getSettings().addSettingsListener( this );
		}

		public void release()
		{
			if( textArea.get() == null )
			{
				SoapUI.getSettings().removeSettingsListener( this );
			}
			else
			{
				System.err.println( "Error, cannot release listener" );
			}
		}

		public void settingChanged( String name, String newValue, String oldValue )
		{
			if( name.equals( UISettings.EDITOR_FONT ) && textArea.get() != null )
			{
				textArea.get().getPainter().setFont( Font.decode( newValue ) );
				textArea.get().invalidate();
			}
		}

	}

	private class UndoAction extends AbstractAction
	{
		public UndoAction()
		{
			super( "Undo" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu Z" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			undo();
		}
	}

	private class RedoAction extends AbstractAction
	{
		public RedoAction()
		{
			super( "Redo" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu Y" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			redo();
		}
	}

	private final class GoToLineAction extends AbstractAction
	{
		public GoToLineAction()
		{
			super( "Go To Line" );
			putValue( Action.SHORT_DESCRIPTION, "Moves the caret to the specified line" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu alt L" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String line = UISupport.prompt( "Enter line-number to (1.." + ( getLineCount() ) + ")", "Go To Line", String
					.valueOf( getCaretLine() + 1 ) );

			if( line != null )
			{
				try
				{
					int ln = Integer.parseInt( line ) - 1;
					if( ln >= 0 && ln < getLineCount() )
					{
						scrollTo( ln, 0 );
						setCaretPosition( getLineStartOffset( ln ) );
					}
				}
				catch( NumberFormatException e1 )
				{
				}
			}
		}
	}

	public class NextElementValueAction implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			toNextElement();
		}
	}

	public class PreviousElementValueAction implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			toPreviousElement();
		}
	}

	public class DeleteLineAction implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			if( !isEditable() )
			{
				getToolkit().beep();
				return;
			}

			int caretLine = getCaretLine();
			if( caretLine == -1 )
				return;
			int lineStartOffset = getLineStartOffset( caretLine );
			int lineEndOffset = getLineEndOffset( caretLine );

			try
			{
				int len = lineEndOffset - lineStartOffset;
				if( lineStartOffset + len >= getDocumentLength() )
					len = getDocumentLength() - lineStartOffset;

				getDocument().remove( lineStartOffset, len );
			}
			catch( BadLocationException e1 )
			{
				SoapUI.logError( e1 );
			}
		}
	}

	public Action createCopyAction()
	{
		return new InputHandler.clip_copy();
	}

	public void undo()
	{
		if( !isEditable() )
		{
			getToolkit().beep();
			return;
		}

		try
		{
			if( undoManager != null )
				undoManager.undo();
		}
		catch( CannotUndoException cue )
		{
			Toolkit.getDefaultToolkit().beep();
		}
	}

	public Action createCutAction()
	{
		return new InputHandler.clip_cut();
	}

	public Action createPasteAction()
	{
		return new InputHandler.clip_paste();
	}

	public int getCaretColumn()
	{
		int pos = getCaretPosition();
		int line = getLineOfOffset( pos );

		return pos - getLineStartOffset( line );
	}

	public void toNextElement()
	{
		int pos = getCaretPosition();
		String text = getText();

		while( pos < text.length() )
		{
			// find ending >
			if( text.charAt( pos ) == '>' && pos < text.length() - 1
					&& ( pos > 2 && !text.substring( pos - 2, pos ).equals( "--" ) )
					&& ( pos > 1 && text.charAt( pos - 1 ) != '/' )
					&& text.indexOf( '/', pos ) == text.indexOf( '<', pos ) + 1
					&& text.lastIndexOf( '/', pos ) != text.lastIndexOf( '<', pos ) + 1 )
			{
				setCaretPosition( pos + 1 );
				return;
			}

			pos++ ;
		}

		getToolkit().beep();
	}

	public void toPreviousElement()
	{
		int pos = getCaretPosition() - 2;
		String text = getText();

		while( pos > 0 )
		{
			// find ending >
			if( text.charAt( pos ) == '>' && pos < text.length() - 1
					&& ( pos > 2 && !text.substring( pos - 2, pos ).equals( "--" ) )
					&& ( pos > 1 && text.charAt( pos - 1 ) != '/' )
					&& text.indexOf( '/', pos ) == text.indexOf( '<', pos ) + 1
					&& text.lastIndexOf( '/', pos ) != text.lastIndexOf( '<', pos ) + 1 )
			{
				setCaretPosition( pos + 1 );
				return;
			}

			pos-- ;
		}

		getToolkit().beep();
	}

	public boolean canUndo()
	{
		return undoManager != null && undoManager.canUndo();
	}

	public boolean canRedo()
	{
		return undoManager != null && undoManager.canRedo();
	}

	public void redo()
	{
		if( !isEditable() )
		{
			getToolkit().beep();
			return;
		}

		try
		{
			if( canRedo() )
				undoManager.redo();
		}
		catch( CannotRedoException cue )
		{
			Toolkit.getDefaultToolkit().beep();
		}
	}
}
