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

package com.eviware.soapui.support.action;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * A group of actions for a ModelItem
 *
 * @author ole.matzura
 */

public interface SoapUIActionGroup<T extends ModelItem> {
    public String getId();

    public String getName();

    public SoapUIActionMappingList<T> getActionMappings(T modelItem);

    public SoapUIActionMapping<? extends ModelItem> addMapping(String id, SoapUIActionMapping<T> mapping);

    public SoapUIActionMapping<? extends ModelItem> addMapping(String id, int index, SoapUIActionMapping<T> mapping);

    public int getMappingIndex(String positionRef);
}
