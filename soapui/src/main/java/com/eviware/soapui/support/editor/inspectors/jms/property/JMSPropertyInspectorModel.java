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

package com.eviware.soapui.support.editor.inspectors.jms.property;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.commons.lang.NotImplementedException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface JMSPropertyInspectorModel {
    public StringToStringMap getJMSProperties();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void setJMSProperties(StringToStringMap jmsProperties);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public boolean isReadOnly();

    public void release();

    public void setInspector(JMSPropertyInspector inspector);

    public static abstract class AbstractJMSPropertyModel<T extends ModelItem> implements JMSPropertyInspectorModel,
            PropertyChangeListener {
        private boolean readOnly;
        private PropertyChangeSupport propertyChangeSupport;
        private final T modelItem;
        private final String propertyName;

        protected AbstractJMSPropertyModel(boolean readOnly, T modelItem, String propertyName) {
            this.readOnly = readOnly;
            this.modelItem = modelItem;
            this.propertyName = propertyName;
            propertyChangeSupport = new PropertyChangeSupport(this);
            modelItem.addPropertyChangeListener(propertyName, this);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            propertyChangeSupport.firePropertyChange(evt);
        }

        public void release() {
            modelItem.removePropertyChangeListener(propertyName, this);
        }

        public T getModelItem() {
            return modelItem;
        }

        public void setJMSProperties(StringToStringMap jmsPorperties) {
            if (!readOnly) {
                throw new NotImplementedException();
            }
        }
    }
}
