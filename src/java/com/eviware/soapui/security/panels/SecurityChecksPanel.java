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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.actions.AddAssertionAction;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.actions.CloneParametersAction;
import com.eviware.soapui.security.check.AbstractSecurityCheck;
import com.eviware.soapui.security.check.AbstractSecurityCheckWithProperties;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;

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
	private final TestStep testStep;
	private SecurityTest securityTest;
	private AddSecurityCheckAction addSecurityCheckAction;
	private ConfigureSecurityCheckAction configureSecurityCheckAction;
	private RemoveSecurityCheckAction removeSecurityCheckAction;
	private CloneParametersAction cloneParametersAction;
	// private MoveSecurityCheckUpAction moveSecurityCheckUpAction;
	// private MoveSecurityCheckDownAction moveSecurityCheckDownAction;
	// private DefaultListModel listModel;
	// private JList securityChecksList;
	// JSplitPane splitPane;
	private JPanel securityCheckConfigPanel;

	public SecurityChecksPanel( TestStep testStep, SecurityTest securityTest )
	{
		super( new BorderLayout() );
		this.testStep = testStep;
		this.securityTest = securityTest;

		securityCheckListModel = new SecurityCheckListModel();
		securityCheckList = new JList( securityCheckListModel );
		securityTest.setListModel( securityCheckListModel );
		securityCheckList.setCellRenderer( new SecurityCheckCellRenderer() );
		securityCheckList.setToolTipText( "SecurityChecks for this TestStep" );
		securityCheckList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		securityCheckListPopup = new JPopupMenu();
		addSecurityCheckAction = new AddSecurityCheckAction();
		securityCheckListPopup.add( addSecurityCheckAction );

		securityCheckListPopup.addPopupMenuListener( new PopupMenuListener()
		{

			public void popupMenuWillBecomeVisible( PopupMenuEvent e )
			{
				while( securityCheckListPopup.getComponentCount() > 1 )
					securityCheckListPopup.remove( 1 );

				int ix = securityCheckList.getSelectedIndex();
				if( ix >= 0 )
				{

					securityCheckListPopup.add( configureSecurityCheckAction );
					SecurityCheck check = securityCheckListModel.getSecurityCheckAt( ix );
					if( check instanceof AbstractSecurityCheckWithProperties )
					{
						// cloneParametersAction = new CloneParametersAction();
						cloneParametersAction.setSecurityCheck( ( AbstractSecurityCheckWithProperties )check );
						securityCheckListPopup.add( cloneParametersAction );
					}
					securityCheckListPopup.addSeparator();
					securityCheckListPopup.add( removeSecurityCheckAction );
					securityCheckListPopup.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );

				}
				else
				{
					securityCheckListPopup.addSeparator();
					securityCheckListPopup.add( new ShowOnlineHelpAction( HelpUrls.RESPONSE_ASSERTIONS_HELP_URL ) );
				}
			}

			public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
			{
			}

			public void popupMenuCanceled( PopupMenuEvent e )
			{
			}
		} );

		securityCheckList.setComponentPopupMenu( securityCheckListPopup );

		securityCheckList.addListSelectionListener( new ListSelectionListener()
		{

			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				int ix = securityCheckList.getSelectedIndex();

				// configureSecurityCheckAction.setEnabled( ix >= 0 );
				removeSecurityCheckAction.setEnabled( ix >= 0 );
				SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
				configureSecurityCheckAction.setEnabled( securityCheck != null && securityCheck.isConfigurable() );
				if( securityCheck instanceof AbstractSecurityCheckWithProperties )
				{
					cloneParametersAction.setSecurityCheck( ( AbstractSecurityCheckWithProperties )securityCheck );
					cloneParametersAction.setEnabled( true );
				}
				else
				{
					cloneParametersAction.setEnabled( false );
				}
				// moveSecurityCheckUpAction.setEnabled( ix >= 0 );
				// moveSecurityCheckDownAction.setEnabled( ix >= 0 );

				if( ix == -1 )
					return;
			}
		} );
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
				if( obj instanceof SecurityCheck )
				{
					AbstractSecurityCheck chck = ( AbstractSecurityCheck )obj;
					chck.setTestStep( getTestStep() );
					if( chck.isConfigurable() )
					{
						XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
								.buildSecurityCheckConfigurationDialog( chck );

						dialog.show();
					}

					return;
				}
			}

		} );

		JScrollPane listScrollPane = new JScrollPane( securityCheckList );
		UISupport.addTitledBorder( listScrollPane, "Security Checks" );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( listScrollPane, BorderLayout.CENTER );
		p.add( createPropertiesToolbar(), BorderLayout.NORTH );

		securityCheckConfigPanel = ( JPanel )buildConfigPanel();
		add( p, BorderLayout.CENTER );

	}

	protected TestStep getTestStep()
	{
		return testStep;
	}

	private JComponent buildConfigPanel()
	{
		securityCheckConfigPanel = UISupport.addTitledBorder( new JPanel( new BorderLayout() ), "Configuration" );
		securityCheckConfigPanel.add( new JLabel( "currently no security checks" ) );
		if( securityCheckList != null && securityCheckList.getSelectedValue() != null )
		{
			SecurityCheck selected = securityTest.getTestStepSecurityCheckByName( testStep.getId(),
					( ( AbstractSecurityCheck )securityCheckList.getSelectedValue() ).getName() );
			securityCheckConfigPanel.removeAll();
			securityCheckConfigPanel.add( selected.getComponent() );
		}
		securityCheckConfigPanel.revalidate();
		return securityCheckConfigPanel;
	}

	public AbstractSecurityCheck getCurrentSecurityCheck()
	{
		int ix = securityCheckList.getSelectedIndex();
		return ix == -1 ? null : securityTest.getTestStepSecurityCheckAt( testStep.getId(), ix );
	}

	protected JXToolBar createPropertiesToolbar()
	{
		JXToolBar checksToolbar = UISupport.createSmallToolbar();
		addSecurityCheckAction = new AddSecurityCheckAction();
		configureSecurityCheckAction = new ConfigureSecurityCheckAction();
		removeSecurityCheckAction = new RemoveSecurityCheckAction();
		cloneParametersAction = new CloneParametersAction();
		// moveSecurityCheckUpAction = new MoveSecurityCheckUpAction();
		// moveSecurityCheckDownAction = new MoveSecurityCheckDownAction();
		addToolbarButtons( checksToolbar );
		securityCheckList.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int ix = securityCheckList.getSelectedIndex();

				configureSecurityCheckAction.setEnabled( ix >= 0 );
				removeSecurityCheckAction.setEnabled( ix >= 0 );
				// moveSecurityCheckUpAction.setEnabled( ix >= 0 );
				// moveSecurityCheckDownAction.setEnabled( ix >= 0 );

				if( ix == -1 )
					return;
				SecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
				configureSecurityCheckAction.setEnabled( securityCheck != null && securityCheck.isConfigurable() );
				if( securityCheck instanceof AbstractSecurityCheckWithProperties )
				{
					cloneParametersAction.setSecurityCheck( ( AbstractSecurityCheckWithProperties )securityCheck );
					cloneParametersAction.setEnabled( true );
				}
				else
				{
					cloneParametersAction.setEnabled( false );
				}
			}
		} );
		return checksToolbar;
	}

	protected void addToolbarButtons( JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( addSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( configureSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( removeSecurityCheckAction ) );
		toolbar.addFixed( UISupport.createToolbarButton( cloneParametersAction ) );
		// toolbar.addFixed( UISupport.createToolbarButton(
		// moveSecurityCheckUpAction ) );
		// toolbar.addFixed( UISupport.createToolbarButton(
		// moveSecurityCheckDownAction ) );
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
				AbstractSecurityCheck securityCheck = ( AbstractSecurityCheck )value;
				setText( securityCheck.getName() );
				setIcon( securityCheck.getIcon() );

				if( securityCheck.isDisabled() && isEnabled() )
					setEnabled( false );
			}
			// else if( value instanceof AssertionError )
			// {
			// AssertionError assertion = ( AssertionError )value;
			// setText( " -> " + assertion.toString() );
			// setIcon( null );
			// }
			// else if( value instanceof String )
			// {
			// setText( value.toString() );
			// }

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

	public class SecurityCheckListModel extends DefaultListModel
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

		public AbstractSecurityCheck getSecurityCheckAt( int index )
		{
			Object object = items.get( index );
			while( !( object instanceof SecurityCheck ) && index > 0 )
			{
				object = items.get( --index );
			}

			return ( AbstractSecurityCheck )( ( object instanceof SecurityCheck ) ? object : null );
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
				AbstractSecurityCheck securityCheck = securityTest.getTestStepSecurityCheckAt( testStep.getId(), c );
				securityCheck.setTestStep( testStep );
				addSecurityCheck( securityCheck );
			}
		}

		public void release()
		{
			items.clear();

			for( int c = 0; c < securityTest.getTestStepSecurityChecksCount( testStep.getId() ); c++ )
			{
				SecurityCheck securityCheck = securityTest.getTestStepSecurityCheckAt( testStep.getId(), c );
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

		public void securityCheckAdded( AbstractSecurityCheck securityCheck )
		{
			synchronized( this )
			{
				int sz = getSize();
				addSecurityCheck( securityCheck );

				fireIntervalAdded( this, sz, items.size() - 1 );
			}
		}

		private void addSecurityCheck( AbstractSecurityCheck securityCheck )
		{
			items.add( securityCheck );

			// AssertionError[] errors = securityCheck.getErrors();
			// if( errors != null )
			// {
			// for( int i = 0; i < errors.length; i++ )
			// items.add( errors[i] );
			// }
		}

		public void securityCheckRemoved( AbstractSecurityCheck securityCheck )
		{
			synchronized( this )
			{
				int ix = items.indexOf( securityCheck );
				if( ix == -1 )
					return;

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

		public void securityCheckMoved( AbstractSecurityCheck newSecurityCheck, int ix, int offset )
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

				items.remove( ix );
				fireIntervalRemoved( this, ix, ix );

				// remove associated errors
				// while( ix < items.size() && items.get( ix ) instanceof
				// AssertionError )
				// {
				// items.remove( ix );
				// fireIntervalRemoved( this, ix, ix );
				// }
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
			String[] availableChecksNames = SoapUI.getSoapUICore().getSecurityCheckRegistry()
					.getAvailableSecurityChecksNames( testStep );
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

			SecurityCheck securityCheck = securityTest.addSecurityCheck( testStep, type, name );

			if( securityCheck == null )
			{
				UISupport.showErrorMessage( "Failed to add security check" );
				return;
			}
			securityCheckList.setSelectedIndex( securityCheckListModel.getSize() - 1 );
			AbstractSecurityCheck secCheck = getCurrentSecurityCheck();
			// secCheck.setTestStep( testStep );

			XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
					.buildSecurityCheckConfigurationDialog( secCheck );

			dialog.show();

		}

	}

	public class ConfigureSecurityCheckAction extends AbstractAction
	{
		ConfigureSecurityCheckAction()
		{
			super( "Configure" );
			putValue( Action.SHORT_DESCRIPTION, "Configures selected security check" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int ix = securityCheckList.getSelectedIndex();
			if( ix == -1 )
				return;

			AbstractSecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( securityCheck.isConfigurable() )
			{
				XFormDialog dialog = SoapUI.getSoapUICore().getSecurityCheckRegistry().getUIBuilder()
						.buildSecurityCheckConfigurationDialog( securityCheck );

				dialog.show();
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

			AbstractSecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			if( UISupport.confirm( "Remove security check [" + securityCheck.getName() + "]", "Remove SecurityCheck" ) )
			{
				securityTest.removeSecurityCheck( testStep, securityCheck );
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
			AbstractSecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			AbstractSecurityCheck newSecurityCheck = null;
			if( ix > 0 )
			{
				if( ix != -1 )
				{
					newSecurityCheck = securityTest.moveTestStepSecurityCheck( testStep, securityCheck, ix, -1 );
				}
				if( newSecurityCheck != null )
				{
					securityCheckList.setSelectedValue( newSecurityCheck, true );
				}
			}
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
			AbstractSecurityCheck securityCheck = securityCheckListModel.getSecurityCheckAt( ix );
			AbstractSecurityCheck newSecurityCheck = null;
			if( ix < securityCheckListModel.getSize() - 1 )
			{
				if( ix != -1 )
				{
					newSecurityCheck = securityTest.moveTestStepSecurityCheck( testStep, securityCheck, ix, 1 );
				}
				if( newSecurityCheck != null )
				{
					securityCheckList.setSelectedValue( newSecurityCheck, true );
				}
			}
		}
	}
}
