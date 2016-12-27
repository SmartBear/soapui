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

package com.eviware.soapui.impl.wsdl.support.jms.header;

import com.eviware.soapui.config.JMSDeliveryModeTypeConfig;
import com.eviware.soapui.config.JMSHeaderConfConfig;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSHeader;
import com.eviware.soapui.support.PropertyChangeNotifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class JMSHeaderConfig implements PropertyChangeNotifier {

    private JMSHeaderConfConfig jmsHeaderConfConfig;

    private PropertyChangeSupport propertyChangeSupport;

    private final JMSHeaderContainer container;

    public JMSHeaderConfig(JMSHeaderConfConfig jmsHeaderConfConfig, JMSHeaderContainer container) {
        this.jmsHeaderConfConfig = jmsHeaderConfConfig;
        this.container = container;
        propertyChangeSupport = new PropertyChangeSupport(this);
        if (!jmsHeaderConfConfig.isSetJMSDeliveryMode()) {
            jmsHeaderConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
        }
    }

    public JMSHeaderContainer getContainer() {
        return container;
    }

    public void setJMSHeaderConfConfig(JMSHeaderConfConfig jmsHeaderConfConfig) {
        this.jmsHeaderConfConfig = jmsHeaderConfConfig;
    }

    public String getJMSCorrelationID() {
        return jmsHeaderConfConfig.getJMSCorrelationID();
    }

    public String getJMSReplyTo() {
        return jmsHeaderConfConfig.getJMSReplyTo();
    }

    public String getJMSDeliveryMode() {
        return jmsHeaderConfConfig.getJMSDeliveryMode().toString();
    }

    public String getJMSPriority() {
        return jmsHeaderConfConfig.getJMSPriority();
    }

    public String getJMSType() {
        return jmsHeaderConfConfig.getJMSType();
    }

    public String getTimeToLive() {
        return jmsHeaderConfConfig.getTimeToLive();
    }

    public String getDurableSubscriptionName() {
        return jmsHeaderConfConfig.getDurableSubscriptionName();
    }

    public String getClientID() {
        return jmsHeaderConfConfig.getClientID();
    }

    public String getMessageSelector() {
        return jmsHeaderConfConfig.getMessageSelector();
    }

    public boolean getSendAsBytesMessage() {
        return jmsHeaderConfConfig.getSendAsBytesMessage();
    }

    public boolean getSoapActionAdd() {
        return jmsHeaderConfConfig.getSoapActionAdd();
    }

    public void setJMSCorrelationID(String newValue) {
        String oldValue = getJMSCorrelationID();
        jmsHeaderConfConfig.setJMSCorrelationID(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.JMSCORRELATIONID, oldValue, newValue);
    }

    public void setJMSDeliveryMode(String newValue) {
        String oldValue = getJMSDeliveryMode();
        jmsHeaderConfConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.Enum.forString(newValue));
        propertyChangeSupport.firePropertyChange(JMSHeader.JMSDELIVERYMODE, oldValue, newValue);
    }

    public void setJMSPriority(String newValue) {
        String oldValue = getJMSPriority();
        jmsHeaderConfConfig.setJMSPriority(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.JMSPRIORITY, oldValue, newValue);
    }

    public void setJMSReplyTo(String newValue) {
        String oldValue = getJMSReplyTo();
        jmsHeaderConfConfig.setJMSReplyTo(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.JMSREPLYTO, oldValue, newValue);
    }

    public void setJMSType(String newValue) {
        String oldValue = getJMSType();
        jmsHeaderConfConfig.setJMSType(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.JMSTYPE, oldValue, newValue);
    }

    public void setTimeToLive(String newValue) {
        String oldValue = getTimeToLive();
        jmsHeaderConfConfig.setTimeToLive(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.TIMETOLIVE, oldValue, newValue);
    }

    public void setDurableSubscriptionName(String newValue) {
        String oldValue = getTimeToLive();
        jmsHeaderConfConfig.setDurableSubscriptionName(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.DURABLE_SUBSCRIPTION_NAME, oldValue, newValue);
    }

    public void setClientID(String newValue) {
        String oldValue = getTimeToLive();
        jmsHeaderConfConfig.setClientID(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.CLIENT_ID, oldValue, newValue);
    }

    public void setMessageSelector(String newValue) {
        String oldValue = getMessageSelector();
        jmsHeaderConfConfig.setMessageSelector(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.MESSAGE_SELECTOR, oldValue, newValue);
    }

    public void setSendAsBytesMessage(boolean newValue) {
        boolean oldValue = getSendAsBytesMessage();
        jmsHeaderConfConfig.setSendAsBytesMessage(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.SEND_AS_BYTESMESSAGE, oldValue, newValue);
    }

    public void setSoapActionAdd(boolean newValue) {
        boolean oldValue = getSoapActionAdd();
        jmsHeaderConfConfig.setSoapActionAdd(newValue);
        propertyChangeSupport.firePropertyChange(JMSHeader.SOAP_ACTION_ADD, oldValue, newValue);
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
