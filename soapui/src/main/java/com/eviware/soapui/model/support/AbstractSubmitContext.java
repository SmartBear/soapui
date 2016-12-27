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

package com.eviware.soapui.model.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Base-class for submit contexts
 *
 * @author ole.matzura
 */

public abstract class AbstractSubmitContext<T extends ModelItem> implements SubmitContext, Map<String, Object> {
    private DefaultPropertyExpansionContext properties;
    private final T modelItem;

    public AbstractSubmitContext(T modelItem) {
        this.modelItem = modelItem;
        this.properties = new DefaultPropertyExpansionContext(modelItem);

        setProperty(TestCaseRunContext.RUN_COUNT, 0);
        setProperty(TestCaseRunContext.THREAD_INDEX, 0);
    }

    public AbstractSubmitContext(T modelItem, StringToObjectMap properties) {
        this(modelItem);

        if (properties != null && properties.size() > 0) {
            if (this.properties == null) {
                this.properties = new DefaultPropertyExpansionContext(modelItem);
            }

            this.properties.putAll(properties);
        }
    }

    public T getModelItem() {
        return modelItem;
    }

    public Object getProperty(String name, TestStep testStep, WsdlTestCase testCase) {
        if (properties != null && properties.containsKey(name)) {
            return properties.get(name);
        }

        if (testCase != null) {
            int ix = name.indexOf(PROPERTY_SEPARATOR);
            if (ix > 0) {
                String teststepname = name.substring(0, ix);
                TestStep refTestStep = testCase.getTestStepByName(teststepname);
                if (refTestStep != null) {
                    TestProperty property = refTestStep.getProperty(name.substring(ix + 1));
                    return property == null ? null : property.getValue();
                }
            }

            if (testCase.getSearchProperties()) {
                ix = testStep == null ? testCase.getTestStepCount() - 1 : testCase.getIndexOfTestStep(testStep);
                if (ix >= testCase.getTestStepCount()) {
                    ix = testCase.getTestStepCount() - 1;
                }

                while (ix >= 0) {
                    TestProperty property = testCase.getTestStepAt(ix).getProperty(name);
                    if (property != null) {
                        return property.getValue();
                    }

                    ix--;
                }
            }
        }

        return null;
    }

    public Object removeProperty(String name) {
        return properties == null ? null : properties.remove(name);
    }

    public void setProperty(String name, Object value) {
        if (properties == null) {
            properties = new DefaultPropertyExpansionContext(modelItem);
        }

        properties.put(name, value);
    }

    public void setProperty(String name, Object value, TestCase testCase) {
        int ix = name.indexOf(PROPERTY_SEPARATOR);
        if (ix > 0) {
            String teststepname = name.substring(0, ix);
            TestStep refTestStep = testCase.getTestStepByName(teststepname);
            if (refTestStep != null) {
                TestProperty property = refTestStep.getProperty(name.substring(ix + 1));
                if (property != null && !property.isReadOnly()) {
                    property.setValue(value.toString());
                    return;
                }
            }
        }

        if (properties == null) {
            properties = new DefaultPropertyExpansionContext(modelItem);
        }

        properties.put(name, value);
    }

    public boolean hasProperty(String name) {
        return properties == null ? false : properties.containsKey(name);
    }

    public void resetProperties() {
        if (properties != null) {
            properties.clear();
        }
    }

    public void clear() {
        properties.clear();
    }

    public Object clone() {
        return properties.clone();
    }

    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    public boolean equals(Object o) {
        return properties.equals(o);
    }

    public Object get(Object key) {
        return properties.get(key);
    }

    public int hashCode() {
        return properties.hashCode();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        properties.putAll(m);
    }

    public Object remove(Object key) {
        return properties.remove(key);
    }

    public int size() {
        return properties.size();
    }

    public String toString() {
        return properties.toString();
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public StringToObjectMap getProperties() {
        return properties;
    }

    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    public String expand(String content) {
        return PropertyExpander.expandProperties(this, content);
    }
}
