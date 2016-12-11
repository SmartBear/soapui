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

package com.eviware.soapui.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ModelItemPropertyEditorModel<T extends ModelItem> extends AbstractEditorModel implements
        PropertyChangeListener {
    private T modelItem;
    private String propertyName;

    public ModelItemPropertyEditorModel(T modelItem, String propertyName) {
        this.modelItem = modelItem;
        this.propertyName = propertyName;

        modelItem.addPropertyChangeListener(propertyName, this);
    }

    public Settings getSettings() {
        return modelItem.getSettings();
    }

    public String getEditorText() {
        try {
            Object value = PropertyUtils.getSimpleProperty(modelItem, propertyName);
            return value == null ? "" : String.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setEditorText(String text) {
        try {
            PropertyUtils.setSimpleProperty(modelItem, propertyName, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        super.release();

        modelItem.removePropertyChangeListener(propertyName, this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireEditorTextChanged(String.valueOf(evt.getOldValue()), String.valueOf(evt.getNewValue()));
    }
}
