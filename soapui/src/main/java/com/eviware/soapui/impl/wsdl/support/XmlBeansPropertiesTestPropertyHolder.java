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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.PropertiesTypeConfig;
import com.eviware.soapui.config.PropertyConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.support.TestPropertyUtils;
import com.eviware.soapui.model.testsuite.RenameableTestProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class XmlBeansPropertiesTestPropertyHolder implements MutableTestPropertyHolder, Map<String, TestProperty> {
    private PropertiesTypeConfig config;
    private List<PropertiesStepProperty> properties = new ArrayList<PropertiesStepProperty>();
    private Map<String, PropertiesStepProperty> propertyMap = new LinkedHashMap<String, PropertiesStepProperty>();
    private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();
    private ModelItem modelItem;
    private Properties overrideProperties;
    private String propertiesLabel = "Test Properties";

    public XmlBeansPropertiesTestPropertyHolder(ModelItem modelItem, PropertiesTypeConfig config) {
        this.modelItem = modelItem;
        this.config = config;

        for (int c = 0; c < config.sizeOfPropertyArray(); c++) {
            PropertyConfig propertyConfig = config.getPropertyArray(c);
            if (StringUtils.hasContent(propertyConfig.getName())) {
                addProperty(propertyConfig, false, null);
            } else {
                config.removeProperty(c);
                c--;
            }
        }
    }

    protected PropertiesStepProperty addProperty(PropertyConfig propertyConfig, boolean notify,
                                                 TestProperty virtualProperty) {
        PropertiesStepProperty propertiesStepProperty = new PropertiesStepProperty(propertyConfig, virtualProperty);

        properties.add(propertiesStepProperty);

        sortVirtualLast();

        propertyMap.put(propertiesStepProperty.getName().toUpperCase(), propertiesStepProperty);

        if (notify) {
            firePropertyAdded(propertiesStepProperty.getName());
        }

        return propertiesStepProperty;
    }

    /**
     * sort properties list and make sure that virtualproperties are always
     * placed after properties
     */
    private void sortVirtualLast() {
        List<PropertiesStepProperty> virtualProperties = new ArrayList<PropertiesStepProperty>();
        List<PropertiesStepProperty> nonVirtualProperties = new ArrayList<PropertiesStepProperty>();

        for (PropertiesStepProperty psp : properties) {
            if (psp.isVirtualProperty()) {
                if (!virtualProperties.contains(psp)) {
                    virtualProperties.add(psp);
                }
            } else {
                nonVirtualProperties.add(psp);
            }
        }
        properties.clear();
        properties.addAll(nonVirtualProperties);
        properties.addAll(virtualProperties);
    }

    private void firePropertyAdded(String name) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyAdded(name);
        }
    }

    private void firePropertyRemoved(String name) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyRemoved(name);
        }
    }

    private void firePropertyMoved(String name, int oldIndex, int newIndex) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyMoved(name, oldIndex, newIndex);
        }
    }

    private void firePropertyRenamed(String oldName, String newName) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyRenamed(oldName, newName);
        }
    }

    public TestProperty addProperty(String name) {
        PropertyConfig propertyConfig = config.addNewProperty();
        propertyConfig.setName(name);
        return addProperty(propertyConfig, true, null);
    }

    public TestProperty addVirtualProperty(String key, TestProperty virtualProperty) {
        PropertyConfig propertyConfig = PropertyConfig.Factory.newInstance();
        propertyConfig.setName(key);
        return addProperty(propertyConfig, true, virtualProperty);
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        listeners.add(listener);
    }

    public PropertiesStepProperty getProperty(String name) {
        return propertyMap.get(name.toUpperCase());
    }

    public String[] getPropertyNames() {
        String[] result = new String[properties.size()];
        for (int c = 0; c < properties.size(); c++) {
            result[c] = properties.get(c).getName();
        }

        return result;
    }

    public List<TestProperty> getPropertyList() {
        List<TestProperty> result = new ArrayList<TestProperty>();

        for (TestProperty property : properties) {
            result.add(property);
        }

        return result;
    }

    public String getPropertyValue(String name) {
        TestProperty property = getProperty(name);
        return property == null ? null : property.getValue();
    }

    public TestProperty removeProperty(String propertyName) {
        TestProperty property = getProperty(propertyName);
        if (property != null) {
            if (property instanceof PropertiesStepProperty && ((PropertiesStepProperty) property).isVirtualProperty()) {
                return property;
            }
            int ix = properties.indexOf(property);
            propertyMap.remove(propertyName.toUpperCase());
            properties.remove(ix);
            firePropertyRemoved(propertyName);
            config.removeProperty(ix);
            return property;
        }

        return null;
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        listeners.remove(listener);
    }

    public void setPropertyValue(String name, String value) {
        PropertiesStepProperty property = getProperty(name);
        if (property != null) {
            property.setValue(value);
        } else {
            addProperty(name).setValue(value);
        }
    }

    public void resetPropertiesConfig(PropertiesTypeConfig config) {
        this.config = config;

        for (int c = 0; c < config.sizeOfPropertyArray(); c++) {
            properties.get(c).setConfig(config.getPropertyArray(c));
        }
    }

    public boolean renameProperty(String name, String newName) {
        if (getProperty(newName) != null) {
            return false;
        }

        PropertiesStepProperty property = getProperty(name);
        if (property == null) {
            return false;
        }

        property.setName(newName);
        return true;
    }

    /**
     * Internal property class
     *
     * @author ole
     */

    public class PropertiesStepProperty implements RenameableTestProperty {
        private PropertyConfig propertyConfig;
        private final TestProperty virtualProperty;

        public PropertiesStepProperty(PropertyConfig propertyConfig, TestProperty virtualProperty) {
            this.propertyConfig = propertyConfig;
            this.virtualProperty = virtualProperty;
        }

        public boolean isVirtualProperty() {
            return virtualProperty != null;
        }

        public void setConfig(PropertyConfig propertyConfig) {
            this.propertyConfig = propertyConfig;
        }

        public String getName() {
            return propertyConfig.getName();
        }

        public void setName(String name) {
            String oldName = getName();
            propertyConfig.setName(name);

            propertyMap.remove(oldName.toUpperCase());
            propertyMap.put(name.toUpperCase(), this);

            firePropertyRenamed(oldName, name);
        }

        public String getDescription() {
            if (virtualProperty != null) {
                return virtualProperty.getDescription();
            }

            return null;
        }

        public String getValue() {
            if (overrideProperties != null && overrideProperties.containsKey(getName())) {
                return overrideProperties.getProperty(getName());
            }

            if (virtualProperty != null) {
                return virtualProperty.getValue();
            }

            return propertyConfig.getValue();
        }

        public void setValue(String value) {
            String oldValue = getValue();
            propertyConfig.setValue(value);

            if (overrideProperties != null && overrideProperties.containsKey(getName())) {
                overrideProperties.remove(getName());
                if (overrideProperties.isEmpty()) {
                    overrideProperties = null;
                }
            }

            firePropertyValueChanged(getName(), oldValue, value);
        }

        public void firePropertyValueChanged(String name, String oldValue, String newValue) {
            XmlBeansPropertiesTestPropertyHolder.this.firePropertyValueChanged(name, oldValue, newValue);
        }

        public boolean isReadOnly() {
            if (virtualProperty != null) {
                return virtualProperty.isReadOnly();
            }

            return false;
        }

        public QName getType() {
            if (virtualProperty != null) {
                return virtualProperty.getType();
            }

            return XmlString.type.getName();
        }

        public ModelItem getModelItem() {
            return modelItem;
        }

        public String getDefaultValue() {
            if (virtualProperty != null) {
                return virtualProperty.getDefaultValue();
            }

            return null;
        }

        @Override
        public boolean isRequestPart() {
            return false;
        }

        @Override
        public SchemaType getSchemaType() {
            if (virtualProperty != null) {
                return virtualProperty.getSchemaType();
            }

            return XmlString.type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestProperty) {
                TestProperty testProperty = (TestProperty) obj;
                if (getModelItem() != null && testProperty.getModelItem() != null) {
                    return getModelItem().equals(testProperty.getModelItem()) && getName().equals(testProperty.getName());
                } else {
                    return getName().equals(testProperty.getName());
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

    }

    public void firePropertyValueChanged(String name, String oldValue, String newValue) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyValueChanged(name, oldValue, newValue);
        }
    }

    public int saveTo(String fileName) throws IOException {
        return TestPropertyUtils.saveTo(this, fileName);
    }

    public int getPropertyCount() {
        return properties.size();
    }

    public TestProperty getPropertyAt(int index) {
        return properties.get(index);
    }

    public Map<String, TestProperty> getProperties() {
        Map<String, TestProperty> result = new LinkedHashMap<String, TestProperty>();
        for (TestProperty property : propertyMap.values()) {
            result.put(property.getName(), property);
        }

        return result;
    }

    public boolean hasProperty(String name) {
        return propertyMap.containsKey(name.toUpperCase());
    }

    public int addPropertiesFromFile(String propFile) {
        if (!StringUtils.hasContent(propFile)) {
            return 0;
        }

        try {
            InputStream input = null;

            File file = new File(propFile);
            if (file.exists()) {
                input = new FileInputStream(file);
            } else if (propFile.toLowerCase().startsWith("http://") || propFile.toLowerCase().startsWith("https://")) {
                UrlWsdlLoader loader = new UrlWsdlLoader(propFile, getModelItem());
                loader.setUseWorker(false);
                input = loader.load();
            }

            if (input != null) {
                if (overrideProperties == null) {
                    overrideProperties = new Properties();
                }

                int sz = overrideProperties.size();
                overrideProperties.load(input);

                for (Object key : overrideProperties.keySet()) {
                    String name = key.toString();
                    if (!hasProperty(name)) {
                        addProperty(name);
                    }
                }

                return overrideProperties.size() - sz;
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return 0;
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public void moveProperty(String propertyName, int targetIndex) {
        PropertiesStepProperty property = getProperty(propertyName);
        int ix = properties.indexOf(property);

        if (ix == targetIndex) {
            return;
        }

        if (targetIndex < 0) {
            targetIndex = 0;
        }

        String value = property.getValue();
        config.removeProperty(ix);

        PropertyConfig propertyConfig = null;

        if (targetIndex < properties.size()) {
            properties.add(targetIndex, properties.remove(ix));
            propertyConfig = config.insertNewProperty(targetIndex);
        } else {
            properties.add(properties.remove(ix));
            propertyConfig = config.addNewProperty();
        }

        propertyConfig.setName(propertyName);
        propertyConfig.setValue(value);

        resetPropertiesConfig(config);

        if (targetIndex > properties.size()) {
            targetIndex = properties.size();
        }

        firePropertyMoved(propertyName, ix, targetIndex);
    }

    public void clear() {
        while (size() > 0) {
            removeProperty(getPropertyAt(0).getName());
        }
    }

    public boolean containsKey(Object key) {
        return hasProperty((String) key);
    }

    public boolean containsValue(Object value) {
        return propertyMap.containsValue(value);
    }

    public Set<java.util.Map.Entry<String, TestProperty>> entrySet() {
        HashSet<java.util.Map.Entry<String, TestProperty>> result = new HashSet<Entry<String, TestProperty>>();

        for (TestProperty p : propertyMap.values()) {
            // This does not compile on JDK 1.5:
            // result.add( new java.util.HashMap.SimpleEntry<String,
            // TestProperty>(p.getName(), p));
            result.add(new HashMapEntry<String, TestProperty>(p.getName(), p));
        }

        return result;
    }

    private static class HashMapEntry<K, V> implements java.util.Map.Entry<K, V> {
        private K key;
        private V value;

        public HashMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    public TestProperty get(Object key) {
        return getProperty((String) key);
    }

    public boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    public Set<String> keySet() {
        return new HashSet<String>(Arrays.asList(getPropertyNames()));
    }

    public TestProperty put(String key, TestProperty value) {
        TestProperty result = addProperty(key);
        result.setValue(value.getValue());
        return result;
    }

    public void putAll(Map<? extends String, ? extends TestProperty> m) {
        for (TestProperty p : m.values()) {
            addProperty(p.getName()).setValue(p.getValue());
        }
    }

    public TestProperty remove(Object key) {
        return removeProperty((String) key);
    }

    public int size() {
        return propertyMap.size();
    }

    public Collection<TestProperty> values() {
        ArrayList<TestProperty> result = new ArrayList<TestProperty>();
        result.addAll(propertyMap.values());
        return result;
    }

    public String getPropertiesLabel() {
        return propertiesLabel;
    }

    public void setPropertiesLabel(String propertiesLabel) {
        this.propertiesLabel = propertiesLabel;
    }
}
