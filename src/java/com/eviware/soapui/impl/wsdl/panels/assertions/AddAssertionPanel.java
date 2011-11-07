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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
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
	private LinkedHashSet<AssertionListEntry> assertions;
	private InternalListSelectionListener selectionListener = new InternalListSelectionListener();
	private LinkedHashMap<String, LinkedHashSet<AssertionListEntry>> categoriesAssertionsMap;
	private SimpleForm assertionsForm;

	public AddAssertionPanel( Assertable assertable )
	{
		super( "Select Assertion", "Select which assertion to add", "" );
		this.assertable = assertable;
		categoriesAssertionsMap = AssertionCategoryMapping.getCategoriesAssertionsMap( assertable );

	}

	@Override
	protected Component buildContent()
	{
		JPanel mainPanel = new JPanel( new BorderLayout() );
		JSplitPane splitPane = UISupport.createHorizontalSplit( buildCategoriesList(), buildAssertionsList() );
		JXToolBar toolbar = UISupport.createSmallToolbar();
		JCheckBox checkBox = new JCheckBox();
		checkBox.setAction( new HideAssertionDescriptionsAction() );
		checkBox.setText( "Hide descriptions" );
		toolbar.add( new JLabel( "Assertions" ) );
		toolbar.addGlue();
		toolbar.add( checkBox );
		splitPane.setDividerLocation( 0.4 );

		mainPanel.add( toolbar, BorderLayout.NORTH );
		mainPanel.add( splitPane, BorderLayout.CENTER );
		//		mainPanel.add( buildSourcePanel(), BorderLayout.SOUTH );
		return mainPanel;
	}

	//	private Component buildSourcePanel()
	//	{
	//		SimpleForm form = new SimpleForm();
	//		JComboBox testStepCombo = new JComboBox( new String[] { "TestStep1", "TestStep2" } );
	//		testStepCombo.setEnabled( false );
	//		form.addLeftComponent( testStepCombo );
	//		JComboBox propertiesCombo = new JComboBox( new String[] { "Prop 1", "Prop 2" } );
	//		propertiesCombo.setEnabled( false );
	//		form.addRightComponent( propertiesCombo );
	//		return form.getPanel();
	//	}

	private Component buildAssertionsList()
	{
		assertionsForm = new SimpleForm();

		//		assertionListPanel = new JPanel( new BorderLayout() );
		assertionsListTableModel = new AssertionsListTableModel();
		assertionsTable = new JXTable( assertionsListTableModel );
		assertionsTable.setTableHeader( null );
		assertionsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		assertionsTable.getSelectionModel().addListSelectionListener( selectionListener );
		assertionsTable.setRowHeight( 50 );

		InternalCellRenderer assertionEntryRenderer = new InternalCellRenderer();
		assertionsTable.getColumnModel().getColumn( 0 ).setCellRenderer( assertionEntryRenderer );
		//		assertionListPanel.add( new JScrollPane( assertionsTable ) );
		assertionsForm.addComponent( new JScrollPane( assertionsTable ) );
		return assertionsForm.getPanel();
	}

	private Component buildCategoriesList()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		AssertionCategoriesListModel listModel = new AssertionCategoriesListModel( categoriesAssertionsMap.keySet() );
		categoriesList = new JXList( listModel );
		categoriesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
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
					InternalCellRenderer assertionEntryRenderer = new InternalCellRenderer();
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
			if( handleOk() )
			{
				setVisible( false );
			}
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

	public void release()
	{
		assertionsTable.getSelectionModel().removeListSelectionListener( selectionListener );
	}

	private class HideAssertionDescriptionsAction extends AbstractAction
	{

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			// TODO Auto-generated method stub

		}

	}

	private class InternalCellRenderer extends DefaultCellRenderer
	{

		private Font boldFont;
		private Font plainFont;

		@Override
		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column )
		{

			Component result = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

			boldFont = getFont().deriveFont( Font.BOLD );

			String str = ( ( AssertionListEntry )value ).getName();
			JLabel label = new JLabel( str );
			label.setFont( boldFont );

			JTextArea text = new JTextArea( ( ( AssertionListEntry )value ).getDescription() );
			//			text.setEditable( false );
			SimpleForm form = new SimpleForm();

			form.addComponent( label );
			form.addComponent( text );

			if( isSelected )
			{
				text.setBackground( Color.LIGHT_GRAY );
				form.getPanel().setBackground( Color.LIGHT_GRAY );
			}
			else
			{
				text.setBackground( Color.WHITE );
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

}
