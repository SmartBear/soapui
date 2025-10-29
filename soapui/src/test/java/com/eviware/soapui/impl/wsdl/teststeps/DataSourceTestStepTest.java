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

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.support.SoapUIException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DataSourceTestStepTest {

    private DataSourceTestStep testStep;
    private WsdlTestCase testCase;

    @Before
    public void setUp() throws SoapUIException {
        com.eviware.soapui.config.TestCaseConfig testCaseConfig = com.eviware.soapui.config.TestCaseConfig.Factory.newInstance();
        testCaseConfig.addNewProperties();
        testCase = new WsdlTestCase(null, testCaseConfig, false);
        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType("datasource");
        testStep = new DataSourceTestStep(testCase, testStepConfig, false);
    }

    @Test
    public void testLoadDataFromCsv() throws Exception {
        File csvFile = File.createTempFile("test", ".csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("name,age\n");
            writer.write("John,30\n");
            writer.write("Jane,25\n");
        }
        testStep.setFile(csvFile.getAbsolutePath());
        testStep.setType("CSV");

        testStep.run(mock(TestCaseRunner.class), null);

        assertEquals("John", testStep.getPropertyValue("name"));
        assertEquals("30", testStep.getPropertyValue("age"));
    }
}
