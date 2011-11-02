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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

public class AddAssertionPanel extends SimpleDialog
{
	private final static String VALIDATE_RESPONSE_CONTENT_CATEGORY = "Validate Response Content";
	private final static String STATUS_CATEGORY = "Compliance, Status and Standards";
	private final static String SCRIPT_CATEGORY = "Script";
	private final static String SLA_CATEGORY = "SLA";
	private final static String JMS_CATEGORY = "JMS";
	private final static String SECURITY_CATEGORY = "Security";

	private final static String XPATH_MATCH = "XPath Match";
	private final static String XQUERY_MATCH = "XQuery Match";
	private final static String NOT_CONTAINS = "Not Contains";
	private final static String CONTAINS = "Contains";

	private final static String INVALID_CODES = "Invalid HTTP Status Codes";
	private final static String WS_SECURITY_STATUS = "WS Security Status";
	private final static String NOT_SOAP_FAULT = "Not Soap Fault";
	private final static String VALID_CODES = "Valid HTTP Status Codes";
	private final static String SOAP_RESPONSE = "SOAP Response";
	private final static String WS_ADDRESSING_RESPONSE = "WS Addressing Response";
	private final static String SCHEMA_COMPLIANCE = "Schema Compliance";
	private final static String SOAP_FAULT = "SOAP Fault";

	private JXList categoriesList;
	private JXTable assertionsTable;
	private Assertable assertable;
	private AddAssertionAction addAssertionAction;
	private AssertionsListTableModel assertionsListTableModel;
	private JPanel assertionListPanel;
	private LinkedHashSet<String> assertions;
	private InternalListSelectionListener selectionListener = new InternalListSelectionListener();

	public AddAssertionPanel( Assertable assertable )
	{
		super( "Select Assertion", "Select which assertion to add", "" );
		this.assertable = assertable;
	}

	public static String[] getAssertionCategories()
	{
		return new String[] { VALIDATE_RESPONSE_CONTENT_CATEGORY, STATUS_CATEGORY, SCRIPT_CATEGORY, SLA_CATEGORY,
				JMS_CATEGORY, SECURITY_CATEGORY };
	}

	public static LinkedHashMap<String, LinkedHashSet<String>> getCategoriesAssertionsMap()
	{
		LinkedHashMap<String, LinkedHashSet<String>> categoriesAssertionsMap = new LinkedHashMap<String, LinkedHashSet<String>>();
		LinkedHashSet<String> validatingResponseAssertionsSet = new LinkedHashSet<String>();
		validatingResponseAssertionsSet.add( XPATH_MATCH );
		validatingResponseAssertionsSet.add( XQUERY_MATCH );
		validatingResponseAssertionsSet.add( NOT_CONTAINS );
		validatingResponseAssertionsSet.add( CONTAINS );
		categoriesAssertionsMap.put( VALIDATE_RESPONSE_CONTENT_CATEGORY, validatingResponseAssertionsSet );

		LinkedHashSet<String> statusAssertionsSet = new LinkedHashSet<String>();
		statusAssertionsSet.clear();
		statusAssertionsSet.add( INVALID_CODES );
		statusAssertionsSet.add( WS_SECURITY_STATUS );
		statusAssertionsSet.add( NOT_SOAP_FAULT );
		statusAssertionsSet.add( VALID_CODES );
		statusAssertionsSet.add( SOAP_RESPONSE );
		statusAssertionsSet.add( WS_ADDRESSING_RESPONSE );
		statusAssertionsSet.add( SCHEMA_COMPLIANCE );
		statusAssertionsSet.add( SOAP_FAULT );
		categoriesAssertionsMap.put( STATUS_CATEGORY, statusAssertionsSet );

		return categoriesAssertionsMap;
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
		return mainPanel;
	}

	private Component buildAssertionsList()
	{
		assertionListPanel = new JPanel( new BorderLayout() );
		assertionsListTableModel = new AssertionsListTableModel();
		assertionsTable = new JXTable( assertionsListTableModel );
		assertionsTable.setTableHeader( null );
		assertionsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		assertionsTable.getSelectionModel().addListSelectionListener( selectionListener );
		assertionListPanel.add( new JScrollPane( assertionsTable ) );
		return assertionListPanel;
	}

	private Component buildCategoriesList()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		AssertionCategoriesListModel listModel = new AssertionCategoriesListModel( getCategoriesAssertionsMap().keySet() );
		categoriesList = new JXList( listModel );
		categoriesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		categoriesList.addListSelectionListener( new ListSelectionListener()
		{
			@Override
			public void valueChanged( ListSelectionEvent arg0 )
			{
				String category = ( String )categoriesList.getSelectedValue();
				if( category != null && getCategoriesAssertionsMap().containsKey( category ) )
				{
					assertions = getCategoriesAssertionsMap().get( category );
					assertionsListTableModel.setListEntriesSet( assertions );
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
		String selection = ( String )assertionsListTableModel.getValueAt( selectedRow, 0 );
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
			if( assertionsTable.getSelectedRow() > 0 )
				addAssertionAction.setEnabled( true );
			else
				addAssertionAction.setEnabled( false );

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

}
