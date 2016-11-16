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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;

import java.util.HashSet;
import java.util.Set;

public class TestSuiteToTestSuiteDropHandler extends
        AbstractBeforeAfterModelItemDropHandler<WsdlTestSuite, WsdlTestSuite> {
    public TestSuiteToTestSuiteDropHandler() {
        super(WsdlTestSuite.class, WsdlTestSuite.class);
    }

    @Override
    boolean canCopyAfter(WsdlTestSuite source, WsdlTestSuite target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestSuite source, WsdlTestSuite target) {
        return source != target;
    }

    @Override
    boolean copyAfter(WsdlTestSuite source, WsdlTestSuite target) {
        WsdlTestSuite testCase = copyTestSuite(source, target.getProject(),
                target.getProject().getIndexOfTestSuite(target) + 1);

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    boolean moveAfter(WsdlTestSuite source, WsdlTestSuite target) {
        WsdlTestSuite testCase = moveTestSuite(source, target.getProject(),
                target.getProject().getIndexOfTestSuite(target) + 1);

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    String getCopyAfterInfo(WsdlTestSuite source, WsdlTestSuite target) {
        return "Copy TestSuite [" + source.getName() + "] to Project [" + target.getProject().getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestSuite source, WsdlTestSuite target) {
        return source == target ? "Move TestCase [" + source.getName() + "] within TestSuite" : "Move TestCase ["
                + source.getName() + "] to TestSuite in Project [" + target.getName() + "]";
    }

    @Override
    boolean canCopyBefore(WsdlTestSuite source, WsdlTestSuite target) {
        return true;
    }

    @Override
    boolean canMoveBefore(WsdlTestSuite source, WsdlTestSuite target) {
        return source != target;
    }

    @Override
    boolean copyBefore(WsdlTestSuite source, WsdlTestSuite target) {
        WsdlTestSuite testCase = copyTestSuite(source, source.getProject(),
                target.getProject().getIndexOfTestSuite(target));

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    String getCopyBeforeInfo(WsdlTestSuite source, WsdlTestSuite target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getMoveBeforeInfo(WsdlTestSuite source, WsdlTestSuite target) {
        return getMoveAfterInfo(source, target);
    }

    @Override
    boolean moveBefore(WsdlTestSuite source, WsdlTestSuite target) {
        WsdlTestSuite testCase = moveTestSuite(source, target.getProject(),
                target.getProject().getIndexOfTestSuite(target));

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    public static WsdlTestSuite moveTestSuite(WsdlTestSuite testSuite, WsdlProject target, int position) {
        if (testSuite.getProject() == target) {
            int ix = target.getIndexOfTestSuite(testSuite);

            if (position == -1) {
                target.moveTestSuite(ix, target.getTestSuiteCount() - ix);
            } else if (ix >= 0 && position != ix) {
                int offset = position - ix;
                if (offset > 0) {
                    offset--;
                }
                target.moveTestSuite(ix, offset);
            }
        } else if (UISupport.confirm("Move TestSuite [" + testSuite.getName() + "] to Project [" + target.getName() + "]",
                "Move TestSuite")) {
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // get required interfaces
            for (TestCase testCase : testSuite.getTestCaseList()) {
                for (int y = 0; y < testCase.getTestStepCount(); y++) {
                    WsdlTestStep testStep = (WsdlTestStep) testCase.getTestStepAt(y);
                    requiredInterfaces.addAll(testStep.getRequiredInterfaces());
                }
            }

            if (DragAndDropSupport.importRequiredInterfaces(target, requiredInterfaces, "Move TestSuite")) {
                WsdlTestSuite importedTestSuite = target.importTestSuite(testSuite, testSuite.getName(), position, true,
                        null);
                if (importedTestSuite != null) {
                    testSuite.getProject().removeTestSuite(testSuite);
                    return importedTestSuite;
                }
            }
        }

        return null;
    }

    public static WsdlTestSuite copyTestSuite(WsdlTestSuite testSuite, WsdlProject target, int position) {
        String name = UISupport.prompt("Specify name of copied TestCase", "Copy TestCase",
                "Copy of " + testSuite.getName());
        if (name == null) {
            return null;
        }

        if (testSuite.getProject() == target) {
            return target.importTestSuite(testSuite, name, position, true, null);
        } else {
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // get required interfaces
            for (TestCase testCase : testSuite.getTestCaseList()) {
                for (int y = 0; y < testCase.getTestStepCount(); y++) {
                    WsdlTestStep testStep = (WsdlTestStep) testCase.getTestStepAt(y);
                    requiredInterfaces.addAll(testStep.getRequiredInterfaces());
                }
            }

            if (DragAndDropSupport.importRequiredInterfaces(target, requiredInterfaces, "Move TestSuite")) {
                return target.importTestSuite(testSuite, testSuite.getName(), position, true, null);
            }
        }

        return null;
    }
}
