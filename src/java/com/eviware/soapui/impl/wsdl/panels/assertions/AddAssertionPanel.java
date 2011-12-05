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
package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.recent.RecentAssertionHandler;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class AddAssertionPanel extends SimpleDialog
{
	private JXList categoriesList;
	private JXTable assertionsTable;
	private Assertable assertable;
	private AddAssertionAction addAssertionAction;
	private AssertionsListTableModel assertionsListTableModel;
	//	private JPanel assertionListPanel;
	private SortedSet<AssertionListEntry> assertions;
	private ListSelectionListener selectionListener;
	private LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap;
	private SimpleForm assertionsForm;
	private JCheckBox hideDescCB;
	private InternalCellRenderer assertionEntryRenderer = new InternalCellRenderer();
	private InternalHideDescListener hideDescListener = new InternalHideDescListener();
	protected RecentAssertionHandler recentAssertionHandler = new RecentAssertionHandler();
	AssertionListMouseAdapter mouseAdapter = new AssertionListMouseAdapter();

	public AddAssertionPanel( Assertable assertable )
	{
		super( "Select Assertion", "Select which assertion to add", "" );
		this.assertable = assertable;
		selectionListener = new InternalListSelectionListener();
		categoriesAssertionsMap = AssertionCategoryMapping
				.getCategoriesAssertionsMap( assertable, recentAssertionHandler );
	}

	@Override
	protected Component buildContent()
	{
		JPanel mainPanel = new JPanel( new BorderLayout() );
		JSplitPane splitPane = UISupport.createHorizontalSplit( buildCategoriesList(), buildAssertionsList() );
		JXToolBar toolbar = UISupport.createSmallToolbar();
		hideDescCB = new JCheckBox( "Hide descriptions" );
		hideDescCB.setOpaque( false );
		hideDescCB.addItemListener( hideDescListener );
		toolbar.add( new JLabel( "Assertions" ) );
		toolbar.addGlue();
		toolbar.add( hideDescCB );
		splitPane.setDividerLocation( 0.4 );

		mainPanel.add( toolbar, BorderLayout.NORTH );
		mainPanel.add( splitPane, BorderLayout.CENTER );
		return mainPanel;
	}

	private Component buildAssertionsList()
	{
		assertionsForm = new SimpleForm();

		assertionsListTableModel = new AssertionsListTableModel();
		assertionsTable = new JXTable( assertionsListTableModel );
		String category = ( String )categoriesList.getSelectedValue();
		if( category != null && categoriesAssertionsMap.containsKey( category ) )
		{
			assertions = categoriesAssertionsMap.get( category );
			assertionsListTableModel.setListEntriesSet( assertions );
		}
		assertionsTable.setTableHeader( null );
		assertionsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		assertionsTable.getSelectionModel().addListSelectionListener( selectionListener );
		assertionsTable.setEditable( false );
		assertionsTable.setGridColor( Color.BLACK );
		assertionsTable.setRowHeight( 40 );
		assertionsTable.addMouseListener( mouseAdapter );

		assertionsTable.getColumnModel().getColumn( 0 ).setCellRenderer( assertionEntryRenderer );
		assertionsForm.addComponent( assertionsTable );
		return new JScrollPane( assertionsForm.getPanel() );
	}

	private Component buildCategoriesList()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		AssertionCategoriesListModel listModel = new AssertionCategoriesListModel( categoriesAssertionsMap.keySet() );
		categoriesList = new JXList( listModel );
		categoriesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		categoriesList.setSelectedIndex( 0 );
		categoriesList.addListSelectionListener( new ListSelectionListener()
		{
			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				String category = ( String )categoriesList.getSelectedValue();
				if( category != null && categoriesAssertionsMap.containsKey( category ) )
				{
					assertions = categoriesAssertionsMap.get( category );
					assertionsListTableModel.setListEntriesSet( assertions );
					assertionsTable.getColumnModel().getColumn( 0 ).setCellRenderer( assertionEntryRenderer );
					assertionsListTableModel.fireTableDataChanged();
				}
			}
		} );
		panel.add( new JScrollPane( categoriesList ) );
		return panel;
	}

	@Override
	protected boolean handleOk()
	{
		setVisible( false );

		int selectedRow = assertionsTable.getSelectedRow();
		String selection = ( ( AssertionListEntry )assertionsListTableModel.getValueAt( selectedRow, 0 ) ).getName();
		if( selection == null )
			return false;

		if( !TestAssertionRegistry.getInstance().canAddMultipleAssertions( selection, assertable ) )
		{
			UISupport.showErrorMessage( "This assertion can only be added once" );
			return false;
		}

		TestAssertion assertion = assertable.addAssertion( selection );
		if( assertion == null )
		{
			UISupport.showErrorMessage( "Failed to add assertion" );
			return false;
		}

		recentAssertionHandler.add( selection );

		if( assertion.isConfigurable() )
		{
			assertion.configure();
			return true;
		}

		return true;
	}

	@Override
	public ActionList buildActions( String url, boolean okAndCancel )
	{
		DefaultActionList actions = new DefaultActionList( "Actions" );
		if( url != null )
			actions.addAction( new HelpAction( url ) );

		addAssertionAction = new AddAssertionAction();
		actions.addAction( addAssertionAction );
		if( okAndCancel )
		{
			actions.addAction( new CancelAction() );
			actions.setDefaultAction( addAssertionAction );
		}
		return actions;
	}

	protected final class AddAssertionAction extends AbstractAction
	{
		public AddAssertionAction()
		{
			super( "Add" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			handleOk();
		}
	}

	private class InternalListSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged( ListSelectionEvent e )
		{
			if( assertionsTable.getSelectedRow() >= 0 )
			{
				addAssertionAction.setEnabled( true );
			}
			else
			{
				addAssertionAction.setEnabled( false );
			}
		}
	}

	private class InternalHideDescListener implements ItemListener
	{
		@Override
		public void itemStateChanged( ItemEvent arg0 )
		{
			assertionsTable.getColumnModel().getColumn( 0 ).setCellRenderer( assertionEntryRenderer );
			assertionsListTableModel.fireTableDataChanged();
		}
	}

	public void release()
	{
		assertionsTable.getSelectionModel().removeListSelectionListener( selectionListener );
		assertionsTable.removeMouseListener( mouseAdapter );
		hideDescCB.removeItemListener( hideDescListener );
	}

	private class InternalCellRenderer extends DefaultCellRenderer
	{

		private Font boldFont;

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column )
		{

			boldFont = getFont().deriveFont( Font.BOLD );

			String str = ( ( AssertionListEntry )value ).getName();
			JLabel label = new JLabel( str );
			label.setFont( boldFont );

			JLabel desc = new JLabel( ( ( AssertionListEntry )value ).getDescription() );
			SimpleForm form = new SimpleForm();

			form.addComponent( label );
			if( !hideDescCB.isSelected() )
			{
				form.addComponent( desc );
				assertionsTable.setRowHeight( 40 );
			}
			else
			{
				assertionsTable.setRowHeight( 20 );
			}

			if( isSelected )
			{
				form.getPanel().setBackground( Color.LIGHT_GRAY );
			}
			else
			{
				form.getPanel().setBackground( Color.WHITE );
			}
			return form.getPanel();
		}
	}

	@Override
	protected void beforeShow()
	{
		setSize( new Dimension( 650, 500 ) );
	}

	public void setCategoriesAssertionsMap( LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap )
	{
		this.categoriesAssertionsMap = categoriesAssertionsMap;
	}

	public class AssertionListMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked( MouseEvent e )
		{
			if( e.getClickCount() == 2 )
			{
				handleOk();
			}
		}
	}

	public JXTable getAssertionsTable()
	{
		return assertionsTable;
	}

	public AddAssertionAction getAddAssertionAction()
	{
		return addAssertionAction;
	}

	public void setSelectionListener( ListSelectionListener selectionListener )
	{
		this.selectionListener = selectionListener;
	}

}
