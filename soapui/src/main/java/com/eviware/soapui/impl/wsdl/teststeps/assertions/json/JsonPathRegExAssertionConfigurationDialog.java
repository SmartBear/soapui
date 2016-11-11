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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.json;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.AssertionConfigurationDialog;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextArea;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

public class JsonPathRegExAssertionConfigurationDialog extends AssertionConfigurationDialog {
    private JTextArea regExArea;
    private JsonPathRegExAssertion jsonPathRegExAssertion;

    public JsonPathRegExAssertionConfigurationDialog(JsonPathRegExAssertion assertion) {
        super(assertion);
        this.jsonPathRegExAssertion = assertion;
    }

    public String getHelpURL() {
        return HelpUrls.ASSERTION_JSON_REGEX_CONFIG;
    }

    @Override
    protected JPanel getPathAreaPanel() {
        JPanel pathAreaPanel = super.getPathAreaPanel();
        regExArea = new JUndoableTextArea();
        regExArea.setRows(2);
        JScrollPane jScrollPane = UISupport.addTitledBorder(new JScrollPane(regExArea), "Regular Expression");
        pathAreaPanel.add(jScrollPane, BorderLayout.SOUTH);
        return pathAreaPanel;
    }

    @Override
    protected AbstractAction createOkAction() {
        return new OkAction();
    }

    @Override
    protected TestPathAction createTestPathAction() {
        return new TestPathAction();
    }

    @Override
    protected SelectFromCurrentAction createSelectFromCurrentAction() {
        return new SelectFromCurrentAction();
    }

    @Override
    protected void initializeFieldsWithValuesFromAssertion() {
        super.initializeFieldsWithValuesFromAssertion();
        regExArea.setText(jsonPathRegExAssertion.getRegularExpression());
    }

    public class OkAction extends AssertionConfigurationDialog.OkAction {
        public void actionPerformed(ActionEvent arg0) {
            setRegExToAssertion();
            super.actionPerformed(arg0);
        }
    }

    public class TestPathAction extends AssertionConfigurationDialog.TestPathAction {
        public void actionPerformed(ActionEvent event) {
            String oldRegEx = jsonPathRegExAssertion.getRegularExpression();
            setRegExToAssertion();
            super.actionPerformed(event);
            jsonPathRegExAssertion.setRegularExpression(oldRegEx);
        }
    }

    public class SelectFromCurrentAction extends AssertionConfigurationDialog.SelectFromCurrentAction {
        public void actionPerformed(ActionEvent event) {
            String oldRegEx = jsonPathRegExAssertion.getRegularExpression();
            setRegExToAssertion();
            super.actionPerformed(event);
            jsonPathRegExAssertion.setRegularExpression(oldRegEx);
        }
    }

    private void setRegExToAssertion() {
        jsonPathRegExAssertion.setRegularExpression(regExArea.getText());
    }
}
