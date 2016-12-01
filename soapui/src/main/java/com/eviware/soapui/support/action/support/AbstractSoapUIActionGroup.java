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
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * Abstract SoapUIActionGroup for extension
 *
 * @author ole.matzura
 */

public abstract class AbstractSoapUIActionGroup<T extends ModelItem> implements SoapUIActionGroup<T> {
    protected final String id;
    protected final String name;

    public AbstractSoapUIActionGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SoapUIActionMapping<T> addMapping(String id, SoapUIActionMapping<T> mapping) {
        return null;
    }

    public SoapUIActionMapping<T> addMapping(String id, int index, SoapUIActionMapping<T> mapping) {
        return null;
    }

    public int getMappingIndex(String positionRef) {
        return -1;
    }
}
