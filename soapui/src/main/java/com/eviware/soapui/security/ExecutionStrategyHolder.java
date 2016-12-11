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

package com.eviware.soapui.security;

import com.eviware.soapui.config.ExecutionStrategyConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.StrategyTypeConfig.Enum;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ExecutionStrategyHolder {

    private ExecutionStrategyConfig config;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ExecutionStrategyHolder(ExecutionStrategyConfig executionStrategy) {
        this.config = executionStrategy;
    }

    public Enum getStrategy() {
        return config.getStrategy();
    }

    public int getDelay() {
        return config.getDelay();
    }

    public void setDelay(int delay) {
        int oldValue = config.getDelay();
        config.setDelay(delay);

        pcs.firePropertyChange("delay", oldValue, delay);
    }

    public void setStrategy(StrategyTypeConfig.Enum strategy) {
        Enum oldValue = config.getStrategy();
        config.setStrategy(strategy);

        pcs.firePropertyChange("strategy", oldValue, strategy);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public void updateConfig(ExecutionStrategyConfig config) {
        this.config = config;
    }

    public Boolean getImmutable() {
        return config.getImmutable();
    }

    public void setImmutable(Boolean immutable) {
        config.setImmutable(immutable);
    }

}
