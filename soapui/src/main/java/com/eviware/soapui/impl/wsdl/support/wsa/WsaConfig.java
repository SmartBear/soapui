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

package com.eviware.soapui.impl.wsdl.support.wsa;

import com.eviware.soapui.config.MustUnderstandTypeConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.support.PropertyChangeNotifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WsaConfig implements PropertyChangeNotifier {

    private WsaConfigConfig wsaConfig;

    private PropertyChangeSupport propertyChangeSupport;

    private final WsaContainer container;

    public WsaConfig(WsaConfigConfig wsaConfig, WsaContainer container) {
        this.wsaConfig = wsaConfig;
        this.container = container;
        propertyChangeSupport = new PropertyChangeSupport(this);
        if (!wsaConfig.isSetMustUnderstand()) {
            wsaConfig.setMustUnderstand(MustUnderstandTypeConfig.NONE);
        }
        if (!wsaConfig.isSetVersion()) {
            wsaConfig.setVersion(WsaVersionTypeConfig.X_200508);
        }
    }

    public String getAction() {
        return wsaConfig.getAction();
    }

    public String getFaultTo() {
        return wsaConfig.getFaultTo();
    }

    public String getFrom() {
        return wsaConfig.getFrom();
    }

    public String getTo() {
        return wsaConfig.getTo();
    }

    public String getRelationshipType() {
        return wsaConfig.getRelationshipType();
    }

    public String getRelatesTo() {
        return wsaConfig.getRelatesTo();
    }

    public String getMessageID() {
        return wsaConfig.getMessageID();
    }

    public boolean isGenerateMessageId() {
        return wsaConfig.getGenerateMessageId();
    }

    public void setGenerateMessageId(boolean generateMessageId) {

        boolean oldValue = isGenerateMessageId();
        wsaConfig.setGenerateMessageId(generateMessageId);
        propertyChangeSupport.firePropertyChange("generateMessageId", oldValue, generateMessageId);
    }

    public boolean isAddDefaultTo() {
        return wsaConfig.getAddDefaultTo();
    }

    public void setAddDefaultTo(boolean addDefaultTo) {

        boolean oldValue = isAddDefaultTo();
        wsaConfig.setAddDefaultTo(addDefaultTo);
        propertyChangeSupport.firePropertyChange("addDefaultTo", oldValue, addDefaultTo);
    }

    public boolean isAddDefaultAction() {
        return wsaConfig.getAddDefaultAction();
    }

    public void setAddDefaultAction(boolean addDefaultAction) {

        boolean oldValue = isAddDefaultAction();
        wsaConfig.setAddDefaultAction(addDefaultAction);
        propertyChangeSupport.firePropertyChange("addDefaultAction", oldValue, addDefaultAction);
    }

    public String getReplyTo() {
        return wsaConfig.getReplyTo();
    }

    public String getVersion() {
        return wsaConfig.getVersion().toString();
    }

    public boolean isWsaEnabled() {
        return container.isWsaEnabled();
    }

    public String getMustUnderstand() {
        return wsaConfig.getMustUnderstand().toString();
    }

    public void setAction(String arg0) {
        String oldValue = getAction();
        wsaConfig.setAction(arg0);
        propertyChangeSupport.firePropertyChange("action", oldValue, arg0);
    }

    public void setFaultTo(String arg0) {
        String oldValue = getFaultTo();
        wsaConfig.setFaultTo(arg0);
        propertyChangeSupport.firePropertyChange("faultTo", oldValue, arg0);

    }

    public void setFrom(String arg0) {
        String oldValue = getFrom();
        wsaConfig.setFrom(arg0);
        propertyChangeSupport.firePropertyChange("from", oldValue, arg0);
    }

    public void setTo(String arg0) {
        String oldValue = getTo();
        wsaConfig.setTo(arg0);
        propertyChangeSupport.firePropertyChange("to", oldValue, arg0);
    }

    public void setRelationshipType(String arg0) {
        String oldValue = getRelationshipType();
        wsaConfig.setRelationshipType(arg0);
        propertyChangeSupport.firePropertyChange("relationshipType", oldValue, arg0);
    }

    public void setRelatesTo(String arg0) {
        String oldValue = getRelatesTo();
        wsaConfig.setRelatesTo(arg0);
        propertyChangeSupport.firePropertyChange("relatesTo", oldValue, arg0);
    }

    public void setMessageID(String arg0) {
        String oldValue = getMessageID();
        wsaConfig.setMessageID(arg0);
        propertyChangeSupport.firePropertyChange("messageID", oldValue, arg0);
    }

    public void setReplyTo(String arg0) {
        String oldValue = getReplyTo();
        wsaConfig.setReplyTo(arg0);
        propertyChangeSupport.firePropertyChange("replyTo", oldValue, arg0);
    }

    public void setMustUnderstand(String arg0) {
        String oldValue = getMustUnderstand();
        wsaConfig.setMustUnderstand(MustUnderstandTypeConfig.Enum.forString(arg0));
        propertyChangeSupport.firePropertyChange("mustUnderstand", oldValue, arg0);
    }

    public void setVersion(String arg0) {
        String oldValue = getVersion();
        wsaConfig.setVersion(WsaVersionTypeConfig.Enum.forString(arg0));
        propertyChangeSupport.firePropertyChange("version", oldValue, arg0);
    }

    public void setWsaEnabled(boolean arg0) {
        boolean oldValue = isWsaEnabled();
        container.setWsaEnabled(arg0);
        propertyChangeSupport.firePropertyChange("wsaEnabled", oldValue, arg0);
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

    public WsaContainer getWsaContainer() {
        return container;
    }

    public void setConfig(WsaConfigConfig wsaConfig) {
        this.wsaConfig = wsaConfig;
    }

    public String getFaultToRefParams() {
        return wsaConfig.getFaultToRefParams();
    }

    public void setFaultToRefParams(String arg0) {
        String oldValue = getFaultToRefParams();
        wsaConfig.setFaultToRefParams(arg0);
        propertyChangeSupport.firePropertyChange("faultToRefParams", oldValue, arg0);
    }

    public String getReplyToRefParams() {
        return wsaConfig.getReplyToRefParams();
    }

    public void setReplyToRefParams(String arg0) {
        String oldValue = getReplyToRefParams();
        wsaConfig.setReplyToRefParams(arg0);
        propertyChangeSupport.firePropertyChange("replyToRefParams", oldValue, arg0);
    }

}
