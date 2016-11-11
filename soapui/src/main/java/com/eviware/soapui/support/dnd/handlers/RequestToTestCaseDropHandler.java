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
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;

public class RequestToTestCaseDropHandler extends AbstractCopyingModelItemDropHandler<AbstractHttpRequest, WsdlTestCase> {
    public RequestToTestCaseDropHandler() {
        super(AbstractHttpRequest.class, WsdlTestCase.class);
    }

    @Override
    boolean canCopyBefore(AbstractHttpRequest source, WsdlTestCase target) {
        return false;
    }

    @Override
    boolean canCopyOn(AbstractHttpRequest source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean canCopyAfter(AbstractHttpRequest source, WsdlTestCase target) {
        return true;
    }

    @Override
    boolean copyBefore(AbstractHttpRequest source, WsdlTestCase target) {
        return false;
    }

    @Override
    boolean copyOn(AbstractHttpRequest source, WsdlTestCase target) {
        return addRequestToTestCase(source, target);
    }

    @Override
    boolean copyAfter(AbstractHttpRequest source, WsdlTestCase target) {
        return addRequestToTestCase(source, target);
    }

    private boolean addRequestToTestCase(AbstractHttpRequest source, WsdlTestCase target) {
        return AbstractAddRequestToTestCaseAction.addRequestToTestCase(source, target, 0);
    }

    @Override
    String getCopyBeforeInfo(AbstractHttpRequest source, WsdlTestCase target) {
        return null;
    }

    @Override
    String getCopyOnInfo(AbstractHttpRequest source, WsdlTestCase target) {
        return getCopyAfterInfo(source, target);
    }

    @Override
    String getCopyAfterInfo(AbstractHttpRequest source, WsdlTestCase target) {
        return "Add Request [" + source.getName() + "] to TestCase [" + target.getName() + "]";
    }
}
