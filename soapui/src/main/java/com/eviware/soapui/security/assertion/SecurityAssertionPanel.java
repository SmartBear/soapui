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

package com.eviware.soapui.security.assertion;

import com.eviware.soapui.impl.wsdl.panels.teststeps.AssertionsPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

public class SecurityAssertionPanel extends AssertionsPanel {
    public SecurityAssertionPanel(Assertable assertable) {
        super(assertable);
    }

    @Override
    protected void initListAndModel() {
        assertionListModel = new SecurityAssertionListModel();
        assertionList = new JList(assertionListModel);
        assertionList.setToolTipText("Assertions for this security scan.");
        assertionList.setCellRenderer(new SecurityAssertionCellRenderer());
    }

    protected class SecurityAssertionListModel extends AssertionListModel {
        protected void addAssertion(TestAssertion assertion) {
            assertion.addPropertyChangeListener(this);
            items.add(assertion);
        }
    }

    private class SecurityAssertionCellRenderer extends JLabel implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            setEnabled(list.isEnabled());

            TestAssertion assertion = (TestAssertion) value;
            setText(assertion.getLabel());

            if (assertion.isDisabled() && isEnabled()) {
                setEnabled(false);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());
            setOpaque(true);

            return this;
        }

    }

    public String getHelpUrl() {
        return HelpUrls.SECURITY_ASSERTION_HELP;
    }

}
