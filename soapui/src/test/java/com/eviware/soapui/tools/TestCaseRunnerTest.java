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

package com.eviware.soapui.tools;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.Tools;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestCaseRunnerTest {

    private String projectFilePath = TestCaseRunnerTest.class.getResource("/sample-soapui-project.xml").getPath();

    @Test
    public void testReplaceHost() throws Exception {
        assertEquals("http://test2:8080/test", Tools.replaceHost("http://test:8080/test", "test2"));

        assertEquals("http://test2/test", Tools.replaceHost("http://test/test", "test2"));

        assertEquals("http://test2:8080", Tools.replaceHost("http://test:8080", "test2"));

        assertEquals("http://test2", Tools.replaceHost("http://test", "test2"));

        assertEquals("http://test2:8081", Tools.replaceHost("http://test:8080", "test2:8081"));

        assertEquals("http://test2:8081/test", Tools.replaceHost("http://test:8080/test", "test2:8081"));
    }

    @Test
    public void testInvalidTestCaseName() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile(projectFilePath);
        runner.setTestCase("tjoho");

        boolean failed = false;
        try {
            runner.run();
            failed = true;
        } catch (Exception e) {
            assertEquals(e.getMessage(), "TestCase with name [tjoho] is missing in Project [Sample Project]");
        }

        assertFalse(failed);
    }

    @Test
    public void testPropertyExpansionInOutputFolder() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setOutputFolder("/dev/${#Project#Env}");

        assertEquals("/dev/", runner.getAbsoluteOutputFolder(null));
        WsdlProject project = new WsdlProject(projectFilePath);
        project.setPropertyValue("Env", "test");
        assertEquals("/dev/test", runner.getAbsoluteOutputFolder(project));
    }

    @Test
    public void testInvalidTestCaseWithValidTestSuiteName() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile(new File(projectFilePath).toURI().toString());
        runner.setTestCase("tjoho");
        runner.setTestSuite("Test Suite");

        boolean failed = false;
        try {
            runner.run();
            failed = true;
        } catch (Exception e) {
            assertEquals(e.getMessage(),
                    "TestCase with name [tjoho] in TestSuite [Test Suite] is missing in Project [Sample Project]");
        }

        assertFalse(failed);
    }

    @Test
    public void testInvalidTestSuiteName() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile(new File(projectFilePath).toURI().toString());
        runner.setTestSuite("tjoho");

        boolean failed = false;
        try {
            runner.run();
            failed = true;
        } catch (Exception e) {
            assertEquals(e.getMessage(), "TestSuite with name [tjoho] is missing in Project [Sample Project]");
        }

        assertFalse(failed);
    }

    @Test
    public void testTestCaseRunner() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile(projectFilePath);
        // assertTrue( runner.run() );
    }

    @Test
    public void testValidTestSuiteAndTestCaseName() throws Exception {
        SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
        runner.setProjectFile(projectFilePath);
        runner.setTestSuite("Test Suite");
        runner.setTestCase("Test Conversions");
        // assertTrue( runner.run() );
    }
}
