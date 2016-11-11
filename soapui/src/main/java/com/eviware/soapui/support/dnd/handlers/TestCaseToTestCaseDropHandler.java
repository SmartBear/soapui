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

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;

public class TestCaseToTestCaseDropHandler extends AbstractBeforeAfterModelItemDropHandler<WsdlTestCase, WsdlTestCase> {
    public TestCaseToTestCaseDropHandler() {
        super(WsdlTestCase.class, WsdlTestCase.class);
    }

    @Override
    boolean canCopyAfter(WsdlTestCase source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestCase source, WsdlTestCase target) {
        return source != target;
    }

    @Override
    boolean copyAfter(WsdlTestCase source, WsdlTestCase target) {
        WsdlTestCase testCase = TestCaseToTestSuiteDropHandler.copyTestCase(source, target.getTestSuite(), target
                .getTestSuite().getIndexOfTestCase(target) + 1);

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    boolean moveAfter(WsdlTestCase source, WsdlTestCase target) {
        WsdlTestCase testCase = TestCaseToTestSuiteDropHandler.moveTestCase(source, target.getTestSuite(), target
                .getTestSuite().getIndexOfTestCase(target) + 1);

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    String getCopyAfterInfo(WsdlTestCase source, WsdlTestCase target) {
        return "Copy TestCase [" + source.getName() + "] to TestSuite [" + target.getTestSuite().getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestCase source, WsdlTestCase target) {
        return source == target ? "Move TestCase [" + source.getName() + "] within TestSuite" : "Move TestCase ["
                + source.getName() + "] to TestSuite in Project [" + target.getName() + "]";
    }

    @Override
    boolean canCopyBefore(WsdlTestCase source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean canMoveBefore(WsdlTestCase source, WsdlTestCase target) {
        return source != target;
    }

    @Override
    boolean copyBefore(WsdlTestCase source, WsdlTestCase target) {
        WsdlTestCase testCase = TestCaseToTestSuiteDropHandler.copyTestCase(source, target.getTestSuite(), target
                .getTestSuite().getIndexOfTestCase(target));

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }

    @Override
    String getCopyBeforeInfo(WsdlTestCase source, WsdlTestCase target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getMoveBeforeInfo(WsdlTestCase source, WsdlTestCase target) {
        return getMoveAfterInfo(source, target);
    }

    @Override
    boolean moveBefore(WsdlTestCase source, WsdlTestCase target) {
        WsdlTestCase testCase = TestCaseToTestSuiteDropHandler.moveTestCase(source, target.getTestSuite(), target
                .getTestSuite().getIndexOfTestCase(target));

        if (testCase != null) {
            UISupport.select(testCase);
        }

        return testCase != null;
    }
}
