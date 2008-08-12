/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.endpoint;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.EndpointConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy.EndpointDefaults;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.MetricsPanel;

public class DefaultEndpointStrategyConfigurationPanel extends JPanel
{
	private EndpointsTableModel tableModel;
	private JXTable table;
	private JButton deleteButton;
	private JButton assignButton;
	private WsdlInterface iface;
	private final DefaultEndpointStrategy strategy;

	public DefaultEndpointStrategyConfigurationPanel( WsdlInterface iface, DefaultEndpointStrategy strategy )
	{
		super( new BorderLayout() );
		
		this.iface = iface;
		this.strategy = strategy;
		
		buildUI();
		
		enableButtons();
	}

	private void buildUI()
	{
		tableModel = new EndpointsTableModel();
		table = new JXTable( tableModel );
		table.setHorizontalScrollEnabled( true );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				enableButtons();
			}
		} );
		
		for( int c = 0; c < table.getColumnCount(); c++ )
			table.getColumn( c ).setHeaderRenderer( new MetricsPanel.InternalHeaderRenderer( getBackground() ) );
		
		table.getColumn( 0 ).setPreferredWidth( 250 );
		JComboBox wssTypeCombo = new JComboBox( new String[] {WsdlRequest.PW_TYPE_NONE, WsdlRequest.PW_TYPE_TEXT, WsdlRequest.PW_TYPE_DIGEST});
		wssTypeCombo.setEditable( true );
		table.getColumn( 4 ).setCellEditor( new DefaultCellEditor( wssTypeCombo) );
		
		table.getColumn( 6 ).setCellEditor( new OutgoingWssCellEditor() );
		table.getColumn( 7 ).setCellEditor( new IncomingWssCellEditor() );
		table.getColumn( 8 ).setCellEditor( new DefaultCellEditor(
					new JComboBox( new String[] {
								EndpointConfig.Mode.OVERRIDE.toString(),
								EndpointConfig.Mode.COMPLEMENT.toString(),
								EndpointConfig.Mode.COPY.toString()
					})) );

		table.getTableHeader().setReorderingAllowed( false );
		
		setBackground( Color.WHITE );
		setOpaque( true );
		
		JScrollPane scrollPane = new JScrollPane( table );
		scrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 2, 5, 2 ));
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBackground( Color.WHITE );

		add( scrollPane, BorderLayout.CENTER );
		add( createButtons(), BorderLayout.NORTH );
	}

	protected void enableButtons()
	{
		deleteButton.setEnabled( table.getSelectedRow() != -1 );
		assignButton.setEnabled( table.getSelectedRow() != -1 );
	}

	private Component createButtons()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		
		toolbar.addFixed( UISupport.createToolbarButton( new AddAction() ) );
		deleteButton = UISupport.createToolbarButton( new DeleteAction() );
		toolbar.addFixed( deleteButton );
		toolbar.addRelatedGap();
		assignButton = new JButton( new AssignAction() );
		toolbar.addFixed( assignButton );

		toolbar.addGlue();
		ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction( HelpUrls.ENDPOINTSEDITOR_HELP_URL );
		toolbar.addFixed( UISupport.createToolbarButton( showOnlineHelpAction ) );
		
		return toolbar;
	}

	private class AddAction extends AbstractAction
	{
		public AddAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ));
			putValue( Action.SHORT_DESCRIPTION, "Adds a new endpoint to the list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String endpoint = UISupport.prompt( "Enter new endpoint URL", "Add Endpoint", "" );

			if( endpoint == null )
				return;

			tableModel.addEndpoint( endpoint );
		}
	}

	private class AssignAction extends AbstractAction
	{
		private static final String ALL_REQUESTS = "- All Requests -";
		private static final String ALL_TEST_REQUESTS = "- All Test Requests -";
		private static final String ALL_REQUESTS_AND_TEST_REQUESTS = "- All Requests and TestRequests -";
		private static final String ALL_REQUESTS_WITH_NO_ENDPOINT = "- All Requests with no endpoint -";

		public AssignAction()
		{
			super( "Assign" );
			putValue( Action.SHORT_DESCRIPTION,
						"Assigns the selected endpoint to Requests/TestRequests for this Interface" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int selectedIndex = table.getSelectedRow();
			if( selectedIndex == -1 )
			{
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			String selectedEndpoint = tableModel.getEndpointAt( selectedIndex );
			EndpointDefaults defaults = tableModel.getDefaultsAt( selectedIndex );

			List<String> list = new ArrayList<String>( Arrays.asList( iface.getEndpoints() ) );
			list.add( 0, ALL_REQUESTS );
			list.add( 1, ALL_TEST_REQUESTS );
			list.add( 2, ALL_REQUESTS_AND_TEST_REQUESTS );
			list.add( 3, ALL_REQUESTS_WITH_NO_ENDPOINT );

			Object endpoint = UISupport.prompt( "Assign selected endpoint and authorization to..", "Assign Endpoint", list.toArray(),
						ALL_REQUESTS_WITH_NO_ENDPOINT );

			if( endpoint == null )
				return;

			int changeCount = 0;

			if( endpoint.equals( ALL_REQUESTS ) || endpoint.equals( ALL_REQUESTS_WITH_NO_ENDPOINT )
						|| endpoint.equals( ALL_REQUESTS_AND_TEST_REQUESTS ) )
			{
				for( int c = 0; c < iface.getOperationCount(); c++ )
				{
					Operation operation = iface.getOperationAt( c );
					for( int i = 0; i < operation.getRequestCount(); i++ )
					{
						WsdlRequest request = ( WsdlRequest ) operation.getRequestAt( i );
						String ep = request.getEndpoint();

						if( endpoint.equals( ALL_REQUESTS ) || endpoint.equals( ALL_REQUESTS_AND_TEST_REQUESTS )
									|| ( endpoint.equals( ALL_REQUESTS_WITH_NO_ENDPOINT ) && ep == null )
									|| ( ep.equals( endpoint ) ) )
						{
							request.setEndpoint( selectedEndpoint );
							
							request.setUsername( defaults.getUsername() );
							request.setPassword( defaults.getPassword() );
							request.setDomain( defaults.getDomain() );
							request.setWssPasswordType( defaults.getWssType() );
							request.setWssTimeToLive( defaults.getWssTimeToLive() );
							request.setOutgoingWss( defaults.getOutgoingWss() );
							request.setIncomingWss( defaults.getIncomingWss() );
							
							changeCount++;
						}
					}
				}
			}

			if( endpoint.equals( ALL_REQUESTS_AND_TEST_REQUESTS ) || endpoint.equals( ALL_TEST_REQUESTS ) )
			{
				for( TestSuite testSuite : iface.getProject().getTestSuiteList() )
				{
					for( TestCase testCase : testSuite.getTestCaseList() )
					{
						for( TestStep testStep : testCase.getTestStepList() )
						{
							if( testStep instanceof WsdlTestRequestStep )
							{
								WsdlTestRequest testRequest = ( ( WsdlTestRequestStep ) testStep ).getTestRequest();
								if( testRequest.getOperation().getInterface() == iface )
								{
									testRequest.setEndpoint( selectedEndpoint );
									
									testRequest.setUsername( defaults.getUsername() );
									testRequest.setPassword( defaults.getPassword() );
									testRequest.setDomain( defaults.getDomain() );
									testRequest.setWssPasswordType( defaults.getWssType() );
									testRequest.setWssTimeToLive( defaults.getWssTimeToLive() );
									testRequest.setOutgoingWss( defaults.getOutgoingWss() );
									testRequest.setIncomingWss( defaults.getIncomingWss() );
									
									changeCount++;
								}
							}
						}
					}
				}
			}
		}
	}

	private class DeleteAction extends AbstractAction
	{
		public DeleteAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ));
			putValue( Action.SHORT_DESCRIPTION, "Deletes the selected endpoint from the list" );
		}

		public void actionPerformed( ActionEvent e )
		{
			int index = table.getSelectedRow();
			if( index == -1 )
			{
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			if( UISupport.confirm( "Delete selected endpoint?", "Delete Endpoint" ) )
			{
				tableModel.removeEndpoint( index );
			}
		}
	}

	private class EndpointsTableModel extends AbstractTableModel
	{
		public int getColumnCount()
		{
			return 9;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0:
				return "Endpoint";
			case 1:
				return "Username";
			case 2:
				return "Password";
			case 3:
				return "Domain";
			case 4:
				return "WSS-Type";
			case 5:
				return "WSS-TimeToLive";
			case 6 :
				return "Outgoing WSS";
			case 7 : 
				return "Incoming WSS";
			case 8:
				return "Mode";
			}

			return null;
		}

		public String getEndpointAt( int selectedIndex )
		{
			return iface.getEndpoints()[selectedIndex];
		}
		
		public EndpointDefaults getDefaultsAt( int selectedIndex )
		{
			String endpoint = getEndpointAt( selectedIndex );
			return strategy.getEndpointDefaults( endpoint );
		}

		public void addEndpoint( String endpoint )
		{
			int rowCount = getRowCount();
			iface.addEndpoint( endpoint );
			
			fireTableRowsInserted( rowCount, rowCount );
		}
		
		public void removeEndpoint( int index )
		{
			String ep = getEndpointAt( index );
			iface.removeEndpoint( ep );
			fireTableRowsDeleted( index, index );
		}
		
		public int getRowCount()
		{
			return iface == null ? 0 : iface.getEndpoints().length;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			String endpoint = getEndpointAt( rowIndex );
			EndpointDefaults defaults = strategy.getEndpointDefaults( endpoint );

			switch( columnIndex )
			{
			case 0:
				return endpoint;
			case 1:
				return defaults.getUsername();
			case 2:
				return defaults.getPassword();
			case 3:
				return defaults.getDomain();
			case 4:
				return defaults.getWssType();
			case 5:
				return defaults.getWssTimeToLive();
			case 6:
				return defaults.getOutgoingWss();
			case 7:
				return defaults.getIncomingWss();
			case 8:
				return defaults.getMode();
			}

			return null;
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return true;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			String endpoint = getEndpointAt( rowIndex );
			EndpointDefaults defaults = strategy.getEndpointDefaults( endpoint );
			
			if( aValue == null )
				aValue = "";
			
			switch( columnIndex )
			{
				case 0 :
				{
//					strategy.changeEndpoint( endpoint, aValue.toString() );
					iface.changeEndpoint( endpoint, aValue.toString() );
					break;
				}
				case 1 :
				{
					defaults.setUsername( aValue.toString() );
					break;
				}
				case 2 :
				{
					defaults.setPassword( aValue.toString() );
					break;
				}
				case 3 :
				{
					defaults.setDomain( aValue.toString() );
					break;
				}
				case 4 :
				{
					defaults.setWssType( aValue.toString() );
					break;
				}
				case 5 :
				{
					defaults.setWssTimeToLive( aValue.toString() );
					break;
				}
				case 6 :
				{
					defaults.setOutgoingWss( aValue.toString() );
					break;
				}
				case 7 :
				{
					defaults.setIncomingWss( aValue.toString() );
					break;
				}
				case 8 :
				{
					defaults.setMode( EndpointConfig.Mode.Enum.forString( aValue.toString() ) );
					break;
				}
			}
		}
	}
	
	private class IncomingWssCellEditor extends DefaultCellEditor
	{
		public IncomingWssCellEditor()
		{
			super( new JComboBox() );
		}

		@Override
		public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
		{
			JComboBox comboBox = ( JComboBox ) super.getTableCellEditorComponent( table, value, isSelected, row, column );
			
			DefaultComboBoxModel model = new DefaultComboBoxModel( iface.getProject().getWssContainer().getIncomingWssNames() );
			model.addElement( "" );
			
			comboBox.setModel( model );
			
			return comboBox;
		}
	}
	
	private class OutgoingWssCellEditor extends DefaultCellEditor
	{
		public OutgoingWssCellEditor()
		{
			super( new JComboBox() );
		}

		@Override
		public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
		{
			JComboBox comboBox = ( JComboBox ) super.getTableCellEditorComponent( table, value, isSelected, row, column );
			
			DefaultComboBoxModel model = new DefaultComboBoxModel( iface.getProject().getWssContainer().getOutgoingWssNames() );
			model.addElement( "" );
			
			comboBox.setModel( model );
			
			return comboBox;
		}
	}
}
