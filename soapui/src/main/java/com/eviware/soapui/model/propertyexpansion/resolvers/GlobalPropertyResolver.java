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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.types.StringList;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GlobalPropertyResolver implements PropertyResolver {
    public class EnvironmentPropertyHolder implements TestPropertyHolder {
        public void addTestPropertyListener(TestPropertyListener listener) {
        }

        public ModelItem getModelItem() {
            return null;
        }

        public Map<String, TestProperty> getProperties() {
            Map<String, String> properties = System.getenv();
            Map<String, TestProperty> result = new HashMap<String, TestProperty>();

            for (Object key : properties.keySet()) {
                result.put(key.toString(), new SystemEnviromentTestProperty(key));
            }

            return result;
        }

        public List<TestProperty> getPropertyList() {
            List<TestProperty> result = new ArrayList<TestProperty>();

            for (TestProperty property : getProperties().values()) {
                result.add(property);
            }

            return result;
        }

        public String getPropertiesLabel() {
            return "Environment Properties";
        }

        public TestProperty getProperty(String name) {
            Map<String, String> properties = System.getenv();
            return properties.containsKey(name) ? new SystemEnviromentTestProperty(name) : null;
        }

        public TestProperty getPropertyAt(int index) {
            return getProperty(getPropertyNames()[index]);
        }

        public int getPropertyCount() {
            return System.getenv().size();
        }

        public String[] getPropertyNames() {
            Set<String> keys = System.getenv().keySet();
            StringList result = new StringList();
            for (Object key : keys) {
                result.add(key.toString());
            }
            return result.toStringArray();
        }

        public String getPropertyValue(String name) {
            TestProperty property = getProperty(name);
            return property == null ? null : property.getValue();
        }

        public boolean hasProperty(String name) {
            return System.getenv().containsKey(name);
        }

        public void removeTestPropertyListener(TestPropertyListener listener) {
        }

        public void setPropertyValue(String name, String value) {
        }

        private class SystemEnviromentTestProperty implements TestProperty {
            private final Object key;

            public SystemEnviromentTestProperty(Object key) {
                this.key = key;
            }

            public String getDefaultValue() {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public ModelItem getModelItem() {
                return null;
            }

            public String getName() {
                return key.toString();
            }

            public QName getType() {
                return XmlString.type.getName();
            }

            public String getValue() {
                return System.getenv(key.toString());
            }

            public boolean isReadOnly() {
                return true;
            }

            public void setValue(String value) {
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
    }

    public class SystemPropertyHolder implements TestPropertyHolder {
        public void addTestPropertyListener(TestPropertyListener listener) {
        }

        public ModelItem getModelItem() {
            return null;
        }

        public Map<String, TestProperty> getProperties() {
            Properties properties = System.getProperties();
            Map<String, TestProperty> result = new HashMap<String, TestProperty>();

            for (Object key : properties.keySet()) {
                result.put(key.toString(), new SystemTestProperty(key));
            }

            return result;
        }

        public String getPropertiesLabel() {
            return "System Properties";
        }

        public TestProperty getProperty(String name) {
            Properties properties = System.getProperties();
            return properties.containsKey(name) ? new SystemTestProperty(name) : null;
        }

        public TestProperty getPropertyAt(int index) {
            return getProperty(getPropertyNames()[index]);
        }

        public int getPropertyCount() {
            return System.getProperties().size();
        }

        public List<TestProperty> getPropertyList() {
            List<TestProperty> result = new ArrayList<TestProperty>();

            for (TestProperty property : getProperties().values()) {
                result.add(property);
            }

            return result;
        }

        public String[] getPropertyNames() {
            Set<Object> keys = System.getProperties().keySet();
            StringList result = new StringList();
            for (Object key : keys) {
                result.add(key.toString());
            }
            return result.toStringArray();
        }

        public String getPropertyValue(String name) {
            TestProperty property = getProperty(name);
            return property == null ? null : property.getValue();
        }

        public boolean hasProperty(String name) {
            return System.getProperties().containsKey(name);
        }

        public void removeTestPropertyListener(TestPropertyListener listener) {
        }

        public void setPropertyValue(String name, String value) {
            System.setProperty(name, value);
        }

        private class SystemTestProperty implements TestProperty {
            private final Object key;

            public SystemTestProperty(Object key) {
                this.key = key;
            }

            public String getDefaultValue() {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public ModelItem getModelItem() {
                return null;
            }

            public String getName() {
                return key.toString();
            }

            public QName getType() {
                return XmlString.type.getName();
            }

            public String getValue() {
                return System.getProperty(key.toString());
            }

            public boolean isReadOnly() {
                return false;
            }

            public void setValue(String value) {
                System.setProperty(key.toString(), value);
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
    }

    private SystemPropertyHolder systemPropertyHolder;
    private EnvironmentPropertyHolder environmentPropertyHolder;

    public GlobalPropertyResolver() {
        systemPropertyHolder = new SystemPropertyHolder();
        environmentPropertyHolder = new EnvironmentPropertyHolder();
    }

    public String resolveProperty(PropertyExpansionContext context, String name, boolean globalOverride) {
        String result = ResolverUtils.checkForExplicitReference(name, PropertyExpansion.GLOBAL_REFERENCE,
                PropertyExpansionUtils.getGlobalProperties(), context, false);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(name, PropertyExpansion.SYSTEM_REFERENCE, systemPropertyHolder,
                context, globalOverride);
        if (result != null) {
            return result;
        }

        result = ResolverUtils.checkForExplicitReference(name, PropertyExpansion.ENV_REFERENCE,
                environmentPropertyHolder, context, globalOverride);
        if (result != null) {
            return result;
        }

        // if not, check for explicit global property (stupid 1.7.6 syntax that
        // should be removed..)
        if (name.length() > 2 && name.charAt(0) == PropertyExpansion.PROPERTY_SEPARATOR
                && name.charAt(1) == PropertyExpansion.PROPERTY_SEPARATOR) {
            return PropertyExpansionUtils.getGlobalProperty(name.substring(2));
        } else {
            return PropertyExpansionUtils.getGlobalProperty(name);
        }

    }
}
