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

package com.eviware.soapui.impl.wsdl.panels.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainerListener;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntryRegistry;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.KeyMaterialWssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

public class WSSTabPanel extends JPanel
{
	private JTable cryptosTable;
	private RemoveCryptoAction removeCryptoAction;
	private RemoveIncomingWssAction removeIncomingWssAction;
	private JTable incomingWssTable;
	private JComboBox incomingWssDecryptionCryptoComboBox;
	private JComboBox incomingWssSignatureCryptoComboBox;
	private JTable outgoingWssTable;
	private RemoveOutgoingWssAction removeOutgoingWssAction;
	private JButton removeOutgoingEntryButton;
	private WssEntry selectedEntry;
	private OutgoingWss selectedOutgoing;
	private JButton addOutgoingEntryButton;
	private final WssContainer wssContainer;
	private InternalWssContainerListener wssContainerListener;
	private JTabbedPane entriesTabs;

	public WSSTabPanel( WssContainer wssContainer )
	{
		super( new BorderLayout() );
		this.wssContainer = wssContainer;

		wssContainerListener = new InternalWssContainerListener();
		wssContainer.addWssContainerListener( wssContainerListener );

		buildUI();
	}

	private void buildUI()
	{
		add( buildMainToolbar(), BorderLayout.NORTH );
		add( buildContent(), BorderLayout.CENTER );
	}

	private JComponent buildContent()
	{
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab( "Outgoing WS-Security Configurations", buildOutgoingConfigurationsTab() );
		tabs.addTab( "Incoming WS-Security Configurations", buildIncomingConfigurationsTab() );
		tabs.addTab( "Keystores", buildCryptosTable() );

		tabs.setMinimumSize( new Dimension( 10, 10 ) );

		return UISupport.createTabPanel( tabs, true );
	}

	private JPanel buildIncomingConfigurationsTab()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( buildIncomingWssToolbar(), BorderLayout.NORTH );

		incomingWssTable = new JTable( new IncomingWssTableModel() );
		incomingWssTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				removeIncomingWssAction.setEnabled( incomingWssTable.getSelectedRow() != -1 );
			}
		} );

		incomingWssDecryptionCryptoComboBox = new JComboBox( new KeystoresComboBoxModel( wssContainer, null ) );
		incomingWssTable.getColumnModel().getColumn( 1 )
				.setCellEditor( new DefaultCellEditor( incomingWssDecryptionCryptoComboBox ) );

		incomingWssSignatureCryptoComboBox = new JComboBox( new KeystoresComboBoxModel( wssContainer, null ) );
		incomingWssTable.getColumnModel().getColumn( 2 )
				.setCellEditor( new DefaultCellEditor( incomingWssSignatureCryptoComboBox ) );

		incomingWssTable.getColumnModel().getColumn( 3 ).setCellEditor( new DefaultCellEditor( new JPasswordField() ) );
		incomingWssTable.getColumnModel().getColumn( 3 ).setCellRenderer( new PasswordTableCellRenderer() );

		p.add( new JScrollPane( incomingWssTable ), BorderLayout.CENTER );
		panel.add( p, BorderLayout.CENTER );
		return panel;
	}

	private JPanel buildOutgoingConfigurationsTab()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( buildOutgoingWssToolbar(), BorderLayout.NORTH );

		outgoingWssTable = new JTable( new OutgoingWssTableModel() );
		outgoingWssTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				int selectedRow = outgoingWssTable.getSelectedRow();
				selectedOutgoing = selectedRow == -1 ? null : wssContainer.getOutgoingWssAt( selectedRow );
				removeOutgoingWssAction.setEnabled( selectedRow != -1 );
				addOutgoingEntryButton.setEnabled( selectedRow != -1 );

				entriesTabs.removeAll();
				if( selectedOutgoing != null )
				{
					for( WssEntry entry : selectedOutgoing.getEntries() )
					{
						entriesTabs.addTab( entry.getLabel(), entry.getConfigurationPanel() );
					}
				}

				entriesTabs.getParent().setVisible( entriesTabs.getTabCount() > 0 );
			}
		} );

		outgoingWssTable.getColumnModel().getColumn( 2 ).setCellEditor( new DefaultCellEditor( new JPasswordField() ) );
		outgoingWssTable.getColumnModel().getColumn( 2 ).setCellRenderer( new PasswordTableCellRenderer() );

		JSplitPane split = UISupport.createVerticalSplit( new JScrollPane( outgoingWssTable ), buildOutgoingWssDetails() );
		split.setDividerLocation( 140 );
		p.add( split, BorderLayout.CENTER );
		panel.add( p, BorderLayout.CENTER );
		return panel;
	}

	private Component buildOutgoingWssDetails()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( buildOutgoingEntriesToolbar(), BorderLayout.NORTH );
		panel.add( buildOutgoingEntryList(), BorderLayout.CENTER );

		entriesTabs.getParent().setVisible( false );

		return panel;
	}

	private Component buildOutgoingEntryList()
	{
		entriesTabs = new JTabbedPane();
		entriesTabs.addChangeListener( new ChangeListener()
		{

			public void stateChanged( ChangeEvent e )
			{
				selectedEntry = entriesTabs.getSelectedIndex() == -1 ? null : selectedOutgoing.getEntries().get(
						entriesTabs.getSelectedIndex() );
				removeOutgoingEntryButton.setEnabled( selectedEntry != null );
			}
		} );

		return UISupport.createTabPanel( entriesTabs, true );
	}

	private Component buildOutgoingEntriesToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( addOutgoingEntryButton = UISupport.createToolbarButton( new AddOutgoingEntryAction() ) );
		toolbar.addFixed( removeOutgoingEntryButton = UISupport.createToolbarButton( new RemoveOutgoingEntryAction(),
				false ) );

		return toolbar;
	}

	private Component buildOutgoingWssToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new AddOutgoingWssAction() ) );
		removeOutgoingWssAction = new RemoveOutgoingWssAction();
		toolbar.addFixed( UISupport.createToolbarButton( removeOutgoingWssAction ) );
		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.OUTGOINGWSS_HELP_URL ) ) );

		return toolbar;
	}

	private JPanel buildCryptosTable()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JPanel p = new JPanel( new BorderLayout() );

		p.add( buildCryptosToolbar(), BorderLayout.NORTH );

		cryptosTable = new JTable( new CryptosTableModel() );
		cryptosTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				removeCryptoAction.setEnabled( cryptosTable.getSelectedRow() != -1 );
			}
		} );

		cryptosTable.getColumnModel().getColumn( 3 ).setCellEditor( new DefaultCellEditor( new JPasswordField() ) );
		cryptosTable.getColumnModel().getColumn( 3 ).setCellRenderer( new PasswordTableCellRenderer() );
		cryptosTable.getColumnModel().getColumn( 5 ).setCellEditor( new DefaultCellEditor( new JPasswordField() ) );
		cryptosTable.getColumnModel().getColumn( 5 ).setCellRenderer( new PasswordTableCellRenderer() );

		p.add( new JScrollPane( cryptosTable ), BorderLayout.CENTER );

		panel.add( p, BorderLayout.CENTER );
		return panel;
	}

	private Component buildCryptosToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new AddKeystoreAction() ) );
		toolbar.addFixed( UISupport.createToolbarButton( new AddTruststoreAction() ) );
		removeCryptoAction = new RemoveCryptoAction();
		toolbar.addFixed( UISupport.createToolbarButton( removeCryptoAction ) );
		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.CRYPTOSWSS_HELP_URL ) ) );
		return toolbar;
	}

	private Component buildIncomingWssToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new AddIncomingWssAction() ) );
		removeIncomingWssAction = new RemoveIncomingWssAction();
		toolbar.addFixed( UISupport.createToolbarButton( removeIncomingWssAction ) );

		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.INCOMINGWSS_HELP_URL ) ) );

		return toolbar;
	}

	private Component buildMainToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();
		toolbar.addGlue();
		toolbar.addFixed( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.WSS_HELP_URL ) ) );
		return toolbar;
	}

	public void release()
	{
		wssContainer.removeWssContainerListener( wssContainerListener );

		( ( IncomingWssTableModel )incomingWssTable.getModel() ).release();
		( ( OutgoingWssTableModel )outgoingWssTable.getModel() ).release();
		( ( CryptosTableModel )cryptosTable.getModel() ).release();

		( ( KeystoresComboBoxModel )incomingWssDecryptionCryptoComboBox.getModel() ).release();
		( ( KeystoresComboBoxModel )incomingWssSignatureCryptoComboBox.getModel() ).release();

		entriesTabs.removeAll();
	}

	public class CryptosTableModel extends AbstractTableModel
	{
		private static final String DEFAULT_OPTION = "<Default>";
		private List<WssCrypto> cryptos;

		public CryptosTableModel()
		{
			cryptos = wssContainer.getCryptoList();
		}

		public void release()
		{
			cryptos = null;
		}

		public int getColumnCount()
		{
			// FIXME: Why not remove??
			// hide last column since this is autodetected in commons-ssl-0.3.10
			return 6;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Type";
			case 1 :
				return "Source";
			case 2 :
				return "Status";
			case 3 :
				return "Password";
			case 4 :
				return "Default Alias";
			case 5 :
				return "Alias Password";
			case 6 :
				return "Security Provider";
			}

			return null;
		}

		public int getRowCount()
		{
			return cryptos == null ? 0 : cryptos.size();
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return columnIndex > 1;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			KeyMaterialWssCrypto crypto = ( KeyMaterialWssCrypto )cryptos.get( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return crypto.getType();
			case 1 :
				return crypto.getSource();
			case 2 :
				return crypto.getStatus();
			case 3 :
				return crypto.getPassword();
			case 4 :
				return crypto.getDefaultAlias();
			case 5 :
				return crypto.getAliasPassword();
			case 6 :
				return StringUtils.hasContent( crypto.getCryptoProvider() ) ? crypto.getCryptoProvider() : DEFAULT_OPTION;
			}

			return null;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			KeyMaterialWssCrypto crypto = ( KeyMaterialWssCrypto )cryptos.get( rowIndex );
			if( aValue == null || aValue.equals( DEFAULT_OPTION ) )
				aValue = "";

			switch( columnIndex )
			{
			case 3 :
				crypto.setPassword( aValue.toString() );
				break;
			case 4 :
				crypto.setDefaultAlias( aValue.toString() );
				break;
			case 5 :
				crypto.setAliasPassword( aValue.toString() );
				break;
			case 6 :
				crypto.setCryptoProvider( aValue.toString() );
				break;
			}
		}

		public void cryptoAdded( WssCrypto crypto )
		{
			cryptos.add( crypto );
			fireTableRowsInserted( cryptos.size() - 1, cryptos.size() - 1 );
		}

		public void cryptoRemoved( WssCrypto crypto )
		{
			int ix = cryptos.indexOf( crypto );
			if( ix != -1 )
			{
				cryptos.remove( ix );
				fireTableRowsDeleted( ix, ix );
			}
		}
	}

	private class AddKeystoreAction extends AbstractAction
	{
		public AddKeystoreAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/keystore.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new keystore to this configuration" );
		}

		public void actionPerformed( ActionEvent e )
		{
			File file = UISupport.getFileDialogs().open( this, "Select Key Material", null, null, null );
			if( file != null )
			{
				String password = new String( UISupport.promptPassword( "Specify password for [" + file.getName() + "]",
						"Add Key Material" ) );
				wssContainer.addCrypto( file.getAbsolutePath(), password, CryptoType.KEYSTORE );
				cryptosTable.setRowSelectionInterval( cryptosTable.getRowCount() - 1, cryptosTable.getRowCount() - 1 );
			}
		}
	}

	private class AddTruststoreAction extends AbstractAction
	{
		public AddTruststoreAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/truststore.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new truststore to this configuration" );
		}

		public void actionPerformed( ActionEvent e )
		{
			File file = UISupport.getFileDialogs().open( this, "Select Key Material", null, null, null );
			if( file != null )
			{
				String password = new String( UISupport.promptPassword( "Specify password for [" + file.getName() + "]",
						"Add Key Material" ) );
				wssContainer.addCrypto( file.getAbsolutePath(), password, CryptoType.TRUSTSTORE );
				cryptosTable.setRowSelectionInterval( cryptosTable.getRowCount() - 1, cryptosTable.getRowCount() - 1 );
			}
		}
	}

	private class RemoveCryptoAction extends AbstractAction
	{
		public RemoveCryptoAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Removes the selected crypto from this configuration" );

			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = cryptosTable.getSelectedRow();
			if( row == -1 )
				return;

			if( UISupport.confirm( "Removes selected crypto?", "Remove Crypto" ) )
			{
				wssContainer.removeCryptoAt( row );
			}
		}
	}

	public class IncomingWssTableModel extends AbstractTableModel
	{
		private List<IncomingWss> incomingWss;

		public IncomingWssTableModel()
		{
			incomingWss = wssContainer.getIncomingWssList();
		}

		public void release()
		{
			incomingWss = null;
		}

		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Name";
			case 1 :
				return "Decrypt Keystore";
			case 2 :
				return "Signature Keystore";
			case 3 :
				return "Password";
			}

			return null;
		}

		public int getRowCount()
		{
			return incomingWss == null ? 0 : incomingWss.size();
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return columnIndex > 0;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			IncomingWss incoming = incomingWss.get( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return incoming.getName();
			case 1 :
				return wssContainer.getCryptoByName( incoming.getDecryptCrypto() );
			case 2 :
				return wssContainer.getCryptoByName( incoming.getSignatureCrypto() );
			case 3 :
				return incoming.getDecryptPassword();
			}

			return null;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			IncomingWss incoming = incomingWss.get( rowIndex );

			switch( columnIndex )
			{
			case 1 :
				incoming.setDecryptCrypto( aValue == null ? null : aValue.toString() );
				break;
			case 2 :
				incoming.setSignatureCrypto( aValue == null ? null : aValue.toString() );
				break;
			case 3 :
				incoming.setDecryptPassword( aValue == null ? null : aValue.toString() );
				break;
			}
		}

		public void incomingWssAdded( IncomingWss incoming )
		{
			incomingWss.add( incoming );
			fireTableRowsInserted( incomingWss.size() - 1, incomingWss.size() - 1 );

		}

		public void incomingWssRemoved( IncomingWss incoming )
		{
			int ix = incomingWss.indexOf( incoming );
			if( ix != -1 )
			{
				incomingWss.remove( ix );
				fireTableRowsDeleted( ix, ix );
			}
		}
	}

	private class AddIncomingWssAction extends AbstractAction
	{
		public AddIncomingWssAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new Incoming WSS Configuration" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify unique name for configuration", "New Incoming WSS Configuration", "" );
			if( StringUtils.hasContent( name ) && wssContainer.getIncomingWssByName( name ) == null )
			{
				wssContainer.addIncomingWss( name );
				incomingWssTable.setRowSelectionInterval( incomingWssTable.getRowCount() - 1,
						incomingWssTable.getRowCount() - 1 );
			}
		}
	}

	private class RemoveIncomingWssAction extends AbstractAction
	{
		public RemoveIncomingWssAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Removes the selected Incoming WSS Configuration" );

			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = incomingWssTable.getSelectedRow();
			if( row == -1 )
				return;

			if( UISupport.confirm( "Removes selected configuration?", "Remove Configuration" ) )
			{
				wssContainer.removeIncomingWssAt( row );
			}
		}
	}

	public class OutgoingWssTableModel extends AbstractTableModel
	{
		private List<OutgoingWss> outgoingWss;

		public OutgoingWssTableModel()
		{
			outgoingWss = wssContainer.getOutgoingWssList();
		}

		public void release()
		{
			outgoingWss = null;
		}

		public int getColumnCount()
		{
			return 5;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Name";
			case 1 :
				return "Default Username/Alias";
			case 2 :
				return "Default Password";
			case 3 :
				return "Actor";
			case 4 :
				return "Must Understand";
			}

			return null;
		}

		@Override
		public Class<?> getColumnClass( int columnIndex )
		{
			return columnIndex == 4 ? Boolean.class : String.class;
		}

		public int getRowCount()
		{
			return outgoingWss == null ? 0 : outgoingWss.size();
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return columnIndex > 0;
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			OutgoingWss outgoing = outgoingWss.get( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return outgoing.getName();
			case 1 :
				return outgoing.getUsername();
			case 2 :
				return outgoing.getPassword();
			case 3 :
				return outgoing.getActor();
			case 4 :
				return outgoing.getMustUnderstand();
			}

			return null;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			OutgoingWss outgoing = outgoingWss.get( rowIndex );

			switch( columnIndex )
			{
			case 1 :
				outgoing.setUsername( aValue == null ? null : aValue.toString() );
				break;
			case 2 :
				outgoing.setPassword( aValue == null ? null : aValue.toString() );
				break;
			case 3 :
				outgoing.setActor( aValue == null ? null : aValue.toString() );
				break;
			case 4 :
				outgoing.setMustUnderstand( aValue == null ? false : ( Boolean )aValue );
				break;
			}
		}

		public void outgoingWssAdded( OutgoingWss outgoing )
		{
			outgoingWss.add( outgoing );
			fireTableRowsInserted( outgoingWss.size() - 1, outgoingWss.size() - 1 );
		}

		public void outgoingWssRemoved( OutgoingWss outgoing )
		{
			int ix = outgoingWss.indexOf( outgoing );
			if( ix != -1 )
			{
				outgoingWss.remove( ix );
				fireTableRowsDeleted( ix, ix );
			}
		}
	}

	private class AddOutgoingWssAction extends AbstractAction
	{
		public AddOutgoingWssAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new Outgoing WSS Configuration" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String name = UISupport.prompt( "Specify unique name for configuration", "New Outgoing WSS Configuration", "" );
			if( StringUtils.hasContent( name ) && wssContainer.getOutgoingWssByName( name ) == null )
			{
				wssContainer.addOutgoingWss( name );
				outgoingWssTable.setRowSelectionInterval( outgoingWssTable.getRowCount() - 1,
						outgoingWssTable.getRowCount() - 1 );
			}
		}
	}

	private class RemoveOutgoingWssAction extends AbstractAction
	{
		public RemoveOutgoingWssAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Removes the selected Outgoing WSS Configuration" );

			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = outgoingWssTable.getSelectedRow();
			if( row == -1 )
				return;

			if( UISupport.confirm( "Removes selected configuration?", "Remove Configuration" ) )
			{
				wssContainer.removeOutgoingWssAt( row );
			}
		}
	}

	public class AddOutgoingEntryAction extends AbstractAction
	{
		public AddOutgoingEntryAction()
		{
			putValue( SHORT_DESCRIPTION, "Adds a new WSS Entry" );
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( selectedOutgoing == null )
				return;

			String type = UISupport.prompt( "Select type of entry to add", "Add WSS Entry", WssEntryRegistry.get()
					.getTypes() );
			if( type != null )
			{
				WssEntry entry = selectedOutgoing.addEntry( type );
				entriesTabs.setSelectedComponent( entry.getConfigurationPanel() );
			}
		}
	}

	public class RemoveOutgoingEntryAction extends AbstractAction
	{
		public RemoveOutgoingEntryAction()
		{
			putValue( SHORT_DESCRIPTION, "Removes the selected WSS-Entry" );
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( selectedEntry == null )
				return;

			if( UISupport.confirm( "Remove entry [" + selectedEntry.getLabel() + "]", "Remove WSS Entry" ) )
			{
				selectedOutgoing.removeEntry( selectedEntry );
			}
		}
	}

	private class InternalWssContainerListener implements WssContainerListener
	{
		public void cryptoAdded( WssCrypto crypto )
		{
			( ( CryptosTableModel )cryptosTable.getModel() ).cryptoAdded( crypto );
		}

		public void cryptoRemoved( WssCrypto crypto )
		{
			( ( CryptosTableModel )cryptosTable.getModel() ).cryptoRemoved( crypto );
		}

		public void incomingWssAdded( IncomingWss incomingWss )
		{
			( ( IncomingWssTableModel )incomingWssTable.getModel() ).incomingWssAdded( incomingWss );

		}

		public void incomingWssRemoved( IncomingWss incomingWss )
		{
			( ( IncomingWssTableModel )incomingWssTable.getModel() ).incomingWssRemoved( incomingWss );

		}

		public void outgoingWssAdded( OutgoingWss outgoingWss )
		{
			( ( OutgoingWssTableModel )outgoingWssTable.getModel() ).outgoingWssAdded( outgoingWss );
		}

		public void outgoingWssEntryAdded( WssEntry entry )
		{
			if( entry.getOutgoingWss() == selectedOutgoing )
			{
				entriesTabs.addTab( entry.getLabel(), entry.getConfigurationPanel() );
				entriesTabs.getParent().setVisible( true );
			}
		}

		public void outgoingWssEntryRemoved( WssEntry entry )
		{
			if( entry.getOutgoingWss() == selectedOutgoing )
			{
				int ix = entriesTabs.indexOfComponent( entry.getConfigurationPanel() );
				if( ix != -1 )
					entriesTabs.remove( ix );

				entriesTabs.getParent().setVisible( entriesTabs.getTabCount() > 0 );
			}
		}

		public void outgoingWssRemoved( OutgoingWss outgoingWss )
		{
			( ( OutgoingWssTableModel )outgoingWssTable.getModel() ).outgoingWssRemoved( outgoingWss );
		}

		public void cryptoUpdated( WssCrypto crypto )
		{
		}
	}

	public class ImportWssSettingsAction extends AbstractAction
	{
		public ImportWssSettingsAction()
		{
			putValue( SHORT_DESCRIPTION, "Imports an existing WS-Security configuration from another project" );
			putValue( SMALL_ICON, UISupport.createImageIcon( "/load_properties.gif" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			String[] names = ModelSupport.getNames( ( ( WorkspaceImpl )SoapUI.getWorkspace() ).getOpenProjectList() );
			String projectName = UISupport.prompt( "Select project to import from", "Import WSS Settings", names );
			if( projectName != null )
			{
				WsdlProject prj = ( WsdlProject )SoapUI.getWorkspace().getProjectByName( projectName );
				wssContainer.importConfig( prj.getWssContainer() );
			}
		}
	}

	public static class PasswordTableCellRenderer extends JPasswordField implements TableCellRenderer
	{
		public PasswordTableCellRenderer()
		{
			setEditable( false );
			setBorder( null );
		}

		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column )
		{
			setBackground( table.getBackground() );
			setText( value == null ? "" : value.toString() );
			return this;
		}

	}
}
