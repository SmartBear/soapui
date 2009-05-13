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

package com.eviware.soapui.support.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.UISupport;

public final class RSyntaxAreaPopupMenu extends JPopupMenu implements PopupMenuListener
{
	private final RSyntaxTextArea textComponent;
	private CutAction cutAction;
	private CopyAction copyAction;
	private PasteAction pasteAction;
	private ClearAction clearAction;
	private SelectAllAction selectAllAction;
	private UndoAction undoAction;
	private RedoAction redoAction;

	public static RSyntaxAreaPopupMenu add( RSyntaxTextArea textComponent )
	{
		// double-check
		if( textComponent.getComponentPopupMenu() instanceof RSyntaxAreaPopupMenu )
			return ( RSyntaxAreaPopupMenu )textComponent.getComponentPopupMenu();

		RSyntaxAreaPopupMenu popupMenu = new RSyntaxAreaPopupMenu( textComponent );
		textComponent.setComponentPopupMenu( popupMenu );
		return popupMenu;
	}

	private RSyntaxAreaPopupMenu( RSyntaxTextArea textComponent )
	{
		super( "Edit" );
		this.textComponent = textComponent;

		undoAction = new UndoAction();
		add( undoAction );

		redoAction = new RedoAction();
		add( redoAction );

		addSeparator();

		cutAction = new CutAction();
		add( cutAction );
		copyAction = new CopyAction();
		add( copyAction );
		pasteAction = new PasteAction();
		add( pasteAction );
		clearAction = new ClearAction();
		add( clearAction );
		addSeparator();
		selectAllAction = new SelectAllAction();
		add( selectAllAction );

		addPopupMenuListener( this );
	}

	private final class CutAction extends AbstractAction
	{
		public CutAction()
		{
			super( "Cut" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu X" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.cut();
		}
	}

	private final class CopyAction extends AbstractAction
	{
		public CopyAction()
		{
			super( "Copy" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu C" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.copy();
		}
	}

	private final class PasteAction extends AbstractAction
	{
		public PasteAction()
		{
			super( "Paste" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu V" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.paste();
		}
	}

	private final class ClearAction extends AbstractAction
	{
		public ClearAction()
		{
			super( "Clear" );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.setText( "" );
		}
	}

	private final class SelectAllAction extends AbstractAction
	{
		public SelectAllAction()
		{
			super( "Select All" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu A" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.selectAll();
		}
	}

	private final class UndoAction extends AbstractAction
	{
		public UndoAction()
		{
			super( "Undo" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu Z" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.undoLastAction();
		}
	}

	private final class RedoAction extends AbstractAction
	{
		public RedoAction()
		{
			super( "Redo" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu Y" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			textComponent.redoLastAction();
		}
	}

	public void popupMenuCanceled( PopupMenuEvent e )
	{
	}

	public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
	{
	}

	public void popupMenuWillBecomeVisible( PopupMenuEvent e )
	{
		// undoAction.setEnabled( textComponent.canUndo() );
		// redoAction.setEnabled( textComponent.canRedo() );

		cutAction.setEnabled( textComponent.getSelectionEnd() != textComponent.getSelectionStart() );
		copyAction.setEnabled( cutAction.isEnabled() );
		clearAction.setEnabled( cutAction.isEnabled() );
		selectAllAction.setEnabled( textComponent.getText().length() > 0 );
	}
}