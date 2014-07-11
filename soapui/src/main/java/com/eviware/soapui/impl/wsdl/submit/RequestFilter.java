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

package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.model.iface.SubmitContext;

/**
 * Filter for modifying a request before it is sent
 *
 * @author Ole.Matzura
 */

public interface RequestFilter extends SoapUIListener {
    public void filterRequest(SubmitContext context, Request request);

    public void afterRequest(SubmitContext context, Request request);

    /**
     * @deprecated
     */

    public void afterRequest(SubmitContext context, Response response);
}
