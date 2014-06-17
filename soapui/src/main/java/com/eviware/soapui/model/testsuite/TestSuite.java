/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.model.testsuite;

import java.util.List;
import java.util.UUID;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * A TestSuite holding a number of TestCases
 *
 * @author Ole.Matzura
 */

public interface TestSuite extends TestModelItem, ResultContainer, TestRunnable {
    public final static String RUNTYPE_PROPERTY = ModelItem.class.getName() + "@runtype";
    public final static String DISABLED_PROPERTY = TestSuite.class.getName() + "@disabled";

    public Project getProject();

    public int getTestCaseCount();

    public TestCase getTestCaseAt(int index);

    public TestCase getTestCaseByName(String testCaseName);

    public TestCase getTestCaseById(UUID id);

    public List<TestCase> getTestCaseList();

    public void addTestSuiteListener(TestSuiteListener listener);

    public void removeTestSuiteListener(TestSuiteListener listener);

    public enum TestSuiteRunType {
        PARALLEL, SEQUENTIAL
    }

    ;

    public TestSuiteRunType getRunType();

    public int getIndexOfTestCase(TestCase testCase);

    public boolean isDisabled();

    public String getLabel();

    public TestSuiteRunner run(StringToObjectMap context, boolean async);

    public void addTestSuiteRunListener(TestSuiteRunListener listener);

    public void removeTestSuiteRunListener(TestSuiteRunListener listener);
}
