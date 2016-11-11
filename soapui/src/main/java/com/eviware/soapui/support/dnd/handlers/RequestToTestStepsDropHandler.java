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
import com.eviware.soapui.model.tree.nodes.support.WsdlTestStepsModelItem;

public class RequestToTestStepsDropHandler extends
        AbstractCopyingModelItemDropHandler<AbstractHttpRequest, WsdlTestStepsModelItem> {
    public RequestToTestStepsDropHandler() {
        super(AbstractHttpRequest.class, WsdlTestStepsModelItem.class);
    }

    @Override
    boolean canCopyBefore(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return true;
    }

    @Override
    boolean canCopyOn(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return true;
    }

    @Override
    boolean canCopyAfter(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return true;
    }

    @Override
    boolean copyBefore(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return addRequestToTestCase(source, target);
    }

    @Override
    boolean copyOn(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return addRequestToTestCase(source, target);
    }

    @Override
    boolean copyAfter(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return addRequestToTestCase(source, target);
    }

    private boolean addRequestToTestCase(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return AbstractAddRequestToTestCaseAction.addRequestToTestCase(source, target.getTestCase(), 0);
    }

    @Override
    String getCopyBeforeInfo(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getCopyOnInfo(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getCopyAfterInfo(AbstractHttpRequest source, WsdlTestStepsModelItem target) {
        return "Add Request [" + source.getName() + "] to TestCase [" + target.getTestCase().getName() + "]";
    }
}
