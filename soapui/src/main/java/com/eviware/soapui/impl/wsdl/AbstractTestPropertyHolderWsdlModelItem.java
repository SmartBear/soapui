/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ModelItemConfig;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class AbstractTestPropertyHolderWsdlModelItem<T extends ModelItemConfig> extends
        AbstractWsdlModelItem<T> implements MutableTestPropertyHolder {
    private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;
    private final static Logger log = LogManager.getLogger(AbstractTestPropertyHolderWsdlModelItem.class);

    protected AbstractTestPropertyHolderWsdlModelItem(T config, ModelItem parent, String icon) {
        super(config, parent, icon);
    }

    protected void setPropertiesConfig(PropertiesTypeConfig config) {
        if (propertyHolderSupport == null) {
            propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder(this, config);
        } else {
            propertyHolderSupport.resetPropertiesConfig(config);
        }

        String propertyName = createPropertyName(getName());
        if (StringUtils.hasContent(propertyName)) {
            String propFileName = "soapui.properties." + propertyName;
            String propFile = System.getProperty(propFileName);
            if (!StringUtils.hasContent(propFile)) {
                propFile = SoapUI.getGlobalProperties().getPropertyValue(propFileName);
            }

            if (StringUtils.hasContent(propFile)) {
                int result = propertyHolderSupport.addPropertiesFromFile(propFile);
                if (result > 0) {
                    log.info("Overriding " + result + " properties from [" + propFile + "] in [" + getName() + "]");
                }
            }
        }
    }

    protected XmlBeansPropertiesTestPropertyHolder getPropertyHolderSupport() {
        return propertyHolderSupport;
    }

    private String createPropertyName(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer result = new StringBuffer();
        for (char ch : str.toCharArray()) {
            if (Character.isLetterOrDigit(ch)) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public int addPropertiesFromFile(String propFile) {
        return propertyHolderSupport.addPropertiesFromFile(propFile);
    }

    public TestProperty addProperty(String name) {
        return propertyHolderSupport.addProperty(name);
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.addTestPropertyListener(listener);
    }

    public TestProperty getProperty(String name) {
        return propertyHolderSupport == null ? null : propertyHolderSupport.getProperty(name);
    }

    public String[] getPropertyNames() {
        return propertyHolderSupport.getPropertyNames();
    }

    public List<TestProperty> getPropertyList() {
        return propertyHolderSupport.getPropertyList();
    }

    public String getPropertyValue(String name) {
        return propertyHolderSupport == null ? null : propertyHolderSupport.getPropertyValue(name);
    }

    public TestProperty removeProperty(String propertyName) {
        return propertyHolderSupport.removeProperty(propertyName);
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.removeTestPropertyListener(listener);
    }

    public void setPropertyValue(String name, String value) {
        propertyHolderSupport.setPropertyValue(name, value);
    }

    public boolean renameProperty(String name, String newName) {
        return PropertyExpansionUtils.renameProperty(propertyHolderSupport.getProperty(name), newName, this) != null;
    }

    public Map<String, TestProperty> getProperties() {
        return propertyHolderSupport.getProperties();
    }

    public boolean hasProperty(String name) {
        return propertyHolderSupport.hasProperty(name);
    }

    public TestProperty getPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }

    public int getPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }

    public void moveProperty(String propertyName, int targetIndex) {
        propertyHolderSupport.moveProperty(propertyName, targetIndex);
    }

    public ModelItem getModelItem() {
        return this;
    }

    public String getPropertiesLabel() {
        return "Custom Properties";
    }
}
