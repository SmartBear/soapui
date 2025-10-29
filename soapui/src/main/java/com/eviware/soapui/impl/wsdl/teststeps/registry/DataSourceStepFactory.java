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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.DataSourceTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;

public class DataSourceStepFactory extends WsdlTestStepFactory {
    private static final String DATA_SOURCE_STEP_ID = "datasource";
    private static final String DATA_SOURCE_STEP_NAME = "Data Source";
    private static final String DATA_SOURCE_STEP_DESCRIPTION = "Reads data from an external source to be used in other test steps.";
    public DataSourceStepFactory() {
        super(DATA_SOURCE_STEP_ID, DATA_SOURCE_STEP_NAME, DATA_SOURCE_STEP_DESCRIPTION, "/datasource_step.png");
    }

    @Override
    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new DataSourceTestStep(testCase, config, forLoadTest);
    }

    @Override
    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(DATA_SOURCE_STEP_ID);
        testStepConfig.setName(name);
        return testStepConfig;
    }

    @Override
    public boolean canCreate() {
        return true;
    }
}
