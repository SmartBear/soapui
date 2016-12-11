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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

public class TestStepToTestStepDropHandler extends AbstractBeforeAfterModelItemDropHandler<WsdlTestStep, WsdlTestStep> {
    public TestStepToTestStepDropHandler() {
        super(WsdlTestStep.class, WsdlTestStep.class);
    }

    boolean copyAfter(WsdlTestStep source, WsdlTestStep target) {
        return DragAndDropSupport.copyTestStep(source, target.getTestCase(),
                target.getTestCase().getIndexOfTestStep(target) + 1);
    }

    boolean moveAfter(WsdlTestStep source, WsdlTestStep target) {
        return DragAndDropSupport.moveTestStep(source, target.getTestCase(),
                target.getTestCase().getIndexOfTestStep(target) + 1);
    }

    @Override
    boolean canCopyAfter(WsdlTestStep source, WsdlTestStep target) {
        return !SoapUI.getTestMonitor().hasRunningTest(target.getTestCase());
    }

    @Override
    boolean canMoveAfter(WsdlTestStep source, WsdlTestStep target) {
        return source != target;
    }

    @Override
    String getCopyAfterInfo(WsdlTestStep source, WsdlTestStep target) {
        return source.getTestCase() == target.getTestCase() ? "Copy TestStep [" + source.getName()
                + "] within TestCase [" + target.getTestCase().getName() + "]" : "Copy TestStep [" + source.getName()
                + "] to TestCase [" + target.getTestCase().getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlTestStep source, WsdlTestStep target) {
        return source.getTestCase() == target.getTestCase() ? "Move TestStep [" + source.getName()
                + "] within TestCase [" + target.getTestCase().getName() + "]" : "Move TestStep [" + source.getName()
                + "] to TestCase [" + target.getTestCase().getName() + "]";
    }

    @Override
    boolean canCopyBefore(WsdlTestStep source, WsdlTestStep target) {
        return true;
    }

    @Override
    boolean canMoveBefore(WsdlTestStep source, WsdlTestStep target) {
        return source != target;
    }

    @Override
    boolean copyBefore(WsdlTestStep source, WsdlTestStep target) {
        return DragAndDropSupport.copyTestStep(source, target.getTestCase(),
                target.getTestCase().getIndexOfTestStep(target));
    }

    @Override
    String getCopyBeforeInfo(WsdlTestStep source, WsdlTestStep target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getMoveBeforeInfo(WsdlTestStep source, WsdlTestStep target) {
        return getMoveAfterInfo(source, target);
    }

    @Override
    boolean moveBefore(WsdlTestStep source, WsdlTestStep target) {
        return DragAndDropSupport.moveTestStep(source, target.getTestCase(),
                target.getTestCase().getIndexOfTestStep(target));
    }
}
