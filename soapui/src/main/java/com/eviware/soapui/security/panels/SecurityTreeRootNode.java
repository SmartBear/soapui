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

package com.eviware.soapui.security.panels;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;

import javax.swing.tree.DefaultMutableTreeNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("serial")
public class SecurityTreeRootNode extends DefaultMutableTreeNode implements PropertyChangeListener {

    private SecurityTest securityTest;

    public SecurityTreeRootNode(SecurityTest securityTest) {
        this.securityTest = securityTest;

        securityTest.addPropertyChangeListener(this);

        initRoot();
    }

    private void initRoot() {
        parent = null;
        initChildren();
        allowsChildren = true;
    }

    private void initChildren() {
        for (TestStep step : securityTest.getTestCase().getTestStepList()) {
            add(new TestStepNode(this, step, securityTest.getSecurityScansMap().get(step.getId())));
        }
    }

    @Override
    public String toString() {
        return securityTest.toString();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // System.out.println(evt.toString());
    }

    public SecurityTest getSecurityTest() {
        return securityTest;
    }

    public void add(TestStep testStep) {
        new TestStepNode(this, testStep, securityTest.getSecurityScansMap().get(testStep.getId()));
    }

    public void release() {
        if (securityTest != null) {
            securityTest.removePropertyChangeListener(this);
        }
    }
}
