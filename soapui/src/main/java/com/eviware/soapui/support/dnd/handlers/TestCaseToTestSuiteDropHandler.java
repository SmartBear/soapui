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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;

import java.util.HashSet;
import java.util.Set;

public class TestCaseToTestSuiteDropHandler extends AbstractAfterModelItemDropHandler<WsdlTestCase, WsdlTestSuite> {
    public TestCaseToTestSuiteDropHandler() {
        super(WsdlTestCase.class, WsdlTestSuite.class);
    }

    @Override
    boolean canCopyAfter(WsdlTestCase source, WsdlTestSuite target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestCase source, WsdlTestSuite target) {
        return true;
    }

    @Override
    boolean copyAfter(WsdlTestCase source, WsdlTestSuite target) {
        WsdlTestCase testCase = copyTestCase(source, target, 0);
        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    public static WsdlTestCase copyTestCase(WsdlTestCase testCase, WsdlTestSuite target, int position) {
        String name = UISupport.prompt("Specify name of copied TestCase", "Copy TestCase",
                "Copy of " + testCase.getName());
        if (name == null) {
            return null;
        }

        if (testCase.getTestSuite() == target) {
            return target.importTestCase(testCase, name, position, true, true, true);
        } else if (testCase.getTestSuite().getProject() == target.getProject()) {
            return target.importTestCase(testCase, name, position, true, true, true);
        } else {
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // get required interfaces
            for (int y = 0; y < testCase.getTestStepCount(); y++) {
                WsdlTestStep testStep = testCase.getTestStepAt(y);
                requiredInterfaces.addAll(testStep.getRequiredInterfaces());
            }

            if (DragAndDropSupport.importRequiredInterfaces(target.getProject(), requiredInterfaces, "Copy TestCase")) {
                return target.importTestCase(testCase, name, position, true, true, true);
            }
        }

        return null;
    }

    @Override
    boolean moveAfter(WsdlTestCase source, WsdlTestSuite target) {
        WsdlTestCase testCase = moveTestCase(source, target, 0);
        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    public static WsdlTestCase moveTestCase(WsdlTestCase testCase, WsdlTestSuite target, int position) {
        if (testCase.getTestSuite() == target) {
            int ix = target.getIndexOfTestCase(testCase);

            if (position == -1) {
                target.moveTestCase(ix, target.getTestCaseCount() - ix);
            } else if (ix >= 0 && position != ix) {
                int offset = position - ix;
                if (offset > 0) {
                    offset--;
                }
                target.moveTestCase(ix, offset);
            }
        } else if (testCase.getTestSuite().getProject() == target.getProject()) {
            if (UISupport.confirm("Move TestCase [" + testCase.getName() + "] to TestSuite [" + target.getName() + "]",
                    "Move TestCase")) {
                WsdlTestCase importedTestCase = target.importTestCase(testCase, testCase.getName(), position, true, true,
                        false);
                if (importedTestCase != null) {
                    testCase.getTestSuite().removeTestCase(testCase);
                    return importedTestCase;
                }
            }
        } else if (UISupport.confirm("Move TestCase [" + testCase.getName() + "] to TestSuite [" + target.getName() + "]",
                "Move TestCase")) {
            Set<Interface> requiredInterfaces = new HashSet<Interface>();

            // get required interfaces
            for (int y = 0; y < testCase.getTestStepCount(); y++) {
                WsdlTestStep testStep = testCase.getTestStepAt(y);
                requiredInterfaces.addAll(testStep.getRequiredInterfaces());
            }

            if (DragAndDropSupport.importRequiredInterfaces(target.getProject(), requiredInterfaces, "Move TestCase")) {
                WsdlTestCase importedTestCase = target.importTestCase(testCase, testCase.getName(), position, true, true,
                        false);
                if (importedTestCase != null) {
                    testCase.getTestSuite().removeTestCase(testCase);
                    return importedTestCase;
                }
            }
        }

        return null;
    }

    @Override
    String getCopyAfterInfo(WsdlTestCase source, WsdlTestSuite target) {
        return "Copy TestCase [" + source.getName() + "] to TestSuite [" + target.getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestCase source, WsdlTestSuite target) {
        return "Move TestCase [" + source.getName() + "] to TestSuite [" + target.getName() + "]";
    }

}
