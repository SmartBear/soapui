/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.rest.actions.request;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a WsdlRequest
 *
 * @author Ole.Matzura
 */

public class CloneRestRequestAction extends AbstractSoapUIAction<RestRequest> {
    public static final String SOAPUI_ACTION_ID = "CloneRestRequestAction";

    public CloneRestRequestAction() {
        super("Clone Request", "Creates a copy of this Request");
    }

    public void perform(RestRequest request, Object param) {
        String name = UISupport
                .prompt("Specify name of cloned Request", "Clone Request", "Copy of " + request.getName());
        if (name == null) {
            return;
        }

        RestRequest newRequest = request.getRestMethod().cloneRequest(request, name);

        UISupport.selectAndShow(newRequest);
    }
}
