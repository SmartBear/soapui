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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.eviware.soapui.model.mock.MockOperation;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.PropertyChangeNotifier;

public abstract class AbstractMockOperationDispatcher implements PropertyChangeNotifier, MockOperationDispatcher {
    private MockOperation mockOperation;
    private PropertyChangeSupport propertyChangeSupport;

    protected AbstractMockOperationDispatcher(MockOperation mockOperation) {
        this.mockOperation = mockOperation;
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Override
    public JComponent getEditorComponent() {
        return new JPanel();
    }

    @Override
    public void releaseEditorComponent() {
    }

    @Override
    public void release() {
        // TODO : the following line causes NullPointerException when getSettings
        // TODO : will removing it cause a memory leak?
        //mockOperation = null;
    }

    public MockOperation getMockOperation() {
        return mockOperation;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
}
