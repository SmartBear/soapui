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
import com.eviware.soapui.support.types.StringList;

/**
 * Default SoapUIActionGroup implementation
 *
 * @author ole.matzura
 */

public class DefaultSoapUIActionGroup<T extends ModelItem> extends AbstractSoapUIActionGroup<T> {
    private SoapUIActionMappingList<T> mappings = new SoapUIActionMappingList<T>();
    private StringList ids = new StringList();

    public DefaultSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<T> getActionMappings(T modelItem) {
        return mappings;
    }

    @Override
    public SoapUIActionMapping<T> addMapping(String id, int index, SoapUIActionMapping<T> mapping) {
        if (index == -1 || index >= mappings.size()) {
            return addMapping(id, mapping);
        }

        mappings.add(index, mapping);
        ids.add(index, id);
        return mapping;
    }

    @Override
    public SoapUIActionMapping<T> addMapping(String id, SoapUIActionMapping<T> mapping) {
        mappings.add(mapping);
        ids.add(id);
        return mapping;
    }

    @Override
    public int getMappingIndex(String positionRef) {
        return ids.indexOf(positionRef);
    }
}
