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

import com.eviware.soapui.config.DataSourceLoopStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.panels.DataSourceLoopTestStepPanel;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.UISupport;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.util.List;

import com.eviware.soapui.impl.wsdl.support.XmlBeansPropertiesTestPropertyHolder;
import java.util.List;
import java.util.Map;

public class DataSourceLoopTestStep extends WsdlTestStep {

    private XmlBeansPropertiesTestPropertyHolder propertyHolderSupport;

    public TestProperty getPropertyAt(int index) {
        return propertyHolderSupport.getPropertyAt(index);
    }

    public TestProperty addProperty(String name) {
        return propertyHolderSupport.addProperty(name);
    }

    public void addTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.addTestPropertyListener(listener);
    }

    public TestProperty getProperty(String name) {
        return propertyHolderSupport.getProperty(name);
    }

    public Map<String, TestProperty> getProperties() {
        return propertyHolderSupport.getProperties();
    }

    public int getPropertyCount() {
        return propertyHolderSupport.getPropertyCount();
    }

    public List<TestProperty> getPropertyList() {
        return propertyHolderSupport.getPropertyList();
    }

    public String[] getPropertyNames() {
        return propertyHolderSupport.getPropertyNames();
    }

    public String getPropertyValue(String name) {
        return propertyHolderSupport.getPropertyValue(name);
    }

    public void setPropertyValue(String name, String value) {
        propertyHolderSupport.setPropertyValue(name, value);
    }

    public boolean hasProperty(String name) {
        return propertyHolderSupport.hasProperty(name);
    }

    public TestProperty removeProperty(String propertyName) {
        return propertyHolderSupport.removeProperty(propertyName);
    }

    public void removeTestPropertyListener(TestPropertyListener listener) {
        propertyHolderSupport.removeTestPropertyListener(listener);
    }

    public boolean renameProperty(String propertyName, String newName) {
        return propertyHolderSupport.renameProperty(propertyName, newName);
    }

    public void moveProperty(String propertyName, int targetIndex) {
        propertyHolderSupport.moveProperty(propertyName, targetIndex);
    }

    private DataSourceLoopStepConfig dataSourceLoopStepConfig;

    public DataSourceLoopTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        super(testCase, config, true, forLoadTest);
        if (!forLoadTest) {
            setIcon(UISupport.createImageIcon("/datasource_loop_step.png"));
        }

        if (config.getConfig() == null) {
            dataSourceLoopStepConfig = (DataSourceLoopStepConfig) config.addNewConfig().changeType(DataSourceLoopStepConfig.type);
        } else {
            dataSourceLoopStepConfig = (DataSourceLoopStepConfig) config.getConfig().changeType(DataSourceLoopStepConfig.type);
        }
        if (dataSourceLoopStepConfig.getProperties() == null) {
            dataSourceLoopStepConfig.addNewProperties();
        }
        propertyHolderSupport = new XmlBeansPropertiesTestPropertyHolder(this, dataSourceLoopStepConfig.getProperties());
    }

    public String getDataSourceStep() {
        return dataSourceLoopStepConfig.getDataSourceStep();
    }

    public void setDataSourceStep(String dataSourceStep) {
        dataSourceLoopStepConfig.setDataSourceStep(dataSourceStep);
    }

    @Override
    public TestStepResult run(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        WsdlTestStepResult result = new WsdlTestStepResult(this);

        DataSourceTestStep dataSourceStep = (DataSourceTestStep) getTestCase().getTestStepByName(getDataSourceStep());
        if (dataSourceStep == null) {
            result.setStatus(TestStepResult.TestStepStatus.FAILED);
            result.setError(new Exception("Could not find DataSource step [" + getDataSourceStep() + "]"));
            return result;
        }

        try {
            dataSourceStep.loadData(testCaseRunContext);
        } catch (Exception e) {
            result.setStatus(TestStepResult.TestStepStatus.FAILED);
            result.setError(e);
            return result;
        }

        int rowCount = 0;
        try (CSVReader reader = new CSVReader(new FileReader(dataSourceStep.getFile()))) {
            rowCount = reader.readAll().size() - 1;
        } catch (Exception e) {
            result.setStatus(TestStepResult.TestStepStatus.FAILED);
            result.setError(e);
            return result;
        }

        Integer currentRow = (Integer) testCaseRunContext.getProperty("currentRow");
        if (currentRow == null) {
            currentRow = 0;
        }

        if (currentRow < rowCount) {
            testCaseRunContext.setProperty("currentRow", currentRow + 1);
            testCaseRunner.gotoStepByName(getDataSourceStep());
        } else {
            testCaseRunContext.setProperty("currentRow", 0);
        }

        result.setStatus(TestStepResult.TestStepStatus.OK);
        return result;
    }
}
