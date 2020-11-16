/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eviware.soapui.impl.wsdl.teststeps.Script.SCRIPT_PROPERTY;

/**
 * DesktopPanel for WsdlGroovyTestSteps
 *
 * @author Ole.Matzura
 */

public class GroovyScriptStepDesktopPanel extends ModelItemDesktopPanel<WsdlGroovyScriptTestStep> implements
        PropertyChangeListener {
    private final WsdlGroovyScriptTestStep groovyStep;
    private GroovyEditor editor;
    private JLogList logArea;
    private Logger logger;
    private TestRunComponentEnabler componentEnabler;
    private RunAction runAction = new RunAction();
    private JEditorStatusBarWithProgress statusBar;
    private SettingsListener settingsListener;
    private JComponentInspector<JComponent> logInspector;
    public boolean updating;
    private JInspectorPanel inspectorPanel;

    public GroovyScriptStepDesktopPanel(WsdlGroovyScriptTestStep groovyStep) {
        super(groovyStep);
        this.groovyStep = groovyStep;
        componentEnabler = new TestRunComponentEnabler(groovyStep.getTestCase());

        buildUI();
        setPreferredSize(new Dimension(600, 440));

        logger = LogManager.getLogger(groovyStep.getName() + "#" + hashCode());

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                editor.requestFocusInWindow();
            }
        });

        groovyStep.addPropertyChangeListener(this);
    }

    protected GroovyEditor getEditor() {
        return editor;
    }

    private void buildUI() {
        editor = new GroovyEditor(new ScriptStepGroovyEditorModel());

        logArea = new JLogList("Groovy Test Log");
        logArea.addLogger(groovyStep.getName() + "#" + hashCode(), true);
        logArea.getLogList().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) {
                    return;
                }

                String value = logArea.getLogList().getSelectedValue().toString();
                if (value == null) {
                    return;
                }

                editor.selectError(value);
            }
        });

        logArea.getLogList().getModel().addListDataListener(new ListDataChangeListener() {

            @Override
            public void dataChanged(ListModel model) {
                logInspector.setTitle("Log Output (" + model.getSize() + ")");

            }
        });

        inspectorPanel = JInspectorPanelFactory.build(editor);
        logInspector = inspectorPanel.addInspector(new JComponentInspector<JComponent>(logArea, "Log Output (0)",
                "Groovy Log output for this script", true));
        inspectorPanel.setDefaultDividerLocation(0.8F);
        inspectorPanel.activate(logInspector);
        add(inspectorPanel.getComponent(), BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildStatusBar(), BorderLayout.SOUTH);

        componentEnabler.add(editor);
    }

    private Component buildStatusBar() {
        statusBar = new JEditorStatusBarWithProgress(editor);
        return statusBar;
    }

    private JComponent buildToolbar() {
        JXToolBar toolBar = UISupport.createToolbar();
        JButton runButton = UISupport.createToolbarButton(runAction);
        toolBar.add(runButton);
        toolBar.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("<html>Script is invoked with <code>log</code>, <code>context</code> "
                + "and <code>testRunner</code> variables</html>");
        label.setToolTipText(label.getText());
        label.setMaximumSize(label.getPreferredSize());

        toolBar.add(label);
        toolBar.addRelatedGap();
        toolBar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(HelpUrls.GROOVYSTEPEDITOR_HELP_URL)));

        componentEnabler.add(runButton);

        return toolBar;
    }

    public boolean onClose(boolean canCancel) {
        componentEnabler.release();
        editor.release();
        SoapUI.getSettings().removeSettingsListener(settingsListener);
        logArea.removeLogger(logger.getName());
        logger = null;
        inspectorPanel.release();

        getModelItem().removePropertyChangeListener(this);

        return super.release();
    }

    public JComponent getComponent() {
        return this;
    }

    public boolean dependsOn(ModelItem modelItem) {
        return modelItem == groovyStep || modelItem == groovyStep.getTestCase()
                || modelItem == groovyStep.getTestCase().getTestSuite()
                || modelItem == groovyStep.getTestCase().getTestSuite().getProject();
    }

    private class ScriptStepGroovyEditorModel implements GroovyEditorModel {
        public String[] getKeywords() {
            return new String[]{"log", "context", "testRunner"};
        }

        public Action getRunAction() {
            return runAction;
        }

        public String getScript() {
            return groovyStep.getScript();
        }

        public void setScript(String text) {
            if (updating) {
                return;
            }

            updating = true;
            groovyStep.setScript(text);
            updating = false;
        }

        public Settings getSettings() {
            return SoapUI.getSettings();
        }

        public String getScriptName() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public ModelItem getModelItem() {
            return groovyStep;
        }
    }

    private class RunAction extends AbstractAction {
        public RunAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION,
                    "Runs this script in a seperate thread using a mock testRunner and testContext");
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getThreadPool().execute(new Runnable() {
                public void run() {
                    MockTestRunner mockTestRunner = new MockTestRunner(groovyStep.getTestCase(), logger);
                    statusBar.setIndeterminate(true);
                    WsdlTestStepResult result = (WsdlTestStepResult) groovyStep.run(mockTestRunner,
                            new MockTestRunContext(mockTestRunner, groovyStep));
                    statusBar.setIndeterminate(false);

                    Throwable er = result.getError();
                    if (er != null) {
                        String message = er.getMessage();

                        // ugly...
                        editor.selectError(message);

                        UISupport.showErrorMessage(StringUtils.join(result.getMessages(), "\n"));
                        editor.requestFocus();
                    } else if (result.getMessages().length > 0) {
                        UISupport.showInfoMessage(StringUtils.join(result.getMessages(), "\n"));
                    }
                }
            });
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SCRIPT_PROPERTY) && !updating) {
            updating = true;
            editor.getEditArea().setText((String) evt.getNewValue());
            updating = false;
        }

        super.propertyChange(evt);
    }
}
