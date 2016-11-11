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

/**
 * The mapping of a SoapUIAction into a SoapUIActionGroup
 *
 * @author ole.matzura
 */

public interface SoapUIActionMapping<T extends ModelItem> {
    public SoapUIAction<T> getAction();

    public String getActionId();

    public String getName();

    public String getDescription();

    public boolean isDefault();

    public boolean isEnabled();

    public String getIconPath();

    public String getKeyStroke();

    public Object getParam();

    public SoapUIActionMapping<T> setName(String name);

    public SoapUIActionMapping<T> setDescription(String description);

    public SoapUIActionMapping<T> setParam(Object param);

    public SoapUIActionMapping<T> setEnabled(boolean enabled);

    int getToolbarIndex();
}
