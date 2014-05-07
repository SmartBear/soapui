package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequestInterface;

import javax.swing.DefaultComboBoxModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*
 * Copyright 2004-2014 SmartBear Software
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
public class RestRequestMethodModel extends DefaultComboBoxModel implements PropertyChangeListener {
    private RestRequestInterface request;

    public RestRequestMethodModel(RestRequestInterface request) {
        super(RestRequestInterface.HttpMethod.values());
        this.request = request;
        request.addPropertyChangeListener(this);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        super.setSelectedItem(anItem);
        request.setMethod((RestRequestInterface.HttpMethod) anItem);
    }

    @Override
    public Object getSelectedItem() {
        return request.getMethod();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        fireContentsChanged(this, -1, -1);
    }
}
