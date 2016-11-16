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

package com.eviware.soapui.impl.wsdl.support.jms.property;

import com.eviware.soapui.config.JMSPropertiesConfConfig;
import com.eviware.soapui.config.JMSPropertyConfig;
import com.eviware.soapui.support.PropertyChangeNotifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class JMSPropertiesConfig implements PropertyChangeNotifier {

    private JMSPropertiesConfConfig jmsPropertiesConfConfig;

    private PropertyChangeSupport propertyChangeSupport;

    private final JMSPropertyContainer container;

    public JMSPropertiesConfig(JMSPropertiesConfConfig jmsPropertiesConfConfig, JMSPropertyContainer container) {
        this.jmsPropertiesConfConfig = jmsPropertiesConfConfig;
        this.container = container;
        // propertyChangeSupport = new PropertyChangeSupport(this);
        // if (!jmsPropertyConfConfig.isSetJMSDeliveryMode())
        // {
        // jmsPropertyConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
        // }
    }

    public JMSPropertiesConfConfig getJmsPropertyConfConfig() {
        return jmsPropertiesConfConfig;
    }

    public void setJmsPropertyConfConfig(JMSPropertiesConfConfig jmsPropertiesConfConfig) {
        this.jmsPropertiesConfConfig = jmsPropertiesConfConfig;
    }

    public JMSPropertyContainer getContainer() {
        return container;
    }

    public List<JMSPropertyConfig> getJMSProperties() {
        return jmsPropertiesConfConfig.getJmsPropertiesList();
    }

    public void setJMSProperties(List<JMSPropertyConfig> map) {

        // List<JMSPropertyConfig> propertyList =
        // jmsPropertiesConfConfig.getJmsPropertiesList();
        // StringToStringMap stringToStringMap = new
        // StringToStringMap(propertyList.size());
        // for (JMSPropertyConfig jmsProperty:propertyList){
        // stringToStringMap
        // }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

}
