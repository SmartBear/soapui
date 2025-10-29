/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.DataSourceStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataSourceTestStep extends WsdlTestStep implements MutableTestPropertyHolder, PropertyExpansionContainer {

    private DataSourceStepConfig dataSourceStepConfig;
    private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;

    public DataSourceTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);
        if (!forLoadTest) {
            setIcon(UISupport.createImageIcon("/datasource_step.png"));
        }

        if (config == null) {
            config = TestStepConfig.Factory.newInstance();
            config.setType("datasource");
        }

        if (config.getConfig() == null) {
            dataSourceStepConfig = (DataSourceStepConfig) config.addNewConfig().changeType(DataSourceStepConfig.type);
        } else {
            dataSourceStepConfig = (DataSourceStepConfig) config.getConfig().changeType(DataSourceStepConfig.type);
        }
        if (dataSourceStepConfig.getProperties() == null) {
            dataSourceStepConfig.addNewProperties();
        }
        propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder(this, dataSourceStepConfig.getProperties());
    }

    public String getFile() {
        return dataSourceStepConfig.getFile();
    }

    public void setFile(String file) {
        dataSourceStepConfig.setFile(file);
    }

    public String getType() {
        return dataSourceStepConfig.getType();
    }

    public void setType(String type) {
        dataSourceStepConfig.setType(type);
    }

    @Override
    public TestStepResult run(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        WsdlTestStepResult result = new WsdlTestStepResult(this);
        try {
            loadData(testCaseRunContext);
            result.setStatus(TestStepResult.TestStepStatus.OK);
        } catch (Exception e) {
            result.setError(e);
            result.setStatus(TestStepResult.TestStepStatus.FAILED);
        }
        return result;
    }

    public void loadData(TestCaseRunContext testCaseRunContext) throws Exception {
        if (getFile() == null || getFile().trim().isEmpty()) {
            return;
        }

        Integer currentRow = 0;
        if (testCaseRunContext != null) {
            currentRow = (Integer) testCaseRunContext.getProperty("currentRow");
            if (currentRow == null) {
                currentRow = 0;
            }
        }

        if ("CSV".equals(getType())) {
            try (CSVReader reader = new CSVReader(new FileReader(getFile()))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.size() < 2) {
                    return;
                }
                String[] header = allRows.get(0);
                String[] row = allRows.get(currentRow + 1);
                for (int j = 0; j < header.length; j++) {
                    String propertyName = header[j];
                    String propertyValue = row[j];
                    TestProperty property = getProperty(propertyName);
                    if (property == null) {
                        property = addProperty(propertyName);
                    }
                    property.setValue(propertyValue);
                }
            }
        } else if ("JSON".equals(getType())) {
            try (FileReader reader = new FileReader(getFile())) {
                JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                if (jsonArray.size() == 0) {
                    return;
                }
                JsonObject jsonObject = jsonArray.get(currentRow).getAsJsonObject();
                for (String key : jsonObject.keySet()) {
                    TestProperty property = getProperty(key);
                    if (property == null) {
                        property = addProperty(key);
                    }
                    property.setValue(jsonObject.get(key).getAsString());
                }
            }
        } else if ("XML".equals(getType())) {
            try (FileReader reader = new FileReader(getFile())) {
                SAXBuilder saxBuilder = new SAXBuilder();
                Document document = saxBuilder.build(reader);
                Element rootElement = document.getRootElement();
                List<Element> children = rootElement.getChildren();
                if (children.isEmpty()) {
                    return;
                }
                Element rowElement = children.get(currentRow);
                for (Element child : rowElement.getChildren()) {
                    TestProperty property = getProperty(child.getName());
                    if (property == null) {
                        property = addProperty(child.getName());
                    }
                    property.setValue(child.getValue());
                }
            }
        }
    }

    @Override
    public TestProperty addProperty(String name) {
        return propertyHolderSupport.addProperty(name);
    }

    @Override
    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.addTestPropertyListener(listener);
    }

    @Override
    public TestProperty getProperty(String name) {
        return propertyHolderSupport.getProperty(name);
    }

    @Override
    public Map<String, TestProperty> getProperties() {
        return propertyHolderSupport.getProperties();
    }

    @Override
    public int getPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }

    @Override
    public List<TestProperty> getPropertyList() {
        return propertyHolderSupport.getPropertyList();
    }

    @Override
    public String[] getPropertyNames() {
        return propertyHolderSupport.getPropertyNames();
    }

    @Override
    public String getPropertyValue(String name) {
        return propertyHolderSupport.getPropertyValue(name);
    }

    @Override
    public void setPropertyValue(String name, String value) {
        propertyHolderSupport.setPropertyValue(name, value);
    }

    @Override
    public boolean hasProperty(String name) {
        return propertyHolderSupport.hasProperty(name);
    }

    @Override
    public TestProperty removeProperty(String propertyName) {
        return propertyHolderSupport.removeProperty(propertyName);
    }

    @Override
    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.removeTestPropertyListener(listener);
    }

    @Override
    public boolean renameProperty(String propertyName, String newName) {
        return propertyHolderSupport.renameProperty(propertyName, newName);
    }

    @Override
    public void moveProperty(String propertyName, int targetIndex) {
        propertyHolderSupport.moveProperty(propertyName, targetIndex);
    }

    @Override
    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> expansions = new ArrayList<>();
        for (TestProperty property : getPropertyList()) {
            expansions.add(new PropertyExpansionImpl(property, ""));
        }
        return expansions.toArray(new PropertyExpansion[0]);
    }

    @Override
    public TestProperty getPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }
}
