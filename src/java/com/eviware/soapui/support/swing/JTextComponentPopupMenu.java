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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.Undoable;

public final class JTextComponentPopupMenu extends JPopupMenu implements PopupMenuListener
{
	private final JTextComponent textComponent;
	private CutAction cutAction;
	private CopyAction copyAction;
	private PasteAction pasteAction;
	private ClearAction clearAction;
	private SelectAllAction selectAllAction;
	private UndoAction undoAction;
	private RedoAction redoAction;

	public static JTextComponentPopupMenu add( JTextComponent textComponent )
	{
		JPopupMenu componentPopupMenu = textComponent.getComponentPopupMenu();

		// double-check
		if( componentPopupMenu instanceof JTextComponentPopupMenu )
			return ( JTextComponentPopupMenu )componentPopupMenu;

		JTextComponentPopupMenu popupMenu = new JTextComponentPopupMenu( textComponent );
		if( componentPopupMenu != null && componentPopupMenu.getComponentCount() > 0 )
		{
			popupMenu.insert( new JSeparator(), 0 );

			while( componentPopupMenu.getComponentCount() > 0 )
			{
				Component comp = componentPopupMenu.getComponent( componentPopupMenu.getComponentCount() - 1 );
				componentPopupMenu.remove( comp );
				popupMenu.insert( comp, 0 );
			}
		}

		if( componentPopupMenu != null )
		{
			for( PopupMenuListener listener : componentPopupMenu.getPopupMenuListeners() )
			{
				popupMenu.addPopupMenuListener( listener );
			}
		}

		textComponent.setComponentPopupMenu( popupMenu );
		return popupMenu;
	}

	private JTextComponentPopupMenu( JTextComponent textComponent )
	{
		super( "Edit" );
		this.textComponent = textComponent;

		if( textComponent instanceof Undoable )
		{
			undoAction = new UndoAction();
			add( undoAction );

			redoAction = new RedoAction();
			add( redoAction );

			addSeparator();
		}

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
			( ( Undoable )textComponent ).undo();
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
			( ( Undoable )textComponent ).redo();
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
		if( textComponent instanceof Undoable )
		{
			undoAction.setEnabled( ( ( Undoable )textComponent ).canUndo() );
			redoAction.setEnabled( ( ( Undoable )textComponent ).canRedo() );
		}

		cutAction.setEnabled( textComponent.getSelectionEnd() != textComponent.getSelectionStart() );
		copyAction.setEnabled( cutAction.isEnabled() );
		clearAction.setEnabled( cutAction.isEnabled() );
		selectAllAction.setEnabled( textComponent.getText().length() > 0 );
	}
}
