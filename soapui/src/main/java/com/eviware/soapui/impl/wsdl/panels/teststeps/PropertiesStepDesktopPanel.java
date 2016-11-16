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

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * DesktopPanel for WsdlPropertiesTestSteps
 *
 * @author Ole.Matzura
 */

public class PropertiesStepDesktopPanel extends ModelItemDesktopPanel<WsdlPropertiesTestStep> implements
        PropertyChangeListener {
    private final WsdlPropertiesTestStep testStep;
    private JTextField sourceField;
    private JTextField targetField;
    private PropertyHolderTable propertiesTable;
    private TestRunComponentEnabler componentEnabler;
    protected boolean updatingSource;
    protected boolean updatingTarget;

    public PropertiesStepDesktopPanel(WsdlPropertiesTestStep testStep) {
        super(testStep);
        this.testStep = testStep;
        componentEnabler = new TestRunComponentEnabler(testStep.getTestCase());
        buildUI();

        testStep.addPropertyChangeListener(this);
    }

    private void buildUI() {
        propertiesTable = createPropertyHolderTable();
        add(propertiesTable, BorderLayout.CENTER);

        JXToolBar toolbar = propertiesTable.getToolbar();

        toolbar.addRelatedGap();
        JButton reloadButton = UISupport.createToolbarButton(new ReloadPropertiesFromSourceAction());
        toolbar.add(reloadButton);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Load from:"));
        sourceField = new JTextField(testStep.getSource(), 20) {
            @Override
            public String getToolTipText(MouseEvent event) {
                return testStep.getSource(true);
            }
        };
        sourceField.setToolTipText("The filename/url or referring system-property to load properties from");
        sourceField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            public void update(Document document) {
                if (updatingSource) {
                    return;
                }

                updatingSource = true;
                testStep.setSource(sourceField.getText());
                updatingSource = false;
            }
        });

        toolbar.addRelatedGap();
        toolbar.addFixed(sourceField);
        JButton setSourceButton = UISupport.createToolbarButton(new SetPropertiesSourceAction());
        toolbar.addRelatedGap();
        toolbar.add(setSourceButton);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Save to:"));
        targetField = new JTextField(testStep.getTarget(), 20) {
            @Override
            public String getToolTipText(MouseEvent event) {
                return testStep.getTarget(true);
            }
        };

        targetField.setToolTipText("The filename/url or referring system-property to save properties to");
        targetField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            public void update(Document document) {
                if (updatingTarget) {
                    return;
                }

                updatingTarget = true;
                testStep.setTarget(targetField.getText());
                updatingTarget = false;
            }
        });

        toolbar.addRelatedGap();
        toolbar.addFixed(targetField);
        JButton setTargetButton = UISupport.createToolbarButton(new SetPropertiesTargetAction());
        toolbar.addRelatedGap();
        toolbar.add(setTargetButton);

        toolbar.add(Box.createHorizontalGlue());
        toolbar.addSeparator();
        toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.PROPERTIESSTEPEDITOR_HELP_URL)));

        componentEnabler.add(sourceField);
        componentEnabler.add(targetField);
        componentEnabler.add(setTargetButton);
        componentEnabler.add(setSourceButton);
        componentEnabler.add(propertiesTable);

        setPreferredSize(new Dimension(600, 400));
    }

    protected PropertyHolderTable createPropertyHolderTable() {
        return new PropertyHolderTable(getModelItem());
    }

    public boolean onClose(boolean canCancel) {
        componentEnabler.release();
        propertiesTable.release();
        return release();
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == testStep || modelItem == testStep.getTestCase()
                || modelItem == testStep.getTestCase().getTestSuite()
                || modelItem == testStep.getTestCase().getTestSuite().getProject();
    }

    private class SetPropertiesSourceAction extends AbstractAction {
        public SetPropertiesSourceAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/set_properties_source.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Selects the properties source file");
        }

        public void actionPerformed(ActionEvent e) {
            String root = ModelSupport.getResourceRoot(testStep);
            File file = UISupport.getFileDialogs().open(this, "Set properties source", "properties",
                    "Properties Files (*.properties)", root);
            if (file != null) {
                updatingSource = true;
                testStep.setSource(file.getAbsolutePath());
                sourceField.setText(testStep.getSource());
                updatingSource = false;
                try {
                    boolean createMissing = UISupport.confirm("Create missing properties?", "Set Properties Source");
                    int cnt = testStep.loadProperties(createMissing);
                    UISupport.showInfoMessage("Loaded " + cnt + " properties from [" + testStep.getSource() + "]");
                } catch (IOException e1) {
                    UISupport.showErrorMessage("Failed to load properties from [" + testStep.getSource() + "]; " + e1);
                }
            }
        }
    }

    private class ReloadPropertiesFromSourceAction extends AbstractAction {
        public ReloadPropertiesFromSourceAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/reload_properties.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Reloads the current properties from the selected file");
        }

        public void actionPerformed(ActionEvent e) {
            if (StringUtils.isNullOrEmpty(testStep.getSource())) {
                UISupport.showErrorMessage("Missing source-file to load from");
                return;
            }

            try {
                boolean createMissing = UISupport.confirm("Create missing properties?", "Reload Properties");
                int cnt = testStep.loadProperties(createMissing);
                UISupport.showInfoMessage("Loaded " + cnt + " properties from [" + testStep.getSource() + "]");
            } catch (Exception e1) {
                UISupport.showErrorMessage("Failed to load properties from [" + testStep.getSource() + "]; " + e1);
            }
        }
    }

    private class SetPropertiesTargetAction extends AbstractAction {
        public SetPropertiesTargetAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/set_properties_target.gif"));
            putValue(Action.SHORT_DESCRIPTION, "Selects the properties target file");
        }

        public void actionPerformed(ActionEvent e) {
            String root = ModelSupport.getResourceRoot(testStep);
            File file = UISupport.getFileDialogs().saveAs(this, "Set properties target", "properties",
                    "Properties Files (*.properties)", new File(root));
            if (file != null) {
                updatingTarget = true;
                testStep.setTarget(file.getAbsolutePath());
                targetField.setText(testStep.getTarget());
                updatingTarget = false;

                try {
                    int cnt = testStep.saveProperties();
                    UISupport.showInfoMessage("Saved " + cnt + " properties to [" + testStep.getTarget() + "]");
                } catch (IOException e1) {
                    UISupport.showErrorMessage("Failed to save properties to [" + testStep.getTarget() + "]; " + e1);
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!updatingSource && evt.getPropertyName().equals(WsdlPropertiesTestStep.SOURCE_PROPERTY)) {
            sourceField.setText(evt.getNewValue().toString());
        } else if (!updatingTarget && evt.getPropertyName().equals(WsdlPropertiesTestStep.TARGET_PROPERTY)) {
            targetField.setText(evt.getNewValue().toString());
        }

        super.propertyChange(evt);
    }

    @Override
    protected boolean release() {
        testStep.removePropertyChangeListener(this);
        return super.release();
    }

	/*
     * public class PropertiesTransferHandler extends
	 * AbstractPropertiesTransferHandler { public
	 * PropertiesTransferHandler(JComponent component) { super(component); }
	 * 
	 * protected StepProperty getSelectedProperty(JComponent c) { int rowIndex =
	 * propertiesTable.getSelectedRow(); if (rowIndex == -1) return null;
	 * 
	 * return testStep.getTestStepPropertyAt(rowIndex); } }
	 */
}
