/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

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
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.SecurityChecksListener;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.check.actions.AddSecurityCheckAction;
import com.eviware.soapui.security.check.actions.ConfigureSecurityCheckAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * Seperate panel for holding/managing securityChecks
 * 
 * @author dragica.soldo
 */

public class SecurityChecksPanel extends JPanel
{
	private SecurityCheckListModel securityCheckListModel;
	private JList securityCheckList;
	private JPopupMenu securityCheckListPopup;
	private final Securable securable;
	private AddSecurityCheckAction addSecurityCheckAction;
	private ConfigureSecurityCheckAction configureSecurityCheckAction;
	private RemoveSecurityCheckActionAction removeSecurityCheckAction;
	private MoveSecurityCheckUpAction moveSecurityCheckUpAction;
	private MoveSecurityCheckDownAction moveSecurityCheckDownAction;

	public SecurityChecksPanel( Securable securable )
	{
		super( new BorderLayout() );
		this.securable = securable;

		securityCheckListModel = new SecurityCheckListModel();
		securityCheckList = new JList( securityCheckListModel );
		securityCheckList.setToolTipText( "SecurityChecks for this TestStep" );
		securityCheckList.setCellRenderer( new AssertionCellRenderer() );
		securityCheckList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		securityCheckListPopup = new JPopupMenu();
		addSecurityCheckAction = new AddSecurityCheckAction( securable );
		securityCheckListPopup.add( addSecurityCheckAction );

		securityCheckListPopup.addPopupMenuListener( new PopupMenuListener()
		{

			public void popupMenuWillBecomeVisible( PopupMenuEvent e )
			{
				while( securityCheckListPopup.getComponentCount() > 1 )
					securityCheckListPopup.remove( 1 );

				int ix = securityCheckList.getSelectedIndex();
				if( ix == -1 )
				{
					securityCheckListPopup.addSeparator();
					securityCheckListPopup.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );
					return;
				}

				SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
				ActionSupport.addActions( ActionListBuilder.buildActions( securityCheck ), securityCheckListPopup );
			}

			public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
			{
			}

			public void popupMenuCanceled( PopupMenuEvent e )
			{
			}
		} );

		securityCheckList.setComponentPopupMenu( securityCheckListPopup );

		securityCheckList.addMouseListener( new MouseAdapter()
		{

			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() < 2 )
					return;

				int ix = securityCheckList.getSelectedIndex();
				if( ix == -1 )
					return;

				Object obj = securityCheckList.getModel().getElementAt( ix );
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

		securityCheckList.addKeyListener( new KeyAdapter()
		{
			public void keyPressed( KeyEvent e )
			{
				int ix = securityCheckList.getSelectedIndex();
				if( ix == -1 )
					return;

				SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
				if( e.getKeyChar() == KeyEvent.VK_ENTER )
				{
					if( securityCheck.isConfigurable() )
						securityCheck.configure();
				}
				else
				{
					ActionList actions = ActionListBuilder.buildActions( securityCheck );
					if( actions != null )
					{
						actions.dispatchKeyEvent( e );
					}
				}
			}
		} );

		add( new JScrollPane( securityCheckList ), BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
	}

	private JComponent buildToolbar()
	{
		configureSecurityCheckAction = new ConfigureSecurityCheckAction();
		removeSecurityCheckAction = new RemoveSecurityCheckActionAction();
		moveSecurityCheckUpAction = new MoveSecurityCheckUpAction();
		moveSecurityCheckDownAction = new MoveSecurityCheckDownAction();

		JXToolBar toolbar = UISupport.createToolbar();
		addToolbarButtons( toolbar );

		toolbar.addGlue();
		toolbar.add( new ShowOnlineHelpAction( HelpUrls.REQUEST_ASSERTIONS_HELP_URL ) );

		securityCheckList.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int ix = securityCheckList.getSelectedIndex();

				configureSecurityCheckAction.setEnabled( ix >= 0 );
				removeSecurityCheckAction.setEnabled( ix >= 0 );
				moveSecurityCheckUpAction.setEnabled( ix >= 0 );
				moveSecurityCheckDownAction.setEnabled( ix >= 0 );

				if( ix == -1 )
					return;
				SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
				configureSecurityCheckAction.setEnabled( securityCheck != null && securityCheck.isConfigurable() );
			}
		} );

		return toolbar;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( addSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( configureSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( removeSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( moveSecurityCheckUpAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( moveSecurityCheckDownAction ) );
	}

	public void setEnabled( boolean enabled )
	{
		securityCheckList.setEnabled( enabled );
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

	private class SecurityCheckListModel extends AbstractListModel implements PropertyChangeListener,
			SecurityChecksListener
	{
		private List<Object> items = new ArrayList<Object>();

		public SecurityCheckListModel()
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

		public SecurityCheck getSecurityCheckAt( int index )
		{
			Object object = items.get( index );
			while( !( object instanceof SecurityCheck ) && index > 0 )
			{
				object = items.get( --index );
			}

			return ( SecurityCheck )( ( object instanceof SecurityCheck ) ? object : null );
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
			securable.addSecurityChecksListener( this );

			for( int c = 0; c < securable.getSecurityCheckCount(); c++ )
			{
				SecurityCheck securityCheck = securable.getSecurityCheckAt( c );
				addSecurityCheck( securityCheck );
			}
		}

		public void release()
		{
			items.clear();

			for( int c = 0; c < securable.getSecurityCheckCount(); c++ )
			{
				SecurityCheck securityCheck = securable.getSecurityCheckAt( c );
				securityCheck.removePropertyChangeListener( this );
			}

			securable.removeSecurityChecksListener( this );
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

		public void securityCheckAdded( SecurityCheck securityCheck )
		{
			synchronized( this )
			{
				int sz = getSize();
				addSecurityCheck( securityCheck );

				fireIntervalAdded( this, sz, items.size() - 1 );
			}
		}

		private void addSecurityCheck( SecurityCheck securityCheck )
		{
			securityCheck.addPropertyChangeListener( this );
			items.add( securityCheck );

			// AssertionError[] errors = securityCheck.getErrors();
			// if( errors != null )
			// {
			// for( int i = 0; i < errors.length; i++ )
			// items.add( errors[i] );
			// }
		}

		public void securityCheckRemoved( SecurityCheck securityCheck )
		{
			synchronized( this )
			{
				int ix = items.indexOf( securityCheck );
				if( ix == -1 )
					return;

				securityCheck.removePropertyChangeListener( this );
				items.remove( ix );
				fireIntervalRemoved( this, ix, ix );

				// remove associated errors
//				while( ix < items.size() && items.get( ix ) instanceof AssertionError )
//				{
//					items.remove( ix );
//					fireIntervalRemoved( this, ix, ix );
//				}
			}
		}

		public void securityCheckMoved( SecurityCheck newSecurityCheck, int ix, int offset )
		{
			synchronized( this )
			{
				// int ix = items.indexOf( assertion );
				SecurityCheck securityCheck = ( SecurityCheck )items.get( ix );
				// if first selected can't move up and if last selected can't move
				// down
				if( ( ix == 0 && offset == -1 ) || ( ix == items.size() - 1 && offset == 1 ) )
				{
					return;
				}

				securityCheck.removePropertyChangeListener( this );
				items.remove( ix );
				fireIntervalRemoved( this, ix, ix );

				// remove associated errors
//				while( ix < items.size() && items.get( ix ) instanceof AssertionError )
//				{
//					items.remove( ix );
//					fireIntervalRemoved( this, ix, ix );
//				}
				newSecurityCheck.addPropertyChangeListener( this );
				items.add( ix + offset, newSecurityCheck );
				fireIntervalAdded( this, ix + offset, ix + offset );
				// add associated errors
//				while( ix < items.size() && items.get( ix ) instanceof AssertionError )
//				{
//					items.add( newSecurityCheck );
//					fireIntervalAdded( this, ix + offset, ix + offset );
//				}
			}
		}

	}

	public void release()
	{
		securityCheckListModel.release();
	}

	public class ConfigureSecurityCheckAction extends AbstractAction
	{
		ConfigureSecurityCheckAction()
		{
			super( "Configure" );
			putValue( Action.SHORT_DESCRIPTION, "Configures the selection assertion" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityCheckList.getSelectedIndex();
			if( ix == -1 )
				return;

			SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( securityCheck.isConfigurable() )
			{
				securityCheck.configure();
			}
			else
				Toolkit.getDefaultToolkit().beep();
		}
	}

	public class RemoveSecurityCheckActionAction extends AbstractAction
	{
		public RemoveSecurityCheckActionAction()
		{
			super( "Remove SecurityCheck" );
			putValue( Action.SHORT_DESCRIPTION, "Removes the selected security check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_securityCheck.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityCheckList.getSelectedIndex();
			if( ix == -1 )
				return;

			SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( UISupport.confirm( "Remove security check [" + securityCheck.getName() + "]", "Remove SecurityCheck" ) )
			{
				securable.removeSecurityCheck( securityCheck );
			}
		}
	}

	private class MoveSecurityCheckUpAction extends AbstractAction
	{
		public MoveSecurityCheckUpAction()
		{
			super( "Move SecurityCheck Up" );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected security check up one row" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/up_arrow.gif" ) );
			setEnabled( false );

		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityCheckList.getSelectedIndex();
			SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( ix != -1 )
			{
				securityCheck = securable.moveSecurityCheck( ix, -1 );
			}
			securityCheckList.setSelectedValue( securityCheck, true );
		}
	}

	private class MoveSecurityCheckDownAction extends AbstractAction
	{
		public MoveSecurityCheckDownAction()
		{
			super( "Move SecurityCheck Down" );
			putValue( Action.SHORT_DESCRIPTION, "Moves selected security check down one row" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/down_arrow.gif" ) );
			setEnabled( false );

		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityCheckList.getSelectedIndex();
			SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( ix != -1 )
			{
				securityCheck = securable.moveSecurityCheck( ix, 1 );
			}
			securityCheckList.setSelectedValue( securityCheck, true );
		}
	}
}
