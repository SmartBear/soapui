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

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AbstractAddRequestToTestCaseAction;
import com.eviware.soapui.model.testsuite.TestStep;

public class RequestToTestStepDropHandler extends AbstractCopyingModelItemDropHandler<AbstractHttpRequest, TestStep> {
    public RequestToTestStepDropHandler() {
        super(AbstractHttpRequest.class, TestStep.class);
    }

    @Override
    boolean canCopyBefore(AbstractHttpRequest source, TestStep target) {
        return true;
    }

    @Override
    boolean canCopyOn(AbstractHttpRequest source, TestStep target) {
        return true;
    }

    @Override
    boolean canCopyAfter(AbstractHttpRequest source, TestStep target) {
        return true;
    }

    @Override
    boolean copyBefore(AbstractHttpRequest source, TestStep target) {
        return addRequestToTestCase(source, target, target.getTestCase().getIndexOfTestStep(target));
    }

    @Override
    boolean copyOn(AbstractHttpRequest source, TestStep target) {
        return copyAfter(source, target);
    }

    @Override
    boolean copyAfter(AbstractHttpRequest source, TestStep target) {
        return addRequestToTestCase(source, target, target.getTestCase().getIndexOfTestStep(target) + 1);
    }

    private boolean addRequestToTestCase(AbstractHttpRequest source, TestStep target, int index) {
        return AbstractAddRequestToTestCaseAction.addRequestToTestCase(source, target.getTestCase(), index);
    }

    @Override
    String getCopyBeforeInfo(AbstractHttpRequest source, TestStep target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getCopyOnInfo(AbstractHttpRequest source, TestStep target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getCopyAfterInfo(AbstractHttpRequest source, TestStep target) {
        return "Add Request [" + source.getName() + "] to TestCase [" + target.getTestCase().getName() + "]";
    }
}
