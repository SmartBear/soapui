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

package com.eviware.soapui.impl;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * Empty PanelBuilder implementation for extension.
 *
 * @author Ole.Matzura
 */

public class EmptyPanelBuilder<T extends ModelItem> implements PanelBuilder<T> {
    private static final EmptyPanelBuilder<?> instance = new EmptyPanelBuilder<EmptyModelItem>();

    public static EmptyPanelBuilder<?> get() {
        return instance;
    }

    public Component buildOverviewPanel(T modelItem) {
        String caption = "Properties";
        if (modelItem.getClass().getSimpleName().startsWith("Wsdl")) {
            caption = modelItem.getClass().getSimpleName().substring(4);

            if (caption.endsWith("TestStep")) {
                caption = caption.substring(0, caption.length() - 8);
            }

            caption += " Properties";
        }

        return buildDefaultProperties(modelItem, caption);
    }

    protected JPropertiesTable<T> buildDefaultProperties(T modelItem, String caption) {
        JPropertiesTable<T> table = new JPropertiesTable<T>(caption, modelItem);

        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);

        table.setPropertyObject(modelItem);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }

    public boolean hasDesktopPanel() {
        return false;
    }

    public DesktopPanel buildDesktopPanel(T modelItem) {
        return null;
    }
}
