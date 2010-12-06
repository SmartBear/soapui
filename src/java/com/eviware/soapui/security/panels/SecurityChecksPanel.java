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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestListener;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * Separate panel for holding/managing securityChecks
 * 
 * @author dragica.soldo
 */

public class SecurityChecksPanel extends JPanel
{
	private SecurityCheckListModel securityCheckListModel;
	private JList securityCheckList;
	private JPopupMenu securityCheckListPopup;
	private final TestStep testStep;
	private SecurityTest securityTest;
	private AddSecurityCheckAction addSecurityCheckAction;
	private ConfigureSecurityCheckAction configureSecurityCheckAction;
	private RemoveSecurityCheckAction removeSecurityCheckAction;
	private MoveSecurityCheckUpAction moveSecurityCheckUpAction;
	private MoveSecurityCheckDownAction moveSecurityCheckDownAction;
	// private DefaultListModel listModel;
	// private JList securityChecksList;
	JSplitPane splitPane;
	private JPanel securityCheckConfigPanel;

	public SecurityChecksPanel( TestStep testStep, SecurityTest securityTest )
	{
		super( new BorderLayout() );
		this.testStep = testStep;
		this.securityTest = securityTest;

		// securityTest.addPropertyChangeListener( listener );

		securityCheckListModel = new SecurityCheckListModel();
		securityCheckList = new JList( securityCheckListModel );
		securityCheckList.setCellRenderer( new SecurityCheckCellRenderer() );
		securityCheckList.setToolTipText( "SecurityChecks for this TestStep" );
		securityCheckList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		// securityTest.addPropertyChangeListener( securityCheckListModel );

		securityCheckList.addListSelectionListener( new ListSelectionListener()
		{

			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				splitPane.remove( splitPane.getRightComponent() );
				splitPane.setRightComponent( buildConfigPanel() );
				revalidate();

				int ix = securityCheckList.getSelectedIndex();

				removeSecurityCheckAction.setEnabled( ix >= 0 );
				moveSecurityCheckUpAction.setEnabled( ix >= 0 );
				moveSecurityCheckDownAction.setEnabled( ix >= 0 );

				if( ix == -1 )
					return;
			}
		} );

		JScrollPane listScrollPane = new JScrollPane( securityCheckList );
		UISupport.addTitledBorder( listScrollPane, "Security Checks" );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( listScrollPane, BorderLayout.CENTER );
		p.add( createPropertiesToolbar(), BorderLayout.NORTH );

		securityCheckConfigPanel = ( JPanel )buildConfigPanel();

		splitPane = UISupport.createHorizontalSplit( p, buildConfigPanel() );
		splitPane.setPreferredSize( new Dimension( 650, 500 ) );
		splitPane.setResizeWeight( 0.1 );
		splitPane.setDividerLocation( 120 );
		add( splitPane, BorderLayout.CENTER );

		add( new JScrollPane( splitPane ), BorderLayout.CENTER );
	}

	private JComponent buildConfigPanel()
	{
		securityCheckConfigPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		// securityCheckConfigPanel.setPreferredSize( new Dimension( 330, 400 ) );
//		securityCheckConfigPanel.add( new JLabel( "currently no security checks" ) );
		// securityCheckConfigPanel = new securityCheckConfigPanel.setText(
		// "currently no security checks" );
		// panel.add( securityCheckConfigPanel );
		if( securityCheckList != null && securityCheckList.getSelectedValue() != null )
		{
			SecurityCheck selected = securityTest.getTestStepSecurityCheckByName( testStep.getId(),
					( ( SecurityCheck )securityCheckList.getSelectedValue() ).getName() );
			securityCheckConfigPanel.removeAll();
			securityCheckConfigPanel.add( selected.getComponent( null ) );
		}
		securityCheckConfigPanel.revalidate();
		return securityCheckConfigPanel;
	}

	public SecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityCheckList.getSelectedIndex();
		return ix == -1 ? null : securityTest.getTestStepSecurityCheckAt( testStep.getId(), ix );
	}

	protected JXToolBar createPropertiesToolbar()
	{
		JXToolBar checksToolbar = UISupport.createSmallToolbar();
		addSecurityCheckAction = new AddSecurityCheckAction();
		// configureSecurityCheckAction = new ConfigureSecurityCheckAction();
		removeSecurityCheckAction = new RemoveSecurityCheckAction();
		moveSecurityCheckUpAction = new MoveSecurityCheckUpAction();
		moveSecurityCheckDownAction = new MoveSecurityCheckDownAction();
		addToolbarButtons( checksToolbar );
		return checksToolbar;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( addSecurityCheckAction ) );
		// toolbar.addFixed( UISupport.createToolbarButton(
		// configureSecurityCheckAction ) );
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

	private static class SecurityCheckCellRenderer extends JLabel implements ListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			setEnabled( list.isEnabled() );

			if( value instanceof SecurityCheck )
			{
				SecurityCheck securityCheck = ( SecurityCheck )value;
				setText( securityCheck.getName() );
				setIcon( securityCheck.getIcon() );

				if( securityCheck.isDisabled() && isEnabled() )
					setEnabled( false );
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

	private class SecurityCheckListModel extends DefaultListModel implements PropertyChangeListener,
			SecurityTestListener
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
			// securityTest.addSecurityChecksListener( this );

			for( int c = 0; c < securityTest.getTestStepSecurityChecksCount( testStep.getId() ); c++ )
			{
				SecurityCheck securityCheck = securityTest.getTestStepSecurityCheckAt( testStep.getId(), c );
				addSecurityCheck( securityCheck );
			}
		}

		public void release()
		{
			items.clear();

			for( int c = 0; c < securityTest.getTestStepSecurityChecksCount( testStep.getId() ); c++ )
			{
				SecurityCheck securityCheck = securityTest.getTestStepSecurityCheckAt( testStep.getId(), c );
				securityCheck.removePropertyChangeListener( this );
			}

			// securityTest.removeSecurityChecksListener( this );
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

		public void securityCheckAdded( TestStep testStep, SecurityCheck securityCheck )
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

		public void securityCheckRemoved( TestStep testStep, SecurityCheck securityCheck )
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
				// while( ix < items.size() && items.get( ix ) instanceof
				// AssertionError )
				// {
				// items.remove( ix );
				// fireIntervalRemoved( this, ix, ix );
				// }
			}
		}

		public void securityCheckMoved( TestStep testStep, SecurityCheck newSecurityCheck, int ix, int offset )
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
				// while( ix < items.size() && items.get( ix ) instanceof
				// AssertionError )
				// {
				// items.remove( ix );
				// fireIntervalRemoved( this, ix, ix );
				// }
				newSecurityCheck.addPropertyChangeListener( this );
				items.add( ix + offset, newSecurityCheck );
				fireIntervalAdded( this, ix + offset, ix + offset );
				// add associated errors
				// while( ix < items.size() && items.get( ix ) instanceof
				// AssertionError )
				// {
				// items.add( newSecurityCheck );
				// fireIntervalAdded( this, ix + offset, ix + offset );
				// }
			}
		}

	}

	public void release()
	{
		securityCheckListModel.release();
	}

	public class AddSecurityCheckAction extends AbstractAction
	{
		public AddSecurityCheckAction()
		{
			super( "Add SecurityCheck" );

			putValue( Action.SHORT_DESCRIPTION, "Adds a security check to this item" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/addSecurityCheck.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{

			String[] availableChecksNames = SecurityCheckRegistry.getInstance().getAvailableSecurityChecksNames();
			String type = UISupport.prompt( "Specify type of security check", "Add SecurityCheck", availableChecksNames );
			if( type == null || type.trim().length() == 0 )
				return;
			String name = UISupport.prompt( "Specify name for security check", "Add SecurityCheck", securityTest
					.findTestStepCheckUniqueName( testStep.getId(), type ) );
			if( name == null || name.trim().length() == 0 )
				return;

			while( securityTest.getTestStepSecurityCheckByName( testStep.getId(), name ) != null )
			{
				name = UISupport.prompt( "Specify unique name for check", "Add SecurityCheck", name + " "
						+ ( securityTest.getTestStepSecurityChecks( testStep.getId() ).size() ) );
				if( name == null )
				{
					return;
				}
			}

			if( availableChecksNames == null || availableChecksNames.length == 0 )
			{
				UISupport.showErrorMessage( "No security checks available for this message" );
				return;
			}

			SecurityCheck securityCheck = securityTest.addSecurityCheck( testStep.getId(), type, name );
			if( securityCheck == null )
			{
				UISupport.showErrorMessage( "Failed to add security check" );
				return;
			}
			securityCheckList.setSelectedIndex( securityCheckListModel.getSize() - 1 );

		}

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

	public class RemoveSecurityCheckAction extends AbstractAction
	{
		public RemoveSecurityCheckAction()
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
				securityTest.removeSecurityCheck( testStep.getId(), securityCheck );
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
			// int ix = securityCheckList.getSelectedIndex();
			// SecurityCheck securityCheck =
			// securityCheckListModel.getSecurityCheckAt( ix );
			// if( ix != -1 )
			// {
			// securityCheck = securityTest.moveSecurityCheck( ix, -1 );
			// }
			// securityCheckList.setSelectedValue( securityCheck, true );
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
			// int ix = securityCheckList.getSelectedIndex();
			// SecurityCheck securityCheck =
			// securityCheckListModel.getSecurityCheckAt( ix );
			// if( ix != -1 )
			// {
			// securityCheck = securityTest.moveSecurityCheck( ix, 1 );
			// }
			// securityCheckList.setSelectedValue( securityCheck, true );
		}
	}
}
