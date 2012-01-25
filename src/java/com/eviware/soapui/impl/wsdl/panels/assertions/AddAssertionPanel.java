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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.recent.RecentAssertionHandler;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.settings.AssertionDescriptionSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class AddAssertionPanel extends SimpleDialog
{
	private JXList categoriesList;
	private AssertionsListTable assertionsTable;
	private Assertable assertable;
	private AddAssertionAction addAssertionAction;
	private AssertionsListTableModel assertionsListTableModel;
	//	private JPanel assertionListPanel;
	private SortedSet<AssertionListEntry> assertions;
	private ListSelectionListener selectionListener;
	private LinkedHashMap<String, SortedSet<AssertionListEntry>> categoriesAssertionsMap;
	private SimpleForm assertionsForm;
	private JCheckBox hideDescCB;
	private AssertionEntryRenderer assertionEntryRenderer = new AssertionEntryRenderer();;
	private InternalHideDescListener hideDescListener = new InternalHideDescListener();
	protected RecentAssertionHandler recentAssertionHandler = new RecentAssertionHandler();
	private AssertionListMouseAdapter mouseAdapter = new AssertionListMouseAdapter();
	private String selectedCategory;

	public AddAssertionPanel( Assertable assertable )
	{
		super( "Select Assertion", "Select which assertion to add", "" );
		this.assertable = assertable;
		assertionEntryRenderer.setAssertable( assertable );
		selectionListener = new InternalListSelectionListener();
		categoriesAssertionsMap = AssertionCategoryMapping
				.getCategoriesAssertionsMap( assertable, recentAssertionHandler );
	}

	public RecentAssertionHandler getRecentAssertionHandler()
	{
		return recentAssertionHandler;
	}

	public AssertionEntryRenderer getAssertionEntryRenderer()
	{
		return assertionEntryRenderer;
	}

	public String getSelectedCategory()
	{
		return selectedCategory;
	}

	public void setAssertable( Assertable assertable )
	{
		this.assertable = assertable;
	}

	public Assertable getAssertable()
	{
		return assertable;
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
		hideDescCB
				.setSelected( SoapUI.getSettings().getBoolean( AssertionDescriptionSettings.SHOW_ASSERTION_DESCRIPTION ) );
		toolbar.add( new JLabel( "Assertions" ) );
		toolbar.addGlue();
		toolbar.add( hideDescCB );
		splitPane.setDividerLocation( 0.4 );

		mainPanel.add( toolbar, BorderLayout.NORTH );
		mainPanel.add( splitPane, BorderLayout.CENTER );
		return mainPanel;
	}

	public AssertionListMouseAdapter getMouseAdapter()
	{
		return mouseAdapter;
	}

	protected Component buildAssertionsList()
	{
		assertionsForm = new SimpleForm();

		assertionsListTableModel = new AssertionsListTableModel();
		assertionsTable = new AssertionsListTable( assertionsListTableModel );
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
				selectedCategory = ( String )categoriesList.getSelectedValue();
				if( selectedCategory != null && categoriesAssertionsMap.containsKey( selectedCategory ) )
				{
					assertions = categoriesAssertionsMap.get( selectedCategory );
					assertionsListTableModel.setListEntriesSet( assertions );
					renderAssertions();
					populateNonSelectableIndexes();
					assertionsListTableModel.fireTableDataChanged();
				}
			}
		} );
		panel.add( new JScrollPane( categoriesList ) );
		return panel;
	}

	protected void renderAssertions()
	{
	}

	protected void populateNonSelectableIndexes()
	{
		SortedSet<AssertionListEntry> assertionsList = getCategoriesAssertionsMap().get( getSelectedCategory() );
		List<Integer> intList = new ArrayList<Integer>();
		assertionsList.toArray();
		for( int i = 0; i < assertionsList.size(); i++ )
		{
			AssertionListEntry assertionListEntry = ( AssertionListEntry )assertionsList.toArray()[i];
			if( !TestAssertionRegistry.getInstance().canAssert( assertionListEntry.getTypeId(), getAssertable() ) )
				intList.add( i );
		}
		getAssertionsTable().setNonSelectableIndexes( intList );
	}

	protected void enableCategoriesList( boolean enable )
	{
		categoriesList.setEnabled( enable );
	}

	protected void enableApplicableAssertions()
	{

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
			SoapUI.getSettings().setBoolean( AssertionDescriptionSettings.SHOW_ASSERTION_DESCRIPTION,
					arg0.getStateChange() == ItemEvent.SELECTED );
		}
	}

	public void release()
	{
		assertionsTable.getSelectionModel().removeListSelectionListener( selectionListener );
		assertionsTable.removeMouseListener( mouseAdapter );
		hideDescCB.removeItemListener( hideDescListener );
	}

	protected class AssertionEntryRenderer extends DefaultCellRenderer
	{
		private Assertable assertable;
		private Font boldFont;

		public void setAssertable( Assertable assertable )
		{
			this.assertable = assertable;
		}

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column )
		{

			boldFont = getFont().deriveFont( Font.BOLD );

			AssertionListEntry entry = ( AssertionListEntry )value;
			String type = TestAssertionRegistry.getInstance().getAssertionTypeForName( entry.getName() );
			boolean canAssert = assertable != null ? TestAssertionRegistry.getInstance().canAssert( type, assertable )
					: true;

			String str = entry.getName();
			JLabel label = new JLabel( str );
			label.setFont( boldFont );
			JLabel desc = new JLabel( ( ( AssertionListEntry )value ).getDescription() );
			JLabel disabledInfo = new JLabel( "Not applicable with selected Source and Property" );
			boolean disable = !categoriesList.isEnabled() || !canAssert;
			if( disable )
			{
				label.setForeground( Color.LIGHT_GRAY );
				desc.setForeground( Color.LIGHT_GRAY );
				disabledInfo.setForeground( Color.LIGHT_GRAY );
			}
			SimpleForm form = new SimpleForm();
			form.addComponent( label );
			if( !isHideDescriptionSelected() )
			{
				form.addComponent( desc );
				if( disable )
				{
					form.addComponent( disabledInfo );
				}
				getAssertionsTable().setRowHeight( 60 );
			}
			else
			{
				if( disable )
				{
					form.addComponent( disabledInfo );
				}
				getAssertionsTable().setRowHeight( 40 );
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

	protected boolean isHideDescriptionSelected()
	{
		return hideDescCB.isSelected();
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

	public LinkedHashMap<String, SortedSet<AssertionListEntry>> getCategoriesAssertionsMap()
	{
		return categoriesAssertionsMap;
	}

	public class AssertionListMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked( MouseEvent e )
		{
			if( e.getClickCount() == 2 && !assertionsTable.getSelectionModel().isSelectionEmpty() )
			{
				handleOk();
			}
		}
	}

	public AssertionsListTable getAssertionsTable()
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
