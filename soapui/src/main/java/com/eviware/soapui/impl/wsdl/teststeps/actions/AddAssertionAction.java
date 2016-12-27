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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.panels.assertions.AddAssertionPanel;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;

/**
 * Adds a WsdlAssertion to a WsdlTestRequest
 *
 * @author Ole.Matzura
 */

public class AddAssertionAction extends AbstractAction {
    private final Assertable assertable;
    private AddAssertionPanel addAssertionPanel;

    public AddAssertionAction(Assertable assertable) {
        super("Add Assertion");
        this.assertable = assertable;

        putValue(Action.SHORT_DESCRIPTION, "Adds an assertion to this item");
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
    }

    public void actionPerformed(ActionEvent e) {
        String[] assertions = TestAssertionRegistry.getInstance().getAvailableAssertionNames(assertable);

        if (assertions == null || assertions.length == 0) {
            UISupport.showErrorMessage("No assertions available for this message");
            return;
        }

        addAssertionPanel = new AddAssertionPanel(assertable);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addAssertionPanel.setVisible(true);
            }
        });

    }

    public void release() {
        if (addAssertionPanel != null) {
            addAssertionPanel.release();
        }
    }
}
