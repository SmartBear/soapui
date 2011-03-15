/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.monitor.MonitorSecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * 
 * @author dragica.soldo
 * 
 */
public class SecurityTestsMonitorDesktopPanel extends JPanel
{
	private final MonitorSecurityTest monitorSecurityTest;
	private DefaultListModel listModel;
	private JPanel securityCheckConfigPanel;
	private JList securityChecksList;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton renameButton;
	private JToggleButton disableButton;
	JSplitPane splitPane;

	public SecurityTestsMonitorDesktopPanel( MonitorSecurityTest securityTest )
	{
		super( new BorderLayout() );
		// super( securityTest );
		this.monitorSecurityTest = securityTest;
		// componentEnabler = new MonitorSecurityCheckEnabler(
		// securityTest.getTestCase() );

		buildUI();

	}

	protected void buildUI()
	{
		listModel = new DefaultListModel();

		securityChecksList = new JList( listModel );
		securityChecksList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		securityChecksList.addListSelectionListener( new ListSelectionListener()
		{

			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				splitPane.remove( splitPane.getRightComponent() );
				splitPane.setRightComponent( buildConfigPanel() );
				revalidate();
				setSelectedCheck( getCurrentSecurityCheck() );
			}
		} );
		JScrollPane listScrollPane = new JScrollPane( securityChecksList );
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

	}

	public AbstractSecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityChecksList.getSelectedIndex();
		return ix == -1 ? null : monitorSecurityTest.getSecurityCheckAt( ix );
	}

	public int getCurrentSecurityCheckIndex()
	{
		return securityChecksList.getSelectedIndex();
	}

	protected JXToolBar createPropertiesToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();
		toolbar.addFixed( UISupport.createToolbarButton( new AddAction() ) );
		deleteButton = UISupport.createToolbarButton( new DeleteAction() );
		deleteButton.setEnabled( false );
		toolbar.addFixed( deleteButton );
		copyButton = UISupport.createToolbarButton( new CopyAction() );
		copyButton.setEnabled( false );
		toolbar.addFixed( copyButton );
		renameButton = UISupport.createToolbarButton( new RenameAction() );
		renameButton.setEnabled( false );
		toolbar.addFixed( renameButton );

		disableButton = new JToggleButton( new DisableAction() );
		disableButton.setPreferredSize( UISupport.TOOLBAR_BUTTON_DIMENSION );
		disableButton.setSelectedIcon( UISupport.createImageIcon( "/bullet_red.png" ) );
		disableButton.setEnabled( false );
		toolbar.addSeparator();
		toolbar.addFixed( disableButton );

		return toolbar;
	}

	private void setSelectedCheck( SecurityCheck securityCheck )
	{
		if( securityCheck != null )
		{
			// securityCheck.addPropertyChangeListener(
			// transferPropertyChangeListener );

			disableButton.setSelected( securityCheck.isDisabled() );
		}

		copyButton.setEnabled( securityCheck != null );
		renameButton.setEnabled( securityCheck != null );
		deleteButton.setEnabled( securityCheck != null );
		disableButton.setEnabled( securityCheck != null );

	}

	private JComponent buildConfigPanel()
	{
		securityCheckConfigPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		// securityCheckConfigPanel.setPreferredSize( new Dimension( 330, 400 ) );
		securityCheckConfigPanel.add( new JLabel( "currently no security checks" ) );
		// securityCheckConfigPanel = new securityCheckConfigPanel.setText(
		// "currently no security checks" );
		// panel.add( securityCheckConfigPanel );
		if( securityChecksList != null && securityChecksList.getSelectedValue() != null )
		{
			SecurityCheck selected = monitorSecurityTest.getSecurityCheckByName( ( String )securityChecksList
					.getSelectedValue() );
			securityCheckConfigPanel.removeAll();
			securityCheckConfigPanel.add( selected.getComponent() );
		}
		securityCheckConfigPanel.revalidate();
		return securityCheckConfigPanel;
	}

	private final class AddAction extends AbstractAction
	{
		public AddAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Adds a new Security Check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String[] availableChecksNames = SoapUI.getSoapUICore().getSecurityCheckRegistry().getAvailableSecurityChecksNames( true );
			String type = UISupport.prompt( "Specify type of security check", "Add SecurityCheck", availableChecksNames );
			if( type == null || type.trim().length() == 0 )
				return;

			String name = UISupport
					.prompt( "Specify name for security check", "Add SecurityCheck", findUniqueName( type ) );
			if( name == null || name.trim().length() == 0 )
				return;
			while( monitorSecurityTest.getSecurityCheckByName( name ) != null
					|| monitorSecurityTest.getSecurityCheckByName( name + " (disabled)" ) != null )
			{
				name = UISupport.prompt( "Specify unique name for check", "Add SecurityCheck", name + " "
						+ ( monitorSecurityTest.getMonitorSecurityChecksList().size() ) );
				if( name == null )
				{
					return;
				}
			}

			monitorSecurityTest.addSecurityCheck( name, type );

			listModel.addElement( name );
			securityChecksList.setSelectedIndex( listModel.getSize() - 1 );
		}
	}

	private final class CopyAction extends AbstractAction
	{
		public CopyAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Copies the selected Security Check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/clone_request.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityChecksList.getSelectedIndex();
			AbstractSecurityCheck sourceCheck = monitorSecurityTest.getSecurityCheckAt( ix );

			String name = UISupport.prompt( "Specify name for SecurityCheck", "Copy SecurityCheck", "Copy of "
					+ sourceCheck.getName() );
			if( name == null || name.trim().length() == 0 )
				return;
			while( monitorSecurityTest.getSecurityCheckByName( name ) != null
					|| monitorSecurityTest.getSecurityCheckByName( name + " (disabled)" ) != null )
			{
				name = UISupport.prompt( "Specify unique name for check", "Rename SecurityCheck", name + " "
						+ ( monitorSecurityTest.getMonitorSecurityChecksList().size() ) );
				if( name == null )
				{
					return;
				}
			}
			SecurityCheck securityCheck = monitorSecurityTest.addSecurityCheck( name, sourceCheck );
			securityCheck.setDisabled( sourceCheck.isDisabled() );

			listModel.addElement( name );
			securityChecksList.setSelectedIndex( listModel.getSize() - 1 );
		}
	}

	private final class DeleteAction extends AbstractAction
	{
		public DeleteAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Deletes the selected Security Check" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Delete selected security check", "Delete Security Check" ) )
			{
				securityChecksList.setSelectedIndex( -1 );

				int ix = securityChecksList.getSelectedIndex();
				monitorSecurityTest.removeSecurityCheckAt( ix );
				listModel.remove( ix );

				if( listModel.getSize() > 0 )
				{
					securityChecksList.setSelectedIndex( ix > listModel.getSize() - 1 ? listModel.getSize() - 1 : ix );
				}
			}
		}
	}

	private final class RenameAction extends AbstractAction
	{
		public RenameAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/rename.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Renames the selected Security Check" );
		}

		public void actionPerformed( ActionEvent e )
		{
			AbstractSecurityCheck securityCheck = getCurrentSecurityCheck();

			String oldName = securityCheck.getName();
			String newName = UISupport.prompt( "Specify new name for security check", "Rename SecurityCheck",
					securityCheck.getName() );

			while( !( newName.equals( oldName ) )
					&& ( monitorSecurityTest.getSecurityCheckByName( newName ) != null || monitorSecurityTest
							.getSecurityCheckByName( newName + " (disabled)" ) != null ) )
			{
				newName = UISupport.prompt( "Specify unique name for check", "Rename SecurityCheck",
						findUniqueName( securityCheck.getType() ) );
				if( newName == null )
				{
					return;
				}
			}
			if( newName != null && !securityCheck.getName().equals( newName ) )
			{
				securityCheck.setName( newName );
				if( securityCheck.isDisabled() )
				{
					newName += " (disabled)";
				}
				listModel.setElementAt( newName, securityChecksList.getSelectedIndex() );
			}
		}
	}

	private final class DisableAction extends AbstractAction
	{
		public DisableAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/bullet_green.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Disables the selected Security Check" );
		}

		public void actionPerformed( ActionEvent e )
		{
			AbstractSecurityCheck securityCheck = getCurrentSecurityCheck();
			securityCheck.setDisabled( disableButton.isSelected() );

			String name = securityCheck.getName();
			if( securityCheck.isDisabled() )
				name += " (disabled)";
			// securityCheck.setName( name );
			// monitorSecurityTest.renameSecurityCheckAt(
			// getCurrentSecurityCheckIndex(), name );

			listModel.setElementAt( name, securityChecksList.getSelectedIndex() );
		}
	}

	private String findUniqueName( String type )
	{
		String name = type;
		int numNames = 0;
		for( SecurityCheck existingCheck : monitorSecurityTest.getMonitorSecurityChecksList() )
		{
			if( existingCheck.getType().equals( name ) )
				numNames++ ;
		}
		if( numNames != 0 )
		{
			name += " " + numNames;
		}
		return name;
	}

}
