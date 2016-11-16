/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.panels.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntry;
import com.eviware.soapui.impl.wsdl.support.wss.WssEntryRegistry;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.CryptoType;
import com.eviware.soapui.impl.wsdl.support.wss.crypto.KeyMaterialWssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.entries.WssContainerListenerAdapter;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// FIXME Consider splitting this up into smaller entities for each main inner component and put all actions into separate files
public class WSSTabPanel extends JPanel {
    private static final String ILLEGAL_CRYPTO_TYPE_MESSAGE = "Illegal crypto type";
    private static final int ENTRIES_LIST_COMPONENT_WIDTH = 150;
    private static final int MOVE_UP = -1;
    private static final int MOVE_DOWN = 1;

    @Nonnull
    private JTable keystoreTable;
    @Nonnull
    private JTable truststoreTable;
    @Nonnull
    private RemoveCryptoAction removeKeystoreAction;
    @Nonnull
    private RemoveCryptoAction removeTruststoreAction;

    private RemoveIncomingWssAction removeIncomingWssAction;
    private JTable incomingWssTable;
    private JComboBox incomingWssDecryptionCryptoComboBox;
    private JComboBox incomingWssSignatureCryptoComboBox;
    private JTable outgoingWssTable;
    private RemoveOutgoingWssAction removeOutgoingWssAction;
    private WssEntry selectedEntry;
    private OutgoingWss selectedOutgoing;
    private final WssContainer wssContainer;
    private InternalWssContainerListener wssContainerListener;

    private JButton addOutgoingEntryButton;
    private JButton removeOutgoingEntryButton;
    private JButton moveOutgoingEntryUpButton;
    private JButton moveOutgoingEntryDownButton;

    private JSplitPane entriesSplitPane;
    private JScrollPane entriesListContainer;
    private JList entriesList;
    private DefaultListModel entriesListModel;

    public WSSTabPanel(WssContainer wssContainer) {
        super(new BorderLayout());
        this.wssContainer = wssContainer;

        wssContainerListener = new InternalWssContainerListener();
        wssContainer.addWssContainerListener(wssContainerListener);

        buildUI();
    }

    public void release() {
        wssContainer.removeWssContainerListener(wssContainerListener);

        ((IncomingWssTableModel) incomingWssTable.getModel()).release();
        ((OutgoingWssTableModel) outgoingWssTable.getModel()).release();
        ((CryptoTableModel) keystoreTable.getModel()).release();
        ((CryptoTableModel) truststoreTable.getModel()).release();

        ((KeystoresComboBoxModel) incomingWssDecryptionCryptoComboBox.getModel()).release();
        ((KeystoresComboBoxModel) incomingWssSignatureCryptoComboBox.getModel()).release();

        entriesListModel.removeAllElements();
    }

    private void buildUI() {
        add(buildMainToolbar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Component buildMainToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.addGlue();
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.WSS_HELP_URL)));
        return toolbar;
    }

    private JComponent buildContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Outgoing WS-Security Configurations", buildOutgoingConfigurationsTab());
        tabs.addTab("Incoming WS-Security Configurations", buildIncomingConfigurationsTab());
        tabs.addTab("Keystores", buildCryptoTable(CryptoType.KEYSTORE));
        tabs.addTab("Truststores", buildCryptoTable(CryptoType.TRUSTSTORE));

        tabs.setMinimumSize(new Dimension(10, 10));

        return UISupport.createTabPanel(tabs, true);
    }

    private JPanel buildOutgoingConfigurationsTab() {
        outgoingWssTable = JTableFactory.getInstance().makeJTable(new OutgoingWssTableModel());
        outgoingWssTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = outgoingWssTable.getSelectedRow();
                selectedOutgoing = selectedRow == -1 ? null : wssContainer.getOutgoingWssAt(selectedRow);

                removeOutgoingWssAction.setEnabled(selectedRow != -1);
                addOutgoingEntryButton.setEnabled(selectedRow != -1);

                if (selectedOutgoing != null) {
                    entriesListModel.removeAllElements();
                    for (WssEntry entry : selectedOutgoing.getEntries()) {
                        entriesListModel.addElement(entry);
                    }

                    entriesListContainer.getViewport().add(entriesList);
                    entriesSplitPane.setRightComponent(new JPanel());
                }
                entriesSplitPane.setDividerLocation(ENTRIES_LIST_COMPONENT_WIDTH);
            }
        });

        outgoingWssTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        outgoingWssTable.getColumnModel().getColumn(2).setCellRenderer(new PasswordTableCellRenderer());

        JPanel outgoingConfigurationSplitPane = new JPanel(new BorderLayout());
        JSplitPane split = UISupport.createVerticalSplit(new JScrollPane(outgoingWssTable), buildOutgoingWssDetails());
        split.setDividerLocation(140);
        outgoingConfigurationSplitPane.add(buildOutgoingWssToolbar(), BorderLayout.NORTH);
        outgoingConfigurationSplitPane.add(split, BorderLayout.CENTER);

        JPanel outgoingConfigurationsPanel = new JPanel(new BorderLayout());
        outgoingConfigurationsPanel.add(outgoingConfigurationSplitPane, BorderLayout.CENTER);

        return outgoingConfigurationsPanel;
    }

    private Component buildOutgoingWssToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(new AddOutgoingWssAction()));
        removeOutgoingWssAction = new RemoveOutgoingWssAction();
        toolbar.addFixed(UISupport.createToolbarButton(removeOutgoingWssAction));
        toolbar.addGlue();
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.OUTGOINGWSS_HELP_URL)));

        return toolbar;
    }

    private Component buildOutgoingWssDetails() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildOutgoingEntriesToolbar(), BorderLayout.NORTH);
        panel.add(buildOutgoingEntryList(), BorderLayout.CENTER);

        return panel;
    }

    private Component buildOutgoingEntriesToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        addOutgoingEntryButton = UISupport.createToolbarButton(new AddOutgoingEntryAction(), false);
        toolbar.addFixed(addOutgoingEntryButton);

        removeOutgoingEntryButton = UISupport.createToolbarButton(new RemoveOutgoingEntryAction(), false);
        toolbar.addFixed(removeOutgoingEntryButton);

        moveOutgoingEntryUpButton = UISupport.createToolbarButton(new MoveOutgoingEntryUpAction(), false);
        toolbar.addFixed(moveOutgoingEntryUpButton);

        moveOutgoingEntryDownButton = UISupport.createToolbarButton(new MoveOutgoingEntryDownAction(), false);
        toolbar.addFixed(moveOutgoingEntryDownButton);

        return toolbar;
    }

    private Component buildOutgoingEntryList() {
        entriesSplitPane = UISupport.createHorizontalSplit();

        entriesListContainer = new JScrollPane();
        entriesListContainer.setMinimumSize(new Dimension(ENTRIES_LIST_COMPONENT_WIDTH, ENTRIES_LIST_COMPONENT_WIDTH));
        entriesSplitPane.setLeftComponent(entriesListContainer);
        entriesSplitPane.setDividerLocation(ENTRIES_LIST_COMPONENT_WIDTH);

        setEntriesSplitPaneToEmpty();

        entriesList = new JList();

        entriesListModel = new DefaultListModel();

        entriesList.setModel(entriesListModel);
        entriesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedIndex = entriesList.getSelectedIndex();

                selectedEntry = (entriesList.getSelectedIndex() == -1 ? null : (WssEntry) entriesListModel
                        .get(selectedIndex));

                removeOutgoingEntryButton.setEnabled(selectedEntry != null);
                moveOutgoingEntryUpButton.setEnabled(selectedIndex > 0);
                moveOutgoingEntryDownButton.setEnabled(selectedIndex > -1
                        && selectedIndex < (entriesListModel.getSize() - 1));

                if (selectedEntry != null) {
                    entriesSplitPane.setRightComponent(selectedEntry.getConfigurationPanel());
                }
                entriesSplitPane.setDividerLocation(ENTRIES_LIST_COMPONENT_WIDTH);
            }
        });

        return entriesSplitPane;
    }

    private JPanel buildIncomingConfigurationsTab() {
        incomingWssTable = JTableFactory.getInstance().makeJTable(new IncomingWssTableModel());
        incomingWssTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                removeIncomingWssAction.setEnabled(incomingWssTable.getSelectedRow() != -1);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.add(buildIncomingWssToolbar(), BorderLayout.NORTH);

        incomingWssDecryptionCryptoComboBox = new JComboBox(new KeystoresComboBoxModel(wssContainer, null, false));
        incomingWssTable.getColumnModel().getColumn(1)
                .setCellEditor(new DefaultCellEditor(incomingWssDecryptionCryptoComboBox));

        incomingWssSignatureCryptoComboBox = new JComboBox(new KeystoresComboBoxModel(wssContainer, null, false));
        incomingWssTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(incomingWssSignatureCryptoComboBox));

        incomingWssTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        incomingWssTable.getColumnModel().getColumn(3).setCellRenderer(new PasswordTableCellRenderer());

        p.add(new JScrollPane(incomingWssTable), BorderLayout.CENTER);
        panel.add(p, BorderLayout.CENTER);
        return panel;
    }

    private Component buildIncomingWssToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(new AddIncomingWssAction()));
        removeIncomingWssAction = new RemoveIncomingWssAction();
        toolbar.addFixed(UISupport.createToolbarButton(removeIncomingWssAction));

        toolbar.addGlue();
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.INCOMINGWSS_HELP_URL)));

        return toolbar;
    }

    private JPanel buildCryptoTable(final CryptoType cryptoType) {
        final JTable cryptoTable = JTableFactory.getInstance().makeJTable(new CryptoTableModel(cryptoType));

        switch (cryptoType) {
            case KEYSTORE:
                keystoreTable = cryptoTable;
                break;
            case TRUSTSTORE:
                truststoreTable = cryptoTable;
                break;
            default:
                throw new IllegalArgumentException(ILLEGAL_CRYPTO_TYPE_MESSAGE);
        }

        JPanel panel = new JPanel(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());

        p.add(buildCryptoToolbar(cryptoType), BorderLayout.NORTH);

        cryptoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                switch (cryptoType) {
                    case KEYSTORE:
                        removeKeystoreAction.setEnabled(cryptoTable.getSelectedRow() != -1);
                        break;
                    case TRUSTSTORE:
                        removeTruststoreAction.setEnabled(cryptoTable.getSelectedRow() != -1);
                        break;
                    default:
                        throw new IllegalArgumentException(ILLEGAL_CRYPTO_TYPE_MESSAGE);
                }
            }
        });

        cryptoTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        cryptoTable.getColumnModel().getColumn(2).setCellRenderer(new PasswordTableCellRenderer());
        cryptoTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        cryptoTable.getColumnModel().getColumn(4).setCellRenderer(new PasswordTableCellRenderer());

        p.add(new JScrollPane(cryptoTable), BorderLayout.CENTER);

        panel.add(p, BorderLayout.CENTER);
        return panel;
    }

    private Component buildCryptoToolbar(CryptoType cryptoType) {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(new AddCryptoAction(cryptoType)));

        RemoveCryptoAction removeCryptoAction = new RemoveCryptoAction(cryptoType);
        switch (cryptoType) {
            case KEYSTORE:
                removeKeystoreAction = removeCryptoAction;
                break;
            case TRUSTSTORE:
                removeTruststoreAction = removeCryptoAction;
                break;
            default:
                throw new IllegalArgumentException(ILLEGAL_CRYPTO_TYPE_MESSAGE);
        }

        toolbar.addFixed(UISupport.createToolbarButton(removeCryptoAction));

        toolbar.addGlue();
        toolbar.addFixed(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.CRYPTOSWSS_HELP_URL)));
        return toolbar;
    }

    private void setEntriesSplitPaneToEmpty() {
        entriesListContainer.getViewport().add(new JList());
        entriesSplitPane.setRightComponent(new JPanel());
    }

    private JTable getCryptoTable(CryptoType cryptoType) {
        switch (cryptoType) {
            case KEYSTORE:
                return keystoreTable;
            case TRUSTSTORE:
                return truststoreTable;
            default:
                throw new IllegalArgumentException(ILLEGAL_CRYPTO_TYPE_MESSAGE);
        }
    }

    // :: Table models ::

    public class OutgoingWssTableModel extends AbstractTableModel {
        private List<OutgoingWss> outgoingWss;

        public OutgoingWssTableModel() {
            outgoingWss = wssContainer.getOutgoingWssList();
        }

        public void release() {
            outgoingWss = null;
        }

        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Default Username/Alias";
                case 2:
                    return "Default Password";
                case 3:
                    return "Actor";
                case 4:
                    return "Must Understand";
            }

            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 4 ? Boolean.class : String.class;
        }

        public int getRowCount() {
            return outgoingWss == null ? 0 : outgoingWss.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            OutgoingWss outgoing = outgoingWss.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return outgoing.getName();
                case 1:
                    return outgoing.getUsername();
                case 2:
                    return outgoing.getPassword();
                case 3:
                    return outgoing.getActor();
                case 4:
                    return outgoing.getMustUnderstand();
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            OutgoingWss outgoing = outgoingWss.get(rowIndex);

            switch (columnIndex) {
                case 1:
                    outgoing.setUsername(aValue == null ? null : aValue.toString());
                    break;
                case 2:
                    outgoing.setPassword(aValue == null ? null : aValue.toString());
                    break;
                case 3:
                    outgoing.setActor(aValue == null ? null : aValue.toString());
                    break;
                case 4:
                    outgoing.setMustUnderstand(aValue == null ? false : (Boolean) aValue);
                    break;
            }
        }

        public void outgoingWssAdded(OutgoingWss outgoing) {
            outgoingWss.add(outgoing);
            fireTableRowsInserted(outgoingWss.size() - 1, outgoingWss.size() - 1);
        }

        public void outgoingWssRemoved(OutgoingWss outgoing) {
            int ix = outgoingWss.indexOf(outgoing);
            if (ix != -1) {
                outgoingWss.remove(ix);
                fireTableRowsDeleted(ix, ix);
            }
        }
    }

    public class IncomingWssTableModel extends AbstractTableModel {
        private List<IncomingWss> incomingWss;

        public IncomingWssTableModel() {
            incomingWss = wssContainer.getIncomingWssList();
        }

        public void release() {
            incomingWss = null;
        }

        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Name";
                case 1:
                    return "Decrypt Keystore";
                case 2:
                    return "Signature Keystore";
                case 3:
                    return "Password";
            }

            return null;
        }

        public int getRowCount() {
            return incomingWss == null ? 0 : incomingWss.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            IncomingWss incoming = incomingWss.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return incoming.getName();
                case 1:
                    return wssContainer.getCryptoByName(incoming.getDecryptCrypto());
                case 2:
                    return wssContainer.getCryptoByName(incoming.getSignatureCrypto());
                case 3:
                    return incoming.getDecryptPassword();
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            IncomingWss incoming = incomingWss.get(rowIndex);

            switch (columnIndex) {
                case 1:
                    incoming.setDecryptCrypto(aValue == null ? null : aValue.toString());
                    break;
                case 2:
                    incoming.setSignatureCrypto(aValue == null ? null : aValue.toString());
                    break;
                case 3:
                    incoming.setDecryptPassword(aValue == null ? null : aValue.toString());
                    break;
            }
        }

        public void incomingWssAdded(IncomingWss incoming) {
            incomingWss.add(incoming);
            fireTableRowsInserted(incomingWss.size() - 1, incomingWss.size() - 1);

        }

        public void incomingWssRemoved(IncomingWss incoming) {
            int ix = incomingWss.indexOf(incoming);
            if (ix != -1) {
                incomingWss.remove(ix);
                fireTableRowsDeleted(ix, ix);
            }
        }
    }

    public class CryptoTableModel extends AbstractTableModel {
        private static final String DEFAULT_OPTION = "<Default>";
        private List<WssCrypto> cryptos;

        public CryptoTableModel(CryptoType cryptoType) {
            cryptos = new ArrayList<WssCrypto>();
            for (WssCrypto crypto : wssContainer.getCryptoList()) {
                if (crypto.getType() == cryptoType) {
                    cryptos.add(crypto);
                }
            }
        }

        public void release() {
            cryptos = null;
        }

        public int getColumnCount() {
            // FIXME: Why not remove??
            // hide last column since this is autodetected in commons-ssl-0.3.10
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Source";
                case 1:
                    return "Status";
                case 2:
                    return "Password";
                case 3:
                    return "Default Alias";
                case 4:
                    return "Alias Password";
                case 5:
                    return "Security Provider";
            }

            return null;
        }

        public int getRowCount() {
            return cryptos == null ? 0 : cryptos.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            KeyMaterialWssCrypto crypto = (KeyMaterialWssCrypto) cryptos.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return crypto.getSource();
                case 1:
                    return crypto.getStatus();
                case 2:
                    return crypto.getPassword();
                case 3:
                    return crypto.getDefaultAlias();
                case 4:
                    return crypto.getAliasPassword();
                case 5:
                    return StringUtils.hasContent(crypto.getCryptoProvider()) ? crypto.getCryptoProvider() : DEFAULT_OPTION;
            }

            return null;
        }

        public WssCrypto getCryptoAt(int row) {
            return cryptos.get(row);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            KeyMaterialWssCrypto crypto = (KeyMaterialWssCrypto) cryptos.get(rowIndex);
            if (aValue == null || aValue.equals(DEFAULT_OPTION)) {
                aValue = "";
            }

            switch (columnIndex) {
                case 2:
                    crypto.setPassword(aValue.toString());
                    break;
                case 3:
                    crypto.setDefaultAlias(aValue.toString());
                    break;
                case 4:
                    crypto.setAliasPassword(aValue.toString());
                    break;
                case 5:
                    crypto.setCryptoProvider(aValue.toString());
                    break;
            }
        }

        public void cryptoAdded(WssCrypto crypto) {
            cryptos.add(crypto);
            fireTableRowsInserted(cryptos.size() - 1, cryptos.size() - 1);
        }

        public void cryptoRemoved(WssCrypto crypto) {
            int ix = cryptos.indexOf(crypto);
            if (ix != -1) {
                cryptos.remove(ix);
                fireTableRowsDeleted(ix, ix);
            }
        }
    }

    // :: Actions ::

    private class AddOutgoingWssAction extends AbstractAction {
        public AddOutgoingWssAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(SHORT_DESCRIPTION, "Adds a new Outgoing WSS Configuration");
        }

        public void actionPerformed(ActionEvent e) {
            String name = UISupport.prompt("Specify unique name for configuration", "New Outgoing WSS Configuration", "");
            if (StringUtils.hasContent(name) && wssContainer.getOutgoingWssByName(name) == null) {
                wssContainer.addOutgoingWss(name);
                outgoingWssTable.setRowSelectionInterval(outgoingWssTable.getRowCount() - 1,
                        outgoingWssTable.getRowCount() - 1);
            }
        }
    }

    private class RemoveOutgoingWssAction extends AbstractAction {
        public RemoveOutgoingWssAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(SHORT_DESCRIPTION, "Removes the selected Outgoing WSS Configuration");

            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int row = outgoingWssTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            if (UISupport.confirm("Removes selected configuration?", "Remove Configuration")) {
                wssContainer.removeOutgoingWssAt(row);
                setEntriesSplitPaneToEmpty();
            }
        }
    }

    public class AddOutgoingEntryAction extends AbstractAction {
        public AddOutgoingEntryAction() {
            putValue(SHORT_DESCRIPTION, "Adds a new WSS Entry");
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (selectedOutgoing == null) {
                return;
            }

            String type = UISupport.prompt("Select type of entry to add", "Add WSS Entry", WssEntryRegistry.get()
                    .getTypes());
            if (type != null) {
                selectedOutgoing.addEntry(type);
                entriesList.setSelectedIndex(entriesListModel.getSize() - 1);
            }
        }
    }

    public class RemoveOutgoingEntryAction extends AbstractAction {
        public RemoveOutgoingEntryAction() {
            putValue(SHORT_DESCRIPTION, "Removes the selected WSS-Entry");
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
        }

        public void actionPerformed(ActionEvent e) {
            if (selectedEntry == null) {
                return;
            }

            if (UISupport.confirm("Remove entry [" + selectedEntry.getLabel() + "]", "Remove WSS Entry")) {
                selectedOutgoing.removeEntry(selectedEntry);
                entriesSplitPane.setRightComponent(new JPanel());
            }
        }
    }

    private class MoveOutgoingEntryUpAction extends AbstractAction {
        public MoveOutgoingEntryUpAction() {
            super("Move entry Up");
            putValue(Action.SHORT_DESCRIPTION, "Moves selected entry up one row");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/up_arrow.gif"));
        }

        public void actionPerformed(ActionEvent e) {
            selectedOutgoing.moveEntry(selectedEntry, MOVE_UP);
        }
    }

    private class MoveOutgoingEntryDownAction extends AbstractAction {

        public MoveOutgoingEntryDownAction() {
            super("Move entry Down");
            putValue(Action.SHORT_DESCRIPTION, "Moves selected entry down one row");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/down_arrow.gif"));
        }

        public void actionPerformed(ActionEvent e) {
            selectedOutgoing.moveEntry(selectedEntry, MOVE_DOWN);
        }
    }

    private class AddIncomingWssAction extends AbstractAction {
        public AddIncomingWssAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(SHORT_DESCRIPTION, "Adds a new Incoming WSS Configuration");
        }

        public void actionPerformed(ActionEvent e) {
            String name = UISupport.prompt("Specify unique name for configuration", "New Incoming WSS Configuration", "");
            if (StringUtils.hasContent(name) && wssContainer.getIncomingWssByName(name) == null) {
                wssContainer.addIncomingWss(name);
                incomingWssTable.setRowSelectionInterval(incomingWssTable.getRowCount() - 1,
                        incomingWssTable.getRowCount() - 1);
            }
        }
    }

    private class RemoveIncomingWssAction extends AbstractAction {
        public RemoveIncomingWssAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(SHORT_DESCRIPTION, "Removes the selected Incoming WSS Configuration");

            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int row = incomingWssTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            if (UISupport.confirm("Removes selected configuration?", "Remove Configuration")) {
                wssContainer.removeIncomingWssAt(row);
            }
        }
    }

    public class ImportWssSettingsAction extends AbstractAction {
        public ImportWssSettingsAction() {
            putValue(SHORT_DESCRIPTION, "Imports an existing WS-Security configuration from another project");
            putValue(SMALL_ICON, UISupport.createImageIcon("/load_properties.gif"));
        }

        public void actionPerformed(ActionEvent e) {
            String[] names = ModelSupport.getNames(((WorkspaceImpl) SoapUI.getWorkspace()).getOpenProjectList());
            String projectName = UISupport.prompt("Select project to import from", "Import WSS Settings", names);
            if (projectName != null) {
                WsdlProject prj = (WsdlProject) SoapUI.getWorkspace().getProjectByName(projectName);
                wssContainer.importConfig(prj.getWssContainer());
            }
        }
    }

    private class AddCryptoAction extends AbstractAction {
        private final CryptoType cryptoType;

        public AddCryptoAction(CryptoType cryptoType) {
            this.cryptoType = cryptoType;
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(SHORT_DESCRIPTION, "Adds a new " + cryptoType.toString() + " to this configuration");
        }

        public void actionPerformed(ActionEvent e) {
            JTable cryptoTable = getCryptoTable(cryptoType);

            File file = UISupport.getFileDialogs().open(this, "Select Key Material", null, null, null);
            if (file != null) {
                String password = new String(UISupport.promptPassword("Specify password for [" + file.getName() + "]",
                        "Add Key Material"));
                wssContainer.addCrypto(file.getAbsolutePath(), password, cryptoType);
                cryptoTable.setRowSelectionInterval(cryptoTable.getRowCount() - 1, cryptoTable.getRowCount() - 1);
            }
        }
    }

    private class RemoveCryptoAction extends AbstractAction {
        private final CryptoType cryptoType;

        public RemoveCryptoAction(CryptoType cryptoType) {
            this.cryptoType = cryptoType;
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(SHORT_DESCRIPTION, "Removes the selected " + cryptoType + " from this configuration");

            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            JTable cryptoTable = getCryptoTable(cryptoType);

            int row = cryptoTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            CryptoTableModel tableModel = (CryptoTableModel) cryptoTable.getModel();
            WssCrypto crypto = tableModel.getCryptoAt(row);

            if (UISupport.confirm("Removes selected " + cryptoType + "?", "Remove " + cryptoType)) {
                wssContainer.removeCrypto(crypto);
            }
        }
    }

    // :: Listeners ::

    private class InternalWssContainerListener extends WssContainerListenerAdapter {
        @Override
        public void outgoingWssAdded(OutgoingWss outgoingWss) {
            ((OutgoingWssTableModel) outgoingWssTable.getModel()).outgoingWssAdded(outgoingWss);
        }

        @Override
        public void outgoingWssRemoved(OutgoingWss outgoingWss) {
            ((OutgoingWssTableModel) outgoingWssTable.getModel()).outgoingWssRemoved(outgoingWss);
        }

        @Override
        public void outgoingWssEntryAdded(WssEntry entry) {
            if (entry.getOutgoingWss() == selectedOutgoing) {
                entriesListModel.addElement(entry);
            }
        }

        @Override
        public void outgoingWssEntryRemoved(WssEntry entry) {
            if (entry.getOutgoingWss() == selectedOutgoing) {
                entriesListModel.removeElement(entry);
            }
        }

        @Override
        public void outgoingWssEntryMoved(WssEntry entry, int offset) {
            if (entry.getOutgoingWss() == selectedOutgoing) {
                int indexBeforeMove = entriesListModel.indexOf(entry);
                if ((offset == MOVE_UP && indexBeforeMove > 0)
                        || (offset == MOVE_DOWN && indexBeforeMove < entriesListModel.size() - 1)) {
                    WssEntry adjacentEntry = (WssEntry) entriesListModel.get(indexBeforeMove + offset);

                    entriesListModel.set(indexBeforeMove + offset, entry);
                    entriesListModel.set(indexBeforeMove, adjacentEntry);

                    entriesList.setSelectedIndex(indexBeforeMove + offset);
                }
            }
        }

        @Override
        public void incomingWssAdded(IncomingWss incomingWss) {
            ((IncomingWssTableModel) incomingWssTable.getModel()).incomingWssAdded(incomingWss);

        }

        @Override
        public void incomingWssRemoved(IncomingWss incomingWss) {
            ((IncomingWssTableModel) incomingWssTable.getModel()).incomingWssRemoved(incomingWss);

        }

        @Override
        public void cryptoAdded(WssCrypto crypto) {
            JTable cryptoTable = getCryptoTable(crypto.getType());
            ((CryptoTableModel) cryptoTable.getModel()).cryptoAdded(crypto);
        }

        @Override
        public void cryptoRemoved(WssCrypto crypto) {
            JTable cryptoTable = getCryptoTable(crypto.getType());
            ((CryptoTableModel) cryptoTable.getModel()).cryptoRemoved(crypto);
        }
    }

    // :: Table cell renderer::

    public static class PasswordTableCellRenderer extends JPasswordField implements TableCellRenderer {
        public PasswordTableCellRenderer() {
            setEditable(false);
            setBorder(null);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setBackground(table.getBackground());
            setText(value == null ? "" : value.toString());
            return this;
        }
    }
}
