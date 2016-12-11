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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.PathLanguage;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep.PropertyTransferResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.TestRunListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * DesktopPanel for TransferResponseValuesTestStep
 *
 * @author Ole.Matzura
 */

public class PropertyTransfersDesktopPanel extends ModelItemDesktopPanel<PropertyTransfersTestStep> {
    private final PropertyTransfersTestStep transferStep;
    private final PropertyChangeListener transferListListener;
    private DefaultListModel listModel;
    private JList transferList;
    private JTextArea sourceArea;
    private JTextArea targetArea;
    private JButton copyButton;
    private JButton deleteButton;
    private JButton declareButton;
    private JComboBox sourcePropertyCombo;
    private JComboBox sourceTransferLanguageCombo;
    private JComboBox targetPropertyCombo;
    private JComboBox targetTransferLanguageCombo;
    private JComboBox sourceStepCombo;
    private JComboBox targetStepCombo;
    private DefaultComboBoxModel sourceStepModel;
    private DefaultComboBoxModel targetStepModel;
    private TestStepPropertiesListener sourceStepPropertiesListener;
    private TestStepPropertiesListener targetStepPropertiesListener;
    private TransferPropertyChangeListener transferPropertyChangeListener = new TransferPropertyChangeListener();
    private boolean selecting;
    private InternalTestSuiteListener testSuiteListener;
    private TestRunComponentEnabler componentEnabler;
    private JCheckBox failTransferCheckBox;
    private JButton runButton;
    private JButton renameButton;
    private JCheckBox setNullCheckBox;
    private JCheckBox transferTextContentCheckBox;
    private JCheckBox ignoreEmptyCheckBox;
    private JCheckBox transferAllCheckBox;
    private JCheckBox entitizeCheckBox;
    private JCheckBox transferChildNodesCheckBox;
    private TransfersTableModel transferLogTableModel;
    private InternalTestRunListener testRunListener;
    private JComponentInspector<JComponent> logInspector;
    private JButton runAllButton;
    private JInspectorPanel inspectorPanel;
    private JXTable logTable;
    private JToggleButton disableButton;

    public PropertyTransfersDesktopPanel(PropertyTransfersTestStep testStep) {
        super(testStep);
        this.transferStep = testStep;
        componentEnabler = new TestRunComponentEnabler(testStep.getTestCase());

        buildUI();

        testSuiteListener = new InternalTestSuiteListener();
        transferStep.getTestCase().getTestSuite().addTestSuiteListener(testSuiteListener);

        testRunListener = new InternalTestRunListener();
        transferStep.getTestCase().addTestRunListener(testRunListener);
        transferListListener = new ListUpdater();
        transferStep.addPropertyChangeListener(PropertyTransfersTestStep.TRANSFERS, transferListListener);
    }

    protected void buildUI() {
        JSplitPane splitPane = UISupport.createHorizontalSplit();

        listModel = createListModel();

        transferList = new JList(listModel);
        transferList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transferList.addListSelectionListener(new TransferListSelectionListener());
        componentEnabler.add(transferList);

        JScrollPane listScrollPane = new JScrollPane(transferList);
        UISupport.addTitledBorder(listScrollPane, "Transfers");

        JPanel p = new JPanel(new BorderLayout());
        p.add(listScrollPane, BorderLayout.CENTER);
        p.add(createPropertiesToolbar(), BorderLayout.NORTH);

        splitPane.setLeftComponent(p);

        JSplitPane innerSplit = UISupport.createVerticalSplit();
        innerSplit.setBorder(null);
        sourceArea = new JUndoableTextArea();
        sourceArea.setToolTipText("XPath selection from source property");
        sourceArea.setEnabled(false);
        sourceArea.getDocument().addDocumentListener(new SourceAreaDocumentListener());
        componentEnabler.add(sourceArea);

        targetArea = new JUndoableTextArea();
        targetArea.setToolTipText("XPath target in target property");
        targetArea.setEnabled(false);
        targetArea.getDocument().addDocumentListener(new TargetAreaDocumentListener());
        componentEnabler.add(targetArea);

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(new JScrollPane(sourceArea), BorderLayout.CENTER);
        JXToolBar toolbar = createSourceToolbar();
        sourcePanel.add(toolbar, BorderLayout.NORTH);
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        innerSplit.setTopComponent(sourcePanel);

        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.add(new JScrollPane(targetArea), BorderLayout.CENTER);
        toolbar = createTargetToolbar();
        targetPanel.add(toolbar, BorderLayout.NORTH);
        targetPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));

        innerSplit.setBottomComponent(targetPanel);

        innerSplit.setResizeWeight(0.5);
        innerSplit.setDividerLocation(0.5);

        JPanel panel = createTransferOptions();

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(innerSplit, BorderLayout.CENTER);
        innerPanel.add(panel, BorderLayout.SOUTH);
        innerPanel.add(createConfigToolbar(), BorderLayout.NORTH);

        splitPane.setRightComponent(innerPanel);
        splitPane.setResizeWeight(0.1);
        splitPane.setDividerLocation(120);

        inspectorPanel = JInspectorPanelFactory.build(splitPane);
        logInspector = new JComponentInspector<JComponent>(buildLog(), "Transfer Log (0)",
                "A log of performed transfers while the editor was open", true);
        inspectorPanel.addInspector(logInspector);
        add(inspectorPanel.getComponent(), BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        setPreferredSize(new Dimension(550, 400));

        if (listModel.getSize() > 0) {
            transferList.setSelectedIndex(0);
        } else {
            setSelectedTransfer(null);
        }

        componentEnabler.add(deleteButton);
        componentEnabler.add(declareButton);
        componentEnabler.add(runButton);
        componentEnabler.add(runAllButton);
        componentEnabler.add(copyButton);
        componentEnabler.add(renameButton);
        componentEnabler.add(failTransferCheckBox);
        componentEnabler.add(setNullCheckBox);
        componentEnabler.add(transferTextContentCheckBox);
        componentEnabler.add(ignoreEmptyCheckBox);
        componentEnabler.add(transferAllCheckBox);
        componentEnabler.add(entitizeCheckBox);
        componentEnabler.add(transferChildNodesCheckBox);

    }

    private DefaultListModel createListModel() {
        DefaultListModel listModel = new DefaultListModel();

        for (int c = 0; c < transferStep.getTransferCount(); c++) {
            String name = transferStep.getTransferAt(c).getName();
            if (transferStep.getTransferAt(c).isDisabled()) {
                name += " (disabled)";
            }

            listModel.addElement(name);
        }
        return listModel;
    }

    private JComponent buildLog() {
        JPanel logPanel = new JPanel(new BorderLayout());

        transferLogTableModel = new TransfersTableModel();
        logTable = JTableFactory.getInstance().makeJXTable(transferLogTableModel);
        logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int row = logTable.getSelectedRow();
                if (row != -1) {
                    String transferName = transferLogTableModel.getValueAt(row, 1).toString();
                    int ix = listModel.indexOf(transferName);
                    if (ix != -1) {
                        transferList.setSelectedIndex(ix);
                    }
                }
            }
        });

        logTable.setHorizontalScrollEnabled(true);
        logTable.packAll();

        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.add(new ClearLogAction());

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
                scrollPane.getBorder()));

        logPanel.add(toolbar, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        return logPanel;
    }

    protected JXToolBar createPropertiesToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();
        toolbar.addFixed(UISupport.createToolbarButton(new AddAction()));
        deleteButton = UISupport.createToolbarButton(new DeleteAction());
        deleteButton.setEnabled(false);
        toolbar.addFixed(deleteButton);
        copyButton = UISupport.createToolbarButton(new CopyAction());
        copyButton.setEnabled(false);
        toolbar.addFixed(copyButton);
        renameButton = UISupport.createToolbarButton(new RenameAction());
        renameButton.setEnabled(false);
        toolbar.addFixed(renameButton);

        disableButton = new JToggleButton(new DisableAction());
        disableButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);
        disableButton.setSelectedIcon(UISupport.createImageIcon("/bullet_red.png"));
        toolbar.addSeparator();
        toolbar.addFixed(disableButton);

        return toolbar;
    }

    protected JXToolBar createConfigToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        runButton = UISupport.createToolbarButton(new RunAction());
        runButton.setEnabled(transferList.getSelectedIndex() != -1);
        toolbar.addFixed(runButton);

        runAllButton = UISupport.createToolbarButton(new RunAllAction());
        runAllButton.setEnabled(transferStep.getTransferCount() > 0);
        toolbar.addFixed(runAllButton);

        declareButton = UISupport.createToolbarButton(new DeclareNamespacesAction());
        declareButton.setEnabled(false);
        toolbar.addFixed(declareButton);
        toolbar.addGlue();
        toolbar.addFixed(UISupport
                .createToolbarButton(new ShowOnlineHelpAction(HelpUrls.TRANSFERSTEPEDITOR_HELP_URL)));
        return toolbar;
    }

    protected JPanel createTransferOptions() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        failTransferCheckBox = new JCheckBox("Fail transfer on error", false);
        failTransferCheckBox.setToolTipText("Fails the Property Transfer Step if an error occurs");
        failTransferCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setFailOnError(failTransferCheckBox.isSelected());
                }
            }
        });

        setNullCheckBox = new JCheckBox("Set null on missing source", false);
        setNullCheckBox.setToolTipText("Will set target to null if source is missing or null");
        setNullCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setSetNullOnMissingSource(setNullCheckBox.isSelected());
                }
            }
        });

        transferTextContentCheckBox = new JCheckBox("Transfer text content", false);
        transferTextContentCheckBox.setToolTipText("Will only transfer text content of source/target elements");
        transferTextContentCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setTransferTextContent(transferTextContentCheckBox.isSelected());
                }
            }
        });

        ignoreEmptyCheckBox = new JCheckBox("Ignore empty/missing values", false);
        ignoreEmptyCheckBox.setToolTipText("Will not transfer empty or missing values");
        ignoreEmptyCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setIgnoreEmpty(ignoreEmptyCheckBox.isSelected());
                }
            }
        });

        transferAllCheckBox = new JCheckBox("Transfer to all", false);
        transferAllCheckBox.setToolTipText("Will transfer to all matching target selections");
        transferAllCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setTransferToAll(transferAllCheckBox.isSelected());
                }
            }
        });

        entitizeCheckBox = new JCheckBox("Entitize transferred value(s)", false);
        entitizeCheckBox.setToolTipText("Entitize transferred values when possible");
        entitizeCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setEntitize(entitizeCheckBox.isSelected());
                }
            }
        });

        transferChildNodesCheckBox = new JCheckBox("Transfer Child Nodes", false);
        transferChildNodesCheckBox
                .setToolTipText("Transfers child nodes of specified source node to children of specified target");
        transferChildNodesCheckBox.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                PropertyTransfer currentTransfer = getCurrentTransfer();
                if (currentTransfer != null) {
                    currentTransfer.setTransferChildNodes(transferChildNodesCheckBox.isSelected());
                }
            }
        });

        panel.add(failTransferCheckBox);
        panel.add(setNullCheckBox);
        panel.add(transferTextContentCheckBox);
        panel.add(ignoreEmptyCheckBox);
        panel.add(transferAllCheckBox);
        panel.add(transferChildNodesCheckBox);
        panel.add(entitizeCheckBox);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return panel;
    }

    protected JXToolBar createTargetToolbar() {
        JXToolBar toolbar;
        toolbar = UISupport.createToolbar();
        toolbar.addSpace(3);
        toolbar.addFixed(new JLabel("<html><b>Target: </b></html>"));
        toolbar.addUnrelatedGap();

        targetStepCombo.setSelectedItem(null);
        targetStepCombo.setToolTipText("The step the value will be transferred to");
        targetStepCombo.setEnabled(false);
        targetStepCombo.addItemListener(new StepComboItemListener(targetPropertyCombo, targetStepPropertiesListener));
        targetStepCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !selecting) {
                    TestPropertyHolder targetStep = (TestPropertyHolder) targetStepCombo.getSelectedItem();
                    PropertyTransfer valueTransfer = getCurrentTransfer();

                    if (valueTransfer != null) {
                        String name;
                        if (targetStep == PropertyExpansionUtils.getGlobalProperties()) {
                            name = PropertyExpansion.GLOBAL_REFERENCE;
                        } else if (targetStep == transferStep.getTestCase().getTestSuite().getProject()) {
                            name = PropertyExpansion.PROJECT_REFERENCE;
                        } else if (targetStep == transferStep.getTestCase().getTestSuite()) {
                            name = PropertyExpansion.TESTSUITE_REFERENCE;
                        } else if (targetStep == transferStep.getTestCase()) {
                            name = PropertyExpansion.TESTCASE_REFERENCE;
                        } else {
                            name = targetStep.getModelItem().getName();
                        }

                        valueTransfer.setTargetStepName(name);
                    }
                }
            }
        });

        toolbar.add(UISupport.setFixedSize(targetStepCombo, 180, 21));
        toolbar.addUnrelatedGap();

        toolbar.addFixed(new JLabel(" Property: "));
        toolbar.addRelatedGap();

        targetPropertyCombo.setToolTipText("The property the value will be transferred to");
        targetPropertyCombo.setEnabled(false);
        targetPropertyCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !selecting) {
                    TestProperty targetProperty = (TestProperty) targetPropertyCombo.getSelectedItem();
                    PropertyTransfer valueTransfer = getCurrentTransfer();

                    if (valueTransfer != null) {
                        valueTransfer.setTargetPropertyName(targetProperty.getName());
                    }
                }
            }
        });
        String context = "Target";
        targetTransferLanguageCombo = createTransferLanguageComboBox(context);
        targetTransferLanguageCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getCurrentTransfer().setTargetPathLanguage((PathLanguage) e.getItem());
                }
            }
        });


        toolbar.add(UISupport.setFixedSize(targetPropertyCombo, 130, 21));
        toolbar.addRelatedGap();
        toolbar.addFixed(new JLabel("Path language: "));
        toolbar.add(UISupport.setFixedSize(targetTransferLanguageCombo, 115, 21));
        customizeTargetToolbar(toolbar);
        return toolbar;
    }

    private JComboBox createTransferLanguageComboBox(String context) {
        DefaultComboBoxModel transferLanguageModel = new DefaultComboBoxModel(PathLanguage.values());
        return UISupport.addTooltipListener(new JComboBox(transferLanguageModel), context + " Transfer Path Language");
    }

    protected void customizeTargetToolbar(JXToolBar toolbar) {
        toolbar.addGlue();
    }

    protected JXToolBar createSourceToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.addSpace(3);
        toolbar.addFixed(new JLabel("<html><b>Source: </b></html>"));
        toolbar.addUnrelatedGap();

        sourcePropertyCombo = UISupport.addTooltipListener(new JComboBox(), "Source Property");
        sourceStepModel = new DefaultComboBoxModel();
        sourceStepCombo = UISupport.addTooltipListener(new JComboBox(sourceStepModel),
                "Source Step or Property Container");
        sourceStepCombo.setRenderer(new StepComboRenderer());
        sourcePropertyCombo.setRenderer(new PropertyComboRenderer());
        sourceTransferLanguageCombo = createTransferLanguageComboBox("Source");

        componentEnabler.add(sourcePropertyCombo);
        componentEnabler.add(sourceStepCombo);
        componentEnabler.add(sourceTransferLanguageCombo);

        targetPropertyCombo = UISupport.addTooltipListener(new JComboBox(), "Target Property");
        targetStepModel = new DefaultComboBoxModel();
        targetStepCombo = UISupport.addTooltipListener(new JComboBox(targetStepModel),
                "Target Step or Property Container");
        targetStepCombo.setRenderer(new StepComboRenderer());
        targetPropertyCombo.setRenderer(new PropertyComboRenderer());

        componentEnabler.add(targetPropertyCombo);
        componentEnabler.add(targetStepCombo);

        sourceStepPropertiesListener = new TestStepPropertiesListener(sourcePropertyCombo);
        targetStepPropertiesListener = new TestStepPropertiesListener(targetPropertyCombo);

        sourceStepModel.addElement(PropertyExpansionUtils.getGlobalProperties());
        sourceStepModel.addElement(transferStep.getTestCase().getTestSuite().getProject());
        sourceStepModel.addElement(transferStep.getTestCase().getTestSuite());
        sourceStepModel.addElement(transferStep.getTestCase());

        for (int c = 0; c < transferStep.getTestCase().getTestStepCount(); c++) {
            WsdlTestStep testStep = transferStep.getTestCase().getTestStepAt(c);
            if (testStep == transferStep) {
                continue;
            }

            sourceStepModel.addElement(testStep);
        }

        for (int c = 0; c < sourceStepModel.getSize(); c++) {
            targetStepModel.addElement(sourceStepModel.getElementAt(c));
        }

        sourceStepCombo.setSelectedItem(null);
        sourceStepCombo.setEnabled(false);
        sourceStepCombo.addItemListener(new StepComboItemListener(sourcePropertyCombo, sourceStepPropertiesListener));
        sourceStepCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !selecting) {
                    TestPropertyHolder sourceStep = (TestPropertyHolder) sourceStepCombo.getSelectedItem();
                    PropertyTransfer valueTransfer = getCurrentTransfer();

                    if (valueTransfer != null) {
                        String name;
                        if (sourceStep == PropertyExpansionUtils.getGlobalProperties()) {
                            name = PropertyExpansion.GLOBAL_REFERENCE;
                        } else if (sourceStep == transferStep.getTestCase().getTestSuite().getProject()) {
                            name = PropertyExpansion.PROJECT_REFERENCE;
                        } else if (sourceStep == transferStep.getTestCase().getTestSuite()) {
                            name = PropertyExpansion.TESTSUITE_REFERENCE;
                        } else if (sourceStep == transferStep.getTestCase()) {
                            name = PropertyExpansion.TESTCASE_REFERENCE;
                        } else {
                            name = sourceStep.getModelItem().getName();
                        }

                        valueTransfer.setSourceStepName(name);
                    }
                }
            }
        });

        toolbar.add(UISupport.setFixedSize(sourceStepCombo, 180, 21));
        toolbar.addUnrelatedGap();

        toolbar.addFixed(new JLabel(" Property: "));
        toolbar.addRelatedGap();

        sourcePropertyCombo.setToolTipText("The property the value will be transferred from");
        sourcePropertyCombo.setEnabled(false);
        sourcePropertyCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !selecting) {
                    TestProperty sourceProperty = (TestProperty) sourcePropertyCombo.getSelectedItem();
                    PropertyTransfer valueTransfer = getCurrentTransfer();

                    if (valueTransfer != null) {
                        valueTransfer.setSourcePropertyName(sourceProperty.getName());
                    }
                }
            }
        });

        toolbar.add(UISupport.setFixedSize(sourcePropertyCombo, 130, 21));
        toolbar.addRelatedGap();
        toolbar.addFixed(new JLabel("Path language: "));
        sourceTransferLanguageCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getCurrentTransfer().setSourcePathLanguage((PathLanguage) e.getItem());
                }
            }
        });
        toolbar.add(UISupport.setFixedSize(sourceTransferLanguageCombo, 115, 21));

        customizeSourceToolbar(toolbar);
        return toolbar;
    }

    protected void customizeSourceToolbar(JXToolBar toolbar) {
        toolbar.addGlue();
    }

    public PropertyTransfer getCurrentTransfer() {
        int ix = transferList.getSelectedIndex();
        return ix == -1 ? null : transferStep.getTransferAt(ix);
    }

    /**
     * Listen for testStep property changes and update properties combo
     * accordingly
     */

    private final class TestStepPropertiesListener implements TestPropertyListener {
        private final JComboBox combo;

        public TestStepPropertiesListener(JComboBox combo) {
            this.combo = combo;
        }

        public void propertyAdded(String name) {
            TestProperty property = combo == targetPropertyCombo ? getCurrentTransfer().getTargetStep().getProperty(name)
                    : getCurrentTransfer().getSourceStep().getProperty(name);

            combo.addItem(property);
            combo.setEnabled(true);
        }

        public void propertyRemoved(String name) {
            if (combo.getSelectedItem() != null && ((TestProperty) combo.getSelectedItem()).getName().equals(name)) {
                combo.setSelectedItem(null);
            }

            for (int c = 0; c < combo.getItemCount(); c++) {
                if (((TestProperty) combo.getItemAt(c)).getName().equals(name)) {
                    combo.removeItemAt(c);
                    break;
                }
            }

            combo.setEnabled(combo.getItemCount() > 0);
        }

        public void propertyRenamed(String oldName, String newName) {
        }

        public void propertyValueChanged(String name, String oldValue, String newValue) {
        }

        public void propertyMoved(String name, int oldIndex, int newIndex) {
            combo.removeItemAt(oldIndex);

            TestProperty property = combo == targetPropertyCombo ? getCurrentTransfer().getTargetStep().getProperty(name)
                    : getCurrentTransfer().getSourceStep().getProperty(name);

            combo.insertItemAt(property, newIndex);
        }
    }

    /**
     * Listen for teststep changes and update source/target step combos
     * accordingly
     */

    private final class InternalTestSuiteListener extends TestSuiteListenerAdapter {
        public void testStepAdded(TestStep testStep, int index) {
            if (testStep.getTestCase() == transferStep.getTestCase()) {
                sourceStepModel.addElement(testStep);
                targetStepModel.addElement(testStep);
            }
        }

        public void testStepMoved(TestStep testStep, int fromIndex, int offset) {
            if (testStep.getTestCase() == transferStep.getTestCase()) {
                String testStepName = testStep.getName();
                if (sourceStepModel.getIndexOf(testStepName) == fromIndex) {
                    String sourceStep = (String) sourceStepCombo.getSelectedItem();
                    String sourceProperty = (String) sourcePropertyCombo.getSelectedItem();

                    sourceStepModel.removeElementAt(fromIndex);
                    if (fromIndex + offset > sourceStepModel.getSize()) {
                        sourceStepModel.addElement(testStepName);
                    } else {
                        sourceStepModel.insertElementAt(testStepName, fromIndex + offset);
                    }

                    sourceStepCombo.setSelectedItem(sourceStep);
                    sourcePropertyCombo.setSelectedItem(sourceProperty);
                }

                if (targetStepModel.getIndexOf(testStepName) == fromIndex) {
                    String targetStep = (String) targetStepCombo.getSelectedItem();
                    String targetProperty = (String) targetPropertyCombo.getSelectedItem();

                    targetStepModel.removeElementAt(fromIndex);
                    if (fromIndex + offset > targetStepModel.getSize()) {
                        targetStepModel.addElement(testStepName);
                    } else {
                        targetStepModel.insertElementAt(testStepName, fromIndex + offset);
                    }

                    targetStepCombo.setSelectedItem(targetStep);
                    targetPropertyCombo.setSelectedItem(targetProperty);
                }
            }
        }

        public void testStepRemoved(TestStep testStep, int index) {
            if (testStep.getTestCase() == transferStep.getTestCase()) {
                sourceStepModel.removeElement(testStep);
                targetStepModel.removeElement(testStep);
            }
        }
    }

    /**
     * Listen to step selections and update properties combo accordingly
     */

    private final class StepComboItemListener implements ItemListener {
        private final JComboBox propertyCombo;
        private final TestStepPropertiesListener testStepPropertiesListener;

        public StepComboItemListener(final JComboBox propertyCombo, TestStepPropertiesListener testStepPropertiesListener) {
            this.propertyCombo = propertyCombo;
            this.testStepPropertiesListener = testStepPropertiesListener;
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                TestPropertyHolder selectedItem = (TestPropertyHolder) e.getItem();
                String[] propertyNames = selectedItem.getPropertyNames();

                // remove read-only properties from target property
                if (propertyCombo == targetPropertyCombo) {
                    List<String> names = new ArrayList<String>();
                    for (String name : propertyNames) {
                        TestProperty property = selectedItem.getProperty(name);
                        if (property != null && !property.isReadOnly()) {
                            names.add(property.getName());
                        }
                    }

                    propertyNames = names.toArray(new String[names.size()]);
                }

                DefaultComboBoxModel model = new DefaultComboBoxModel();
                for (String name : propertyNames) {
                    model.addElement(selectedItem.getProperty(name));
                }

                propertyCombo.setModel(model);
                propertyCombo.setEnabled(propertyNames.length > 0);

                if (propertyCombo == targetPropertyCombo) {
                    propertyCombo.setSelectedItem(getCurrentTransfer().getTargetProperty());
                } else {
                    propertyCombo.setSelectedItem(getCurrentTransfer().getSourceProperty());
                }

                selectedItem.addTestPropertyListener(testStepPropertiesListener);
            } else {
                propertyCombo.removeAllItems();
                propertyCombo.setEnabled(false);
            }
        }
    }

    /**
     * Handle updates to source path
     */

    private final class SourceAreaDocumentListener extends DocumentListenerAdapter {
        public void update(Document document) {
            int ix = transferList.getSelectedIndex();
            if (ix != -1) {
                transferStep.getTransferAt(ix).setSourcePath(sourceArea.getText());
            }
        }
    }

    /**
     * Handle updates to target path
     */

    private final class TargetAreaDocumentListener extends DocumentListenerAdapter {
        public void update(Document document) {
            int ix = transferList.getSelectedIndex();
            if (ix != -1) {
                transferStep.getTransferAt(ix).setTargetPath(targetArea.getText());
            }
        }
    }

    /**
     * Listen to selection changes in transfer list and update controls
     * accordingly
     */

    private final class TransferListSelectionListener implements ListSelectionListener {
        private PropertyTransfer transfer;

        public void valueChanged(ListSelectionEvent e) {
            selecting = true;

            if (transfer != null) {
                transfer.removePropertyChangeListener(transferPropertyChangeListener);
            }

            transfer = getCurrentTransfer();
            setSelectedTransfer(transfer);
            selecting = false;
        }
    }

    protected void setSelectedTransfer(PropertyTransfer transfer) {
        if (transfer == null) {
            sourceArea.setText("");
            targetArea.setText("");

            sourcePropertyCombo.removeAllItems();
            targetPropertyCombo.removeAllItems();

            sourceStepCombo.setSelectedIndex(-1);
            targetStepCombo.setSelectedIndex(-1);
        } else {
            transfer.addPropertyChangeListener(transferPropertyChangeListener);

            sourceArea.setText(transfer.getSourcePath());
            sourceArea.setCaretPosition(0);
            targetArea.setText(transfer.getTargetPath());
            targetArea.setCaretPosition(0);

            sourceStepCombo.setSelectedItem(transfer.getSourceStep());
            sourcePropertyCombo.setSelectedItem(transfer.getSourceProperty());
            sourceTransferLanguageCombo.setSelectedItem(transfer.getSourcePathLanguage());

            targetStepCombo.setSelectedItem(transfer.getTargetStep());
            targetPropertyCombo.setSelectedItem(transfer.getTargetProperty());
            targetTransferLanguageCombo.setSelectedItem(transfer.getTargetPathLanguage());

            failTransferCheckBox.setSelected(transfer.getFailOnError());
            setNullCheckBox.setSelected(transfer.getSetNullOnMissingSource());
            transferTextContentCheckBox.setSelected(transfer.getTransferTextContent());
            ignoreEmptyCheckBox.setSelected(transfer.getIgnoreEmpty());
            transferAllCheckBox.setSelected(transfer.getTransferToAll());
            entitizeCheckBox.setSelected(transfer.getEntitize());
            transferChildNodesCheckBox.setSelected(transfer.getTransferChildNodes());

            disableButton.setSelected(transfer.isDisabled());
        }

        copyButton.setEnabled(transfer != null);
        renameButton.setEnabled(transfer != null);
        deleteButton.setEnabled(transfer != null);
        disableButton.setEnabled(transfer != null);
        declareButton.setEnabled(transfer != null);
        sourceStepCombo.setEnabled(transfer != null);
        targetStepCombo.setEnabled(transfer != null);
        sourceArea.setEnabled(transfer != null);
        targetArea.setEnabled(transfer != null);
        failTransferCheckBox.setEnabled(transfer != null);
        setNullCheckBox.setEnabled(transfer != null);
        transferTextContentCheckBox.setEnabled(transfer != null);
        ignoreEmptyCheckBox.setEnabled(transfer != null);
        transferAllCheckBox.setEnabled(transfer != null);
        entitizeCheckBox.setEnabled(transfer != null);
        transferChildNodesCheckBox.setEnabled(transfer != null);

        runAllButton.setEnabled(transferList.getModel().getSize() > 0);
        runButton.setEnabled(transfer != null);

        sourcePropertyCombo.setEnabled(transfer != null);
        targetPropertyCombo.setEnabled(transfer != null);
    }

    /**
     * Listen to property changes and update UI objects. These may have been
     * triggered by UI so first check for actual difference so we dont end up in
     * loop.
     */

    private class TransferPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Object newValue = evt.getNewValue();

            if (evt.getPropertyName().equals(PropertyTransfer.SOURCE_PATH_PROPERTY)) {
                if (!sourceArea.getText().equals(newValue)) {
                    sourceArea.setText((String) newValue);
                }
            } else if (evt.getPropertyName().equals(PropertyTransfer.TARGET_PATH_PROPERTY)) {
                if (!targetArea.getText().equals(newValue)) {
                    targetArea.setText((String) newValue);
                }
            } else if (evt.getPropertyName().equals(PropertyTransfer.SOURCE_STEP_PROPERTY)) {
                Object selectedItem = sourceStepCombo.getSelectedItem();
                if (newValue == null || selectedItem == null || !selectedItem.equals(newValue)) {
                    selecting = true;
                    sourceStepCombo.setSelectedItem(newValue);
                    selecting = false;
                }
            } else if (evt.getPropertyName().equals(PropertyTransfer.TARGET_STEP_PROPERTY)) {
                Object selectedItem = targetStepCombo.getSelectedItem();
                if (newValue == null || selectedItem == null || !selectedItem.equals(newValue)) {
                    selecting = true;
                    targetStepCombo.setSelectedItem(newValue);
                    selecting = false;
                }
            } else if (evt.getPropertyName().equals(PropertyTransfer.SOURCE_TYPE_PROPERTY)) {
                Object selectedItem = sourcePropertyCombo.getSelectedItem();
                if (selectedItem == null || !selectedItem.equals(newValue)) {
                    sourcePropertyCombo.setSelectedItem(newValue);
                }
            } else if (evt.getPropertyName().equals(PropertyTransfer.TARGET_TYPE_PROPERTY)) {
                Object selectedItem = targetPropertyCombo.getSelectedItem();
                if (selectedItem == null || !selectedItem.equals(newValue)) {
                    targetPropertyCombo.setSelectedItem(newValue);
                }
            }
        }
    }

    private final class AddAction extends AbstractAction {
        public AddAction() {
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Property Transfer");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
        }

        public void actionPerformed(ActionEvent e) {

            Analytics.trackAction(SoapUIActions.ADD_PROPERTY_TRASNFER_IN_PROPERTY_TRANSFER_TEST_STEP.getActionName());

            String name = UISupport.prompt("Specify name for value transfer", "Add Transfer", "");
            if (name == null || name.trim().length() == 0) {
                return;
            }

            transferStep.addTransfer(name);
            transferList.setSelectedIndex(listModel.getSize() - 1);
        }
    }

    private final class CopyAction extends AbstractAction {
        public CopyAction() {
            putValue(Action.SHORT_DESCRIPTION, "Copies the selected Property Transfer");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clone.png"));
        }

        public void actionPerformed(ActionEvent e) {
            int ix = transferList.getSelectedIndex();
            PropertyTransfer originalTransfer = transferStep.getTransferAt(ix);

            String name = UISupport.prompt("Specify name for value transfer", "Copy Transfer", originalTransfer.getName());
            if (name == null || name.trim().length() == 0) {
                return;
            }

            PropertyTransfer transfer = transferStep.addTransfer(name);
            transfer.setSourceStepName(originalTransfer.getSourceStepName());
            transfer.setSourcePropertyName(originalTransfer.getSourcePropertyName());
            transfer.setSourcePath(originalTransfer.getSourcePath());
            transfer.setSourcePathLanguage(originalTransfer.getSourcePathLanguage());
            transfer.setTargetStepName(originalTransfer.getTargetStepName());
            transfer.setTargetPropertyName(originalTransfer.getTargetPropertyName());
            transfer.setTargetPath(originalTransfer.getTargetPath());
            transfer.setTargetPathLanguage(originalTransfer.getTargetPathLanguage());
            transfer.setDisabled(originalTransfer.isDisabled());
            transfer.setEntitize(originalTransfer.getEntitize());
            transfer.setFailOnError(originalTransfer.getFailOnError());
            transfer.setIgnoreEmpty(originalTransfer.getIgnoreEmpty());
            transfer.setSetNullOnMissingSource(originalTransfer.getSetNullOnMissingSource());
            transfer.setTransferChildNodes(originalTransfer.getTransferChildNodes());
            transfer.setTransferTextContent(originalTransfer.getTransferTextContent());
            transfer.setTransferToAll(originalTransfer.getTransferToAll());
            transfer.setUseXQuery(originalTransfer.getUseXQuery());

            transferList.setSelectedIndex(listModel.getSize() - 1);
        }


    }

    private final class DeleteAction extends AbstractAction {
        public DeleteAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Deletes the selected Property Transfer");
        }

        public void actionPerformed(ActionEvent e) {
            if (UISupport.confirm("Delete selected transfer", "Delete Transfer")) {
                transferList.setSelectedIndex(-1);

                int ix = transferList.getSelectedIndex();
                transferStep.removeTransferAt(ix);

                if (listModel.getSize() > 0) {
                    transferList.setSelectedIndex(ix > listModel.getSize() - 1 ? listModel.getSize() - 1 : ix);
                }
            }
        }
    }

    private final class ClearLogAction extends AbstractAction {
        public ClearLogAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clear.png"));
            putValue(Action.SHORT_DESCRIPTION, "Clears the property-transfer log");
        }

        public void actionPerformed(ActionEvent e) {
            transferLogTableModel.clear();
        }
    }

    private final class RenameAction extends AbstractAction {
        public RenameAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/rename.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Renames the selected Property Transfer");
        }

        public void actionPerformed(ActionEvent e) {
            PropertyTransfer transfer = getCurrentTransfer();

            String newName = UISupport.prompt("Specify new name for transfer", "Rename Transfer", transfer.getName());

            if (newName != null && !transfer.getName().equals(newName)) {
                listModel.setElementAt(newName, transferList.getSelectedIndex());
                transfer.setName(newName);
            }
        }
    }

    private final class DisableAction extends AbstractAction {
        public DisableAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/bullet_green.png"));
            putValue(Action.SHORT_DESCRIPTION, "Disables the selected Property Transfer");
        }

        public void actionPerformed(ActionEvent e) {
            PropertyTransfer transfer = getCurrentTransfer();
            transfer.setDisabled(disableButton.isSelected());

            String name = transfer.getName();
            if (transfer.isDisabled()) {
                name += " (disabled)";
            }

            listModel.setElementAt(name, transferList.getSelectedIndex());
        }
    }

    private final class DeclareNamespacesAction extends AbstractAction {
        public DeclareNamespacesAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/declareNs.gif"));
            putValue(Action.SHORT_DESCRIPTION,
                    "Declare available response/request namespaces in source/target expressions");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                TestPropertyHolder previousStep = getCurrentTransfer().getSourceStep();

                if (previousStep instanceof WsdlTestRequestStep) {
                    WsdlTestRequest testRequest = ((WsdlTestRequestStep) previousStep).getTestRequest();
                    sourceArea.setText(XmlUtils.declareXPathNamespaces(testRequest.getOperation().getInterface())
                            + sourceArea.getText());
                } else {
                    UISupport.showErrorMessage("Property Source is not a Request");
                }

                TestPropertyHolder nextStep = getCurrentTransfer().getTargetStep();

                if (nextStep instanceof WsdlTestRequestStep) {
                    WsdlTestRequest testRequest = ((WsdlTestRequestStep) nextStep).getTestRequest();
                    targetArea.setText(XmlUtils.declareXPathNamespaces(testRequest.getOperation().getInterface())
                            + targetArea.getText());
                } else {
                    UISupport.showErrorMessage("Property Target is not a Request");
                }
            } catch (Exception e1) {
                UISupport.showErrorMessage(e1);
            }
        }
    }

    private final class RunAllAction extends AbstractAction {
        public RunAllAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run_all.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs all Property Transfers");
        }

        public void actionPerformed(ActionEvent e) {
            if (listModel.getSize() == 0) {
                UISupport.showErrorMessage("Missing transfers!");
                return;
            }

            MockTestRunner mockRunner = new MockTestRunner(transferStep.getTestCase());
            MockTestRunContext context = new MockTestRunContext(mockRunner, transferStep);

            for (int c = 0; c < transferStep.getTransferCount(); c++) {
                PropertyTransfer transfer = transferStep.getTransferAt(c);
                PropertyTransfersTestStep.PropertyTransferResult result = (PropertyTransfersTestStep.PropertyTransferResult) transferStep
                        .run(mockRunner, context, transfer);
                transferLogTableModel.addResult(result);
            }
        }
    }

    private final class RunAction extends AbstractAction {
        public RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs selected PropertyTransfer");
        }

        public void actionPerformed(ActionEvent e) {
            if (transferList.getSelectedIndex() == -1) {
                UISupport.showErrorMessage("No transfer selectd!");
                return;
            }

            Analytics.trackAction(SoapUIActions.RUN_TEST_STEP.getActionName(), "RequestType", "PropertyTransfer");

            MockTestRunner mockRunner = new MockTestRunner(transferStep.getTestCase());
            MockTestRunContext context = new MockTestRunContext(mockRunner, transferStep);
            PropertyTransferResult result = (PropertyTransferResult) transferStep.run(mockRunner, context,
                    getCurrentTransfer());
            transferLogTableModel.addResult(result);
        }
    }

    public boolean onClose(boolean canCancel) {
        transferStep.getTestCase().getTestSuite().removeTestSuiteListener(testSuiteListener);
        transferStep.getTestCase().removeTestRunListener(testRunListener);

        PropertyTransfer transfer = getCurrentTransfer();

        if (transfer != null) {
            transfer.removePropertyChangeListener(transferPropertyChangeListener);
        }

        TestPropertyHolder item = (TestPropertyHolder) sourceStepCombo.getSelectedItem();
        if (item != null) {
            item.removeTestPropertyListener(sourceStepPropertiesListener);
        }

        item = (TestPropertyHolder) targetStepCombo.getSelectedItem();
        if (item != null) {
            item.removeTestPropertyListener(targetStepPropertiesListener);
        }

        if (transferListListener != null) {
            transferStep.removePropertyChangeListener(transferListListener);
        }

        componentEnabler.release();
        inspectorPanel.release();

        return release();
    }

    public JComponent getComponent() {
        return this;
    }

    protected JTextArea getSourceArea() {
        return sourceArea;
    }

    protected JTextArea getTargetArea() {
        return targetArea;
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == transferStep || modelItem == transferStep.getTestCase()
                || modelItem == transferStep.getTestCase().getTestSuite()
                || modelItem == transferStep.getTestCase().getTestSuite().getProject();
    }

    public boolean selectTransfer(PropertyTransfer transfer) {
        for (int c = 0; c < transferStep.getTransferCount(); c++) {
            if (transferStep.getTransferAt(c) == transfer) {
                transferList.setSelectedIndex(c);
                return true;
            }
        }

        return false;
    }

    private class TransfersTableModel extends AbstractTableModel {
        private List<PropertyTransfersTestStep.PropertyTransferResult> results = new ArrayList<PropertyTransfersTestStep.PropertyTransferResult>();

        public synchronized int getRowCount() {
            int sum = 0;
            for (PropertyTransfersTestStep.PropertyTransferResult result : results) {
                sum += result.getTransferCount();
            }

            return sum;
        }

        public synchronized void clear() {
            results.clear();
            fireTableDataChanged();
            logInspector.setTitle("Transfer Log (0)");
        }

        public void addResult(PropertyTransfersTestStep.PropertyTransferResult result) {
            int rowCount;
            synchronized (this) {
                rowCount = getRowCount();
                results.add(result);
            }

            fireTableRowsInserted(rowCount, rowCount + result.getTransferCount());

            logInspector.setTitle("Transfer Log (" + getRowCount() + ")");
            inspectorPanel.activate(logInspector);
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Timestamp";
                case 1:
                    return "Transfer Name";
                case 2:
                    return "Transferred Values";
            }

            return null;
        }

        public synchronized Object getValueAt(int rowIndex, int columnIndex) {
            // find correct transfer
            PropertyTransfersTestStep.PropertyTransferResult result = null;
            int sum = 0;

            for (int c = 0; c < results.size(); c++) {
                if (sum + results.get(c).getTransferCount() > rowIndex) {
                    result = results.get(c);
                    break;
                } else {
                    sum += results.get(c).getTransferCount();
                }
            }

            if (result != null) {
                switch (columnIndex) {
                    case 0:
                        return new Date(result.getTimeStamp()).toString();
                    case 1:
                        return result.getTransferAt(rowIndex - sum).getName();
                    case 2:
                        return Arrays.toString(result.getTransferredValuesAt(rowIndex - sum));
                }
            }

            return null;
        }

    }

    private class InternalTestRunListener extends TestRunListenerAdapter {
        @Override
        public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
            if (result.getTestStep() == transferStep) {
                transferLogTableModel.addResult((PropertyTransferResult) result);
            }
        }
    }

    private class StepComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof TestModelItem) {
                TestModelItem item = (TestModelItem) value;
                setIcon(item.getIcon());
                setText(item.getName());
            } else if (value == PropertyExpansionUtils.getGlobalProperties()) {
                setText("Global");
            }

            setToolTipText(getText());

            return result;
        }
    }

    private class PropertyComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null) {
                TestProperty item = (TestProperty) value;
                setText(item.getName());
            }

            setToolTipText(getText());

            return result;
        }
    }

    private class ListUpdater implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            listModel = createListModel();
            transferList.setModel(listModel);
        }
    }
}
