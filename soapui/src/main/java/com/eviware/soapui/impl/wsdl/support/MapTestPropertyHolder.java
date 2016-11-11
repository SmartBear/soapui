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
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringList;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MapTestPropertyHolder implements MutableTestPropertyHolder {
    private Map<String, TestProperty> propertyMap = new HashMap<String, TestProperty>();
    private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();
    private List<TestProperty> properties = new ArrayList<TestProperty>();
    public ModelItem modelItem;
    private String propertiesLabel = "Test Properties";

    public MapTestPropertyHolder(ModelItem modelItem) {
        this.modelItem = modelItem;
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

    private void firePropertyValueChanged(String name, String oldValue, String newValue) {
        TestPropertyListener[] listenersArray = listeners.toArray(new TestPropertyListener[listeners.size()]);
        for (TestPropertyListener listener : listenersArray) {
            listener.propertyValueChanged(name, oldValue, newValue);
        }
    }

    public TestProperty addProperty(String name) {
        TestProperty result = new InternalTestProperty(name, null);
        propertyMap.put(name.toUpperCase(), result);
        properties.add(result);
        firePropertyAdded(name);
        return result;
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        listeners.add(listener);
    }

    public TestProperty getProperty(String name) {
        return propertyMap.get(name.toUpperCase());
    }

    public String[] getPropertyNames() {
        StringList result = new StringList();
        for (String name : propertyMap.keySet()) {
            result.add(propertyMap.get(name).getName());
        }

        return result.toStringArray();
    }

    public List<TestProperty> getPropertyList() {
        return Collections.unmodifiableList(properties);
    }

    public String getPropertyValue(String name) {
        TestProperty property = getProperty(name);
        return property == null ? null : property.getValue();
    }

    public TestProperty removeProperty(String propertyName) {
        TestProperty property = getProperty(propertyName);
        if (property != null) {
            properties.remove(property);
            propertyMap.remove(propertyName.toUpperCase());
            firePropertyRemoved(propertyName);
        }

        return property;
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        listeners.remove(listener);
    }

    public void setPropertyValue(String name, String value) {
        InternalTestProperty property = (InternalTestProperty) getProperty(name);
        if (property != null) {
            property.setValue(value);
        }
    }

    public boolean renameProperty(String name, String newName) {
        if (getProperty(newName) != null) {
            return false;
        }

        InternalTestProperty property = (InternalTestProperty) getProperty(name);
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

    public class InternalTestProperty implements TestProperty {
        private String name;
        private String value;

        public InternalTestProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String oldName = getName();

            propertyMap.remove(oldName.toUpperCase());
            propertyMap.put(name.toUpperCase(), this);

            this.name = name;

            firePropertyRenamed(oldName, name);
        }

        public String getDescription() {
            return null;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            String oldValue = getValue();
            this.value = value;

            firePropertyValueChanged(getName(), oldValue, value);
        }

        public boolean isReadOnly() {
            return false;
        }

        public QName getType() {
            return XmlString.type.getName();
        }

        public ModelItem getModelItem() {
            return modelItem;
        }

        public String getDefaultValue() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isRequestPart() {
            return false;
        }

        @Override
        public SchemaType getSchemaType() {
            return XmlBeans.getBuiltinTypeSystem().findType(getType());

        }
    }

    public void saveTo(Properties props) {
        int cnt = 0;
        for (TestProperty p : properties) {
            String name = p.getName();
            String value = p.getValue();
            if (value == null) {
                value = "";
            }

            props.setProperty(name, value);
            cnt++;
        }
    }

    public Map<String, TestProperty> getProperties() {
        Map<String, TestProperty> result = new HashMap<String, TestProperty>();
        for (String name : propertyMap.keySet()) {
            result.put(name, propertyMap.get(name));
        }

        return result;
    }

    public boolean hasProperty(String name) {
        return propertyMap.containsKey(name.toUpperCase());
    }

    public int addPropertiesFromFile(String propFile) {
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

            Properties properties = new Properties();
            properties.load(input);

            for (Object key : properties.keySet()) {
                String name = key.toString();
                if (!hasProperty(name)) {
                    addProperty(name).setValue(properties.getProperty(name));
                } else {
                    setPropertyValue(name, properties.getProperty(name));
                }
            }

            return properties.size();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return 0;
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public void moveProperty(String propertyName, int targetIndex) {
        TestProperty property = getProperty(propertyName);
        int ix = properties.indexOf(property);

        if (ix == targetIndex) {
            return;
        }

        if (targetIndex < 0) {
            targetIndex = 0;
        }

        if (targetIndex < properties.size()) {
            properties.add(targetIndex, properties.remove(ix));
        } else {
            properties.add(properties.remove(ix));
        }

        if (targetIndex > properties.size()) {
            targetIndex = properties.size();
        }

        firePropertyMoved(propertyName, ix, targetIndex);
    }

    public TestProperty getPropertyAt(int index) {
        return properties.get(index);
    }

    public int getPropertyCount() {
        return properties.size();
    }

    public void setPropertiesLabel(String propertiesLabel) {
        this.propertiesLabel = propertiesLabel;
    }

    public String getPropertiesLabel() {
        return propertiesLabel;
    }
}
