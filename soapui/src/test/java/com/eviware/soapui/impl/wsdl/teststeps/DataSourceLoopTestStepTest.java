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
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSourceLoopTestStepTest {

    private DataSourceTestStep dataSourceTestStep;
    private DataSourceLoopTestStep loopTestStep;
    private WsdlTestCase testCase;

    @Before
    public void setUp() throws SoapUIException {
        com.eviware.soapui.config.TestCaseConfig testCaseConfig = com.eviware.soapui.config.TestCaseConfig.Factory.newInstance();
        testCaseConfig.addNewProperties();
        testCase = new WsdlTestCase(null, testCaseConfig, false);

        TestStepConfig dataSourceConfig = TestStepConfig.Factory.newInstance();
        dataSourceConfig.setType("datasource");
        dataSourceConfig.setName("DataSource");
        dataSourceTestStep = (DataSourceTestStep) testCase.addTestStep(dataSourceConfig);

        TestStepConfig loopConfig = TestStepConfig.Factory.newInstance();
        loopConfig.setType("datasourceloop");
        loopConfig.setName("DataSourceLoop");
        loopTestStep = (DataSourceLoopTestStep) testCase.addTestStep(loopConfig);
    }

    @Test
    public void testLoop() throws Exception {
        File csvFile = File.createTempFile("test", ".csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("name,age\n");
            writer.write("John,30\n");
            writer.write("Jane,25\n");
        }
        dataSourceTestStep.setFile(csvFile.getAbsolutePath());
        dataSourceTestStep.setType("CSV");
        loopTestStep.setDataSourceStep(dataSourceTestStep.getName());

        TestCaseRunner runner = mock(TestCaseRunner.class);
        when(runner.getTestCase()).thenReturn(testCase);

        StringToObjectMap properties = new StringToObjectMap();
        TestCaseRunContext context = mock(TestCaseRunContext.class);
        when(context.getProperty("currentRow")).thenAnswer(invocation -> properties.get("currentRow"));
        doAnswer(invocation -> {
            properties.put("currentRow", 0);
            return null;
        }).when(context).setProperty("currentRow", 0);
        doAnswer(invocation -> {
            properties.put("currentRow", 1);
            return null;
        }).when(context).setProperty("currentRow", 1);
        doAnswer(invocation -> {
            properties.put("currentRow", 2);
            return null;
        }).when(context).setProperty("currentRow", 2);

        loopTestStep.run(runner, context);
        loopTestStep.run(runner, context);
        loopTestStep.run(runner, context);

        verify(runner, times(2)).gotoStepByName(dataSourceTestStep.getName());
    }
}
