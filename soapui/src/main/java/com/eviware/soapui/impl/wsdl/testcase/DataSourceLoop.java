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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.config.DataSourceLoopConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;

import java.util.ArrayList;
import java.util.List;

public class DataSourceLoop extends AbstractWsdlModelItem<DataSourceLoopConfig> {

    private final WsdlTestCase testCase;
    private final List<WsdlTestStep> testSteps = new ArrayList<>();

    public DataSourceLoop(WsdlTestCase testCase, DataSourceLoopConfig config) {
        super(config, testCase, "/datasource_loop_step.png");
        this.testCase = testCase;

        for (TestStepConfig testStepConfig : config.getTestStepList()) {
            WsdlTestStep testStep = testCase.createTestStepFromConfig(testStepConfig);
            if (testStep != null) {
                testSteps.add(testStep);
            }
        }
    }

    public String getDataSourceStep() {
        return getConfig().getDataSourceStep();
    }

    public void setDataSourceStep(String dataSourceStep) {
        getConfig().setDataSourceStep(dataSourceStep);
    }

    public WsdlTestCase getTestCase() {
        return testCase;
    }

    public List<WsdlTestStep> getTestSteps() {
        return testSteps;
    }

    public void addTestStep(WsdlTestStep testStep) {
        testSteps.add(testStep);
        getConfig().addNewTestStep().set(testStep.getConfig());
    }

    public void removeTestStep(WsdlTestStep testStep) {
        testSteps.remove(testStep);
        for (int i = 0; i < getConfig().getTestStepList().size(); i++) {
            if (getConfig().getTestStepList().get(i).getName().equals(testStep.getName())) {
                getConfig().removeTestStep(i);
                break;
            }
        }
    }
}
