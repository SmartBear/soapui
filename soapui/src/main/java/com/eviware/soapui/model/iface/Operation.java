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

package com.eviware.soapui.model.iface;

import com.eviware.soapui.model.ModelItem;

import java.util.List;

/**
 * Operation interface
 *
 * @author Ole.Matzura
 */

public interface Operation extends ModelItem {
    public final static String ACTION_PROPERTY = Operation.class.getName() + "@action";

    public boolean isUnidirectional();

    public boolean isBidirectional();

    public Request getRequestAt(int index);

    public Request getRequestByName(String requestName);

    public List<Request> getRequestList();

    public int getRequestCount();

    public Interface getInterface();

    public MessagePart[] getDefaultRequestParts();

    public MessagePart[] getDefaultResponseParts();

    public String createRequest(boolean b);

    public String createResponse(boolean b);
}
