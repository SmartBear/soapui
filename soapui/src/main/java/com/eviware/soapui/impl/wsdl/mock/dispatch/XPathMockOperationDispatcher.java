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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

public class XPathMockOperationDispatcher extends AbstractMockOperationDispatcher {
    private GroovyEditor xpathEditor;

    public XPathMockOperationDispatcher(MockOperation mockOperation) {
        super(mockOperation);
    }

    public MockResponse selectMockResponse(MockRequest request, MockResult result)
            throws DispatchException {
        XmlObject xmlObject;
        try {
            xmlObject = request.getRequestXmlObject();
        } catch (XmlException e) {
            throw new DispatchException("Error getting XmlObject for request: " + e);
        }

        String path = getMockOperation().getScript();
        if (StringUtils.isNullOrEmpty(path)) {
            throw new DispatchException("Missing dispatch XPath expression");
        }

        String[] values = XmlUtils.selectNodeValues(xmlObject, path);
        for (String value : values) {
            MockResponse mockResponse = getMockOperation().getMockResponseByName(value);
            if (mockResponse != null) {
                return mockResponse;
            }
        }

        return null;
    }

    @Override
    public JComponent getEditorComponent() {
        JPanel xpathEditorPanel = new JPanel(new BorderLayout());
        DispatchXPathGroovyEditorModel editorModel = new DispatchXPathGroovyEditorModel();
        xpathEditor = new GroovyEditor(editorModel);
        xpathEditorPanel.add(xpathEditor, BorderLayout.CENTER);
        xpathEditorPanel.add(buildXPathEditorToolbar(editorModel), BorderLayout.PAGE_START);

        return xpathEditorPanel;
    }

    public GroovyEditor getXPathEditor() {
        return xpathEditor;
    }

    @Override
    public void release() {
        releaseEditorComponent();
        super.release();
    }

    @Override
    public void releaseEditorComponent() {
        if (xpathEditor != null) {
            xpathEditor.release();
        }

        super.releaseEditorComponent();
    }

    @Override
    public boolean hasDefaultResponse() {
        return true;
    }

    protected JXToolBar buildXPathEditorToolbar(DispatchXPathGroovyEditorModel editorModel) {
        JXToolBar toolbar = UISupport.createToolbar();
        toolbar.addSpace(3);
        addToolbarActions(editorModel, toolbar);
        toolbar.addGlue();
        toolbar.addFixed(ModelItemDesktopPanel.createActionButton(new ShowOnlineHelpAction(
                HelpUrls.MOCKOPERATION_XPATHDISPATCH_HELP_URL), true));
        return toolbar;
    }

    protected void addToolbarActions(DispatchXPathGroovyEditorModel editorModel, JXToolBar toolbar) {
        toolbar.addFixed(UISupport.createToolbarButton(editorModel.getRunAction()));
    }

    public static class Factory implements MockOperationDispatchFactory {
        public MockOperationDispatcher build(MockOperation mockOperation) {
            return new XPathMockOperationDispatcher(mockOperation);
        }
    }

    public class DispatchXPathGroovyEditorModel implements GroovyEditorModel {
        private RunXPathAction runXPathAction = new RunXPathAction();

        public String[] getKeywords() {
            return new String[]{"define", "namespace"};
        }

        public Action getRunAction() {
            return runXPathAction;
        }

        public String getScript() {
            return getMockOperation().getScript();
        }

        public Settings getSettings() {
            return getMockOperation().getSettings();
        }

        public void setScript(String text) {
            getMockOperation().setScript(text);
        }

        public String getScriptName() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public ModelItem getModelItem() {
            return getMockOperation();
        }
    }

    private class RunXPathAction extends AbstractAction {
        public RunXPathAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/run.png"));
            putValue(Action.SHORT_DESCRIPTION, "Evaluates this xpath expression against the latest request");
        }

        public void actionPerformed(ActionEvent e) {
            MockResult lastMockResult = getMockOperation().getLastMockResult();
            if (lastMockResult == null) {
                UISupport.showErrorMessage("Missing last request to select from");
                return;
            }

            try {
                MockResponse retVal = selectMockResponse(lastMockResult.getMockRequest(), null);
                UISupport.showInfoMessage("XPath Selection returned [" + (retVal == null ? "null" : retVal.getName())
                        + "]");
            } catch (Exception e1) {
                SoapUI.logError(e1);
            }
        }
    }
}
