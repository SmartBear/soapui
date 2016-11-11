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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

public class TestStepToTestCaseDropHandler extends AbstractBeforeAfterModelItemDropHandler<WsdlTestStep, WsdlTestCase> {
    public TestStepToTestCaseDropHandler() {
        super(WsdlTestStep.class, WsdlTestCase.class);
    }

    @Override
    boolean copyAfter(WsdlTestStep source, WsdlTestCase target) {
        return DragAndDropSupport.copyTestStep(source, target, -1);
    }

    @Override
    boolean moveAfter(WsdlTestStep source, WsdlTestCase target) {
        return DragAndDropSupport.moveTestStep(source, target, -1);
    }

    @Override
    boolean canCopyAfter(WsdlTestStep source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlTestStep source, WsdlTestCase target) {
        return true;
    }

    @Override
    String getCopyAfterInfo(WsdlTestStep source, WsdlTestCase target) {
        return source.getTestCase() == target ? "Copy TestStep [" + source.getName() + "] within TestCase ["
                + target.getName() + "]" : "Copy TestStep [" + source.getName() + "] to TestCase [" + target.getName()
                + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestStep source, WsdlTestCase target) {
        return source.getTestCase() == target ? "Move TestStep [" + source.getName() + "] within TestCase ["
                + target.getName() + "]" : "Move TestStep [" + source.getName() + "] to TestCase [" + target.getName()
                + "]";
    }

    @Override
    boolean canCopyBefore(WsdlTestStep source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean canMoveBefore(WsdlTestStep source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean copyBefore(WsdlTestStep source, WsdlTestCase target) {
        return DragAndDropSupport.copyTestStep(source, target, 0);
    }

    @Override
    String getCopyBeforeInfo(WsdlTestStep source, WsdlTestCase target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getMoveBeforeInfo(WsdlTestStep source, WsdlTestCase target) {
        return getMoveAfterInfo(source, target);
    }

    @Override
    boolean moveBefore(WsdlTestStep source, WsdlTestCase target) {
        return DragAndDropSupport.moveTestStep(source, target, 0);
    }
}
