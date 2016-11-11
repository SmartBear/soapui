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

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

public class TestStepNode extends DefaultMutableTreeNode {

    private TestStep testStep;

    public TestStepNode(SecurityTreeRootNode securityTreeRootNode, TestStep step, List<SecurityScan> list) {
        this.testStep = step;
        if (step instanceof SamplerTestStep) {
            setAllowsChildren(true);
        } else {
            setAllowsChildren(false);
            children = null;
        }
        if (list != null) {
            for (SecurityScan sc : list) {
                add(new SecurityScanNode(sc));
            }
        }
    }

    @Override
    public String toString() {
        return testStep.toString();
    }

    public TestStep getTestStep() {
        return testStep;
    }

}
