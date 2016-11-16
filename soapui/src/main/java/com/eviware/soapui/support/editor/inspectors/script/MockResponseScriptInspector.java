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

package com.eviware.soapui.support.editor.inspectors.script;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

public class MockResponseScriptInspector extends AbstractXmlInspector {
    private final MockResponse mockResponse;
    private GroovyEditor responseScriptEditor;
    private RunScriptAction runScriptAction = new RunScriptAction();
    private JPanel panel;

    protected MockResponseScriptInspector(MockResponse mockResponse) {
        super("Script", "Script for this MockResponse", true, ScriptInspectorFactory.INSPECTOR_ID);
        this.mockResponse = mockResponse;
    }

    public JComponent getComponent() {
        if (panel == null) {
            buildResponseScriptEditor();
        }

        return panel;
    }

    @Override
    public void activate() {
        responseScriptEditor.requestFocusInWindow();
    }

    protected void buildResponseScriptEditor() {
        responseScriptEditor = new GroovyEditor(new MockResponseGroovyEditorModel());

        panel = new JPanel(new BorderLayout());
        panel.add(buildScriptToolbar(), BorderLayout.NORTH);
        panel.add(responseScriptEditor, BorderLayout.CENTER);
    }

    private JComponent buildScriptToolbar() {
        JXToolBar toolBar = UISupport.createToolbar();
        JButton runButton = UISupport.createToolbarButton(runScriptAction);
        toolBar.add(runButton);
        toolBar.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("<html>Script is invoked with <code>log</code>, <code>context</code>, "
                + "<code>requestContext</code>, <code>mockRequest</code> and <code>mockResponse</code> variables</html>");
        label.setToolTipText(label.getText());
        label.setMaximumSize(label.getPreferredSize());

        toolBar.add(label);
        toolBar.addUnrelatedGap();
        toolBar.addFixed(UISupport.createActionButton(
                new ShowOnlineHelpAction(mockResponse.getScriptHelpUrl()), true));

        return toolBar;
    }

    @Override
    public void release() {
        super.release();

        responseScriptEditor.release();
    }

    private class RunScriptAction extends AbstractAction {
        public RunScriptAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Runs this script using mock httpRequest/httpResponse objects");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                MockResult mockResult = mockResponse.getMockResult();
                mockResponse.evaluateScript(mockResult == null ? null : mockResult.getMockRequest());

                StringToStringMap values = null;
                if (mockResponse.getMockResult() != null) {
                    values = mockResponse.getMockResult().getMockRequest().getContext().toStringToStringMap();
                }

                if (values == null || values.isEmpty()) {
                    UISupport.showInfoMessage("No values were returned");
                } else {
                    String msg = "<html><body>Returned values:<br>";

                    for (String name : values.keySet()) {
                        msg += XmlUtils.entitize(name) + " : " + XmlUtils.entitize(values.get(name)) + "<br>";
                    }

                    msg += "</body></html>";

                    UISupport.showExtendedInfo("Result", "Result of MockResponse Script", msg, new Dimension(500, 400));
                }
            } catch (Throwable e1) {
                responseScriptEditor.selectError(e1.getMessage());
                UISupport.showErrorMessage(e1.toString());
            }
        }
    }

    private class MockResponseGroovyEditorModel implements GroovyEditorModel {
        public String[] getKeywords() {
            return new String[]{"context", "mockRequest", "mockResponse", "log", "requestContext"};
        }

        public Action getRunAction() {
            return runScriptAction;
        }

        public String getScript() {
            return mockResponse.getScript();
        }

        public void setScript(String text) {
            mockResponse.setScript(text);
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
            return mockResponse;
        }
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return true;
    }
}
