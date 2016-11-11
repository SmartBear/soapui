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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;

/**
 * Default implementation for a SoapUIActionMapping
 *
 * @author ole.matzura
 */

public class DefaultActionMapping<T extends ModelItem> implements SoapUIActionMapping<T> {
    private String actionId;
    private String keyStroke;
    private String iconPath;
    private boolean isDefault;
    private Object param;
    private String description;
    private String name;
    private boolean enabled = true;
    //private boolean isToolbarAction;
    //private int toolbarIndex = 100;

    public DefaultActionMapping(String actionId, String keyStroke, String iconPath, boolean isDefault, Object param) {
        super();
        this.actionId = actionId;
        this.keyStroke = keyStroke;
        this.iconPath = iconPath;
        this.isDefault = isDefault;
        this.param = param;
    }

    @SuppressWarnings("unchecked")
    public SoapUIAction<T> getAction() {
        return SoapUI.getActionRegistry().getAction(actionId);
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getKeyStroke() {
        return keyStroke;
    }

    public String getActionId() {
        return actionId;
    }

    public Object getParam() {
        return param;
    }

    public String getDescription() {
        return description == null ? getAction().getDescription() : description;
    }

    public String getName() {
        return name == null ? getAction().getName() : name;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public SoapUIActionMapping<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setKeyStroke(String keyStroke) {
        this.keyStroke = keyStroke;
    }

    public SoapUIActionMapping<T> setName(String name) {
        this.name = name;
        return this;
    }

    public SoapUIActionMapping<T> setParam(Object param) {
        this.param = param;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SoapUIActionMapping<T> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public void setToolbarAction(boolean toolbarAction) {
    }

    public int getToolbarIndex() {
        return -1;
    }

    public void setToolbarIndex(int toolbarIndex) {
    }

}
