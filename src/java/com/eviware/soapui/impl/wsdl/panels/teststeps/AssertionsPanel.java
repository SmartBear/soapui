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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * Seperate panel for holding/managing assertions
 * 
 * @author ole.matzura
 */

public class AssertionsPanel extends JPanel
{
	private AssertionListModel assertionListModel;
	private JList assertionList;
	private JPopupMenu assertionListPopup;
	private final Assertable assertable;
	private AddAssertionAction addAssertionAction;
	private ConfigureAssertionAction configureAssertionAction;
	private RemoveAssertionAction removeAssertionAction;

	public AssertionsPanel( Assertable assertable )
	{
		super( new BorderLayout() );
		this.assertable = assertable;

		assertionListModel = new AssertionListModel();
		assertionList = new JList( assertionListModel );
		assertionList.setToolTipText( "Assertions for this request" );
		assertionList.setCellRenderer( new AssertionCellRenderer() );
		assertionList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		assertionListPopup = new JPopupMenu();
		addAssertionAction = new AddAssertionAction( assertable );
		assertionListPopup.add( addAssertionAction );

		assertionListPopup.addPopupMenuListener( new PopupMenuListener()
		{

			public void popupMenuWillBecomeVisible( PopupMenuEvent e )
			{
				while( assertionListPopup.getComponentCount() > 1 )
					assertionListPopup.remove( 1 );

				int ix = assertionList.getSelectedIndex();
				if( ix == -1 )
				{
					assertionListPopup.addSeparator();
					assertionListPopup.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );
					return;
				}

				TestAssertion assertion = assertionListModel.getAssertionAt( ix );
				ActionSupport.addActions( ActionListBuilder.buildActions( assertion ), assertionListPopup );
			}

			public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
			{
			}

			public void popupMenuCanceled( PopupMenuEvent e )
			{
			}
		} );

		assertionList.setComponentPopupMenu( assertionListPopup );

		assertionList.addMouseListener( new MouseAdapter()
		{

			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() < 2 )
					return;

				int ix = assertionList.getSelectedIndex();
				if( ix == -1 )
					return;

				Object obj = assertionList.getModel().getElementAt( ix );
				if( obj instanceof TestAssertion )
				{
					TestAssertion assertion = ( TestAssertion )obj;
					if( assertion.isConfigurable() )
						assertion.configure();

					return;
				}

				if( obj instanceof AssertionError )
				{
					AssertionError error = ( AssertionError )obj;
					if( error.getLineNumber() >= 0 )
					{
						selectError( error );
					}
					else
						Toolkit.getDefaultToolkit().beep();
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		} );

		assertionList.addKeyListener( new KeyAdapter()
		{
			public void keyPressed( KeyEvent e )
			{
				int ix = assertionList.getSelectedIndex();
				if( ix == -1 )
					return;

				TestAssertion assertion = assertionListModel.getAssertionAt( ix );
				if( e.getKeyChar() == KeyEvent.VK_ENTER )
				{
					if( assertion.isConfigurable() )
						assertion.configure();
				}
				else
				{
					ActionList actions = ActionListBuilder.buildActions( assertion );
					if( actions != null )
					{
						actions.dispatchKeyEvent( e );
					}
				}
			}
		} );

		add( new JScrollPane( assertionList ), BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
	}

	private JComponent buildToolbar()
	{
		configureAssertionAction = new ConfigureAssertionAction();
		removeAssertionAction = new RemoveAssertionAction();

		JXToolBar toolbar = UISupport.createToolbar();
		addToolbarButtons( toolbar );

		toolbar.addGlue();
		toolbar.add( new ShowOnlineHelpAction( HelpUrls.REQUEST_ASSERTIONS_HELP_URL ) );

		assertionList.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int ix = assertionList.getSelectedIndex();

				configureAssertionAction.setEnabled( ix >= 0 );
				removeAssertionAction.setEnabled( ix >= 0 );

				if( ix == -1 )
					return;
				TestAssertion assertion = assertionListModel.getAssertionAt( ix );
				configureAssertionAction.setEnabled( assertion != null && assertion.isConfigurable() );
			}
		} );

		return toolbar;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( addAssertionAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( configureAssertionAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( removeAssertionAction ) );
	}

	public void setEnabled( boolean enabled )
	{
		assertionList.setEnabled( enabled );
	}

	protected void selectError( AssertionError error )
	{
	}

	private static class AssertionCellRenderer extends JLabel implements ListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			setEnabled( list.isEnabled() );

			if( value instanceof TestAssertion )
			{
				TestAssertion assertion = ( TestAssertion )value;
				setText( assertion.getLabel() + " - " + assertion.getStatus().toString() );
				setIcon( assertion.getIcon() );

				if( assertion.isDisabled() && isEnabled() )
					setEnabled( false );
			}
			else if( value instanceof AssertionError )
			{
				AssertionError assertion = ( AssertionError )value;
				setText( " -> " + assertion.toString() );
				setIcon( null );
			}
			else if( value instanceof String )
			{
				setText( value.toString() );
			}

			if( isSelected )
			{
				setBackground( list.getSelectionBackground() );
				setForeground( list.getSelectionForeground() );
			}
			else
			{
				setBackground( list.getBackground() );
				setForeground( list.getForeground() );
			}

			setFont( list.getFont() );
			setOpaque( true );

			return this;
		}
	}

	private class AssertionListModel extends AbstractListModel implements PropertyChangeListener, AssertionsListener
	{
		private List<Object> items = new ArrayList<Object>();

		public AssertionListModel()
		{
			init();
		}

		public int getSize()
		{
			return items.size();
		}

		public Object getElementAt( int index )
		{
			return index >= items.size() ? null : items.get( index );
		}

		public TestAssertion getAssertionAt( int index )
		{
			Object object = items.get( index );
			while( !( object instanceof TestAssertion ) && index > 0 )
			{
				object = items.get( --index );
			}

			return ( TestAssertion )( ( object instanceof TestAssertion ) ? object : null );
		}

		public void refresh()
		{
			synchronized( this )
			{
				release();
				init();
				fireContentsChanged( this, 0, getSize() - 1 );
			}
		}

		private void init()
		{
			assertable.addAssertionsListener( this );

			for( int c = 0; c < assertable.getAssertionCount(); c++ )
			{
				TestAssertion assertion = assertable.getAssertionAt( c );
				addAssertion( assertion );
			}
		}

		public void release()
		{
			items.clear();

			for( int c = 0; c < assertable.getAssertionCount(); c++ )
			{
				TestAssertion assertion = assertable.getAssertionAt( c );
				assertion.removePropertyChangeListener( this );
			}

			assertable.removeAssertionsListener( this );
		}

		public synchronized void propertyChange( PropertyChangeEvent evt )
		{
			if( SwingUtilities.isEventDispatchThread() )
				refresh();
			else
				SwingUtilities.invokeLater( new Runnable()
				{

					public void run()
					{
						refresh();
					}
				} );
		}

		public void assertionAdded( TestAssertion assertion )
		{
			synchronized( this )
			{
				int sz = getSize();
				addAssertion( assertion );

				fireIntervalAdded( this, sz, items.size() - 1 );
			}
		}

		private void addAssertion( TestAssertion assertion )
		{
			assertion.addPropertyChangeListener( this );
			items.add( assertion );

			AssertionError[] errors = assertion.getErrors();
			if( errors != null )
			{
				for( int i = 0; i < errors.length; i++ )
					items.add( errors[i] );
			}
		}

		public void assertionRemoved( TestAssertion assertion )
		{
			synchronized( this )
			{
				int ix = items.indexOf( assertion );
				if( ix == -1 )
					return;

				assertion.removePropertyChangeListener( this );
				items.remove( ix );
				fireIntervalRemoved( this, ix, ix );

				// remove associated errors
				while( ix < items.size() && items.get( ix ) instanceof AssertionError )
				{
					items.remove( ix );
					fireIntervalRemoved( this, ix, ix );
				}
			}
		}
	}

	public void release()
	{
		assertionListModel.release();
	}

	public class ConfigureAssertionAction extends AbstractAction
	{
		ConfigureAssertionAction()
		{
			super( "Configure" );
			putValue( Action.SHORT_DESCRIPTION, "Configures the selection assertion" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = assertionList.getSelectedIndex();
			if( ix == -1 )
				return;

			TestAssertion assertion = assertionListModel.getAssertionAt( ix );
			if( assertion.isConfigurable() )
			{
				assertion.configure();
			}
			else
				Toolkit.getDefaultToolkit().beep();
		}
	}

	public class RemoveAssertionAction extends AbstractAction
	{
		public RemoveAssertionAction()
		{
			super( "Remove Assertion" );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected assertion" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_assertion.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = assertionList.getSelectedIndex();
			if( ix == -1 )
				return;

			TestAssertion assertion = assertionListModel.getAssertionAt( ix );
			if( UISupport.confirm( "Remove assertion [" + assertion.getName() + "]", "Remove Assertion" ) )
			{
				assertable.removeAssertion( assertion );
			}
		}
	}
}
