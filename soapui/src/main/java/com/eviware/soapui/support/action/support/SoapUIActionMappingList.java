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

package com.eviware.soapui.support.action.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIActionMapping;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list of SoapUIActionMappings
 *
 * @author ole.matzura
 */

public class SoapUIActionMappingList<T extends ModelItem> extends ArrayList<SoapUIActionMapping<T>> {
    public SoapUIActionMappingList() {
        super();
    }

    public SoapUIActionMappingList(Collection<? extends SoapUIActionMapping<T>> arg0) {
        super(arg0);
    }

    public int getMappingIndex(String id) {
        for (int c = 0; c < size(); c++) {
            if (get(c).getActionId().equals(id)) {
                return c;
            }
        }

        return -1;
    }

    public SoapUIActionMapping<T> getMapping(String id) {
        for (SoapUIActionMapping<T> mapping : this) {
            if (mapping.getActionId().equals(id)) {
                return mapping;
            }
        }

        return null;
    }
}
