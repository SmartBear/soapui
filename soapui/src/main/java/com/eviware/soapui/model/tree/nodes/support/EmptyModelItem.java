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

package com.eviware.soapui.model.tree.nodes.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;

import javax.swing.ImageIcon;
import java.util.Collections;
import java.util.List;

/**
 * Empty ModelItem used by intermediary TreeNodes
 *
 * @author ole.matzura
 */

public class EmptyModelItem extends AbstractModelItem {
    private String name;
    private ImageIcon icon;

    public EmptyModelItem(String name, ImageIcon icon) {
        this.name = name;
        this.icon = icon;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;

        notifyPropertyChanged(ModelItem.NAME_PROPERTY, oldName, name);
    }

    public String getName() {
        return name;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getDescription() {
        return name;
    }

    public Settings getSettings() {
        return SoapUI.getSettings();
    }

    public void release() {
    }

    public String getId() {
        return String.valueOf(hashCode());
    }

    @SuppressWarnings("unchecked")
    public List<? extends ModelItem> getChildren() {
        return Collections.EMPTY_LIST;
    }

    public ModelItem getParent() {
        return null;
    }
}
