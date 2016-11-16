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

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.testsuite.CloneTestSuiteAction;
import com.eviware.soapui.support.UISupport;

public class TestSuiteToProjectDropHandler extends AbstractAfterModelItemDropHandler<WsdlTestSuite, WsdlProject> {
    public TestSuiteToProjectDropHandler() {
        super(WsdlTestSuite.class, WsdlProject.class);
    }

    @Override
    boolean canCopyAfter(WsdlTestSuite source, WsdlProject target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestSuite source, WsdlProject target) {
        return source.getProject() != target;
    }

    @Override
    boolean copyAfter(WsdlTestSuite source, WsdlProject target) {
        String name = UISupport.prompt("Specify name for copied TestSuite", "Copy TestSuite",
                "Copy of " + source.getName());
        if (name == null) {
            return false;
        }

        if (source.getProject() == target) {
            return CloneTestSuiteAction.cloneTestSuiteWithinProject(source, name, target, source.getDescription()) != null;
        } else {
            return CloneTestSuiteAction.cloneToAnotherProject(source, target.getName(), name, false,
                    source.getDescription()) != null;
        }
    }

    @Override
    boolean moveAfter(WsdlTestSuite source, WsdlProject target) {
        String name = UISupport.prompt("Specify name for moved TestSuite", "Move TestSuite", source.getName());
        if (name == null) {
            return false;
        }

        WsdlTestSuite testSuite = CloneTestSuiteAction.cloneToAnotherProject(source, target.getName(), name, true,
                source.getDescription());
        if (testSuite != null) {
            source.getProject().removeTestSuite(source);
            return true;
        }

        return false;
    }

    @Override
    String getCopyAfterInfo(WsdlTestSuite source, WsdlProject target) {
        return source.getProject() == target ? "Copy TestSuite [" + source.getName() + "] within Project ["
                + target.getName() + "]" : "Copy TestSuite [" + source.getName() + "] to Project [" + target.getName()
                + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestSuite source, WsdlProject target) {
        return "Move TestSuite [" + source.getName() + "] to Project [" + target.getName() + "]";
    }

}
