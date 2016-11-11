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

package com.eviware.soapui.model.testsuite;

import java.util.List;

/**
 * Runs a TestCase
 *
 * @author Ole.Matzura
 */

public interface TestCaseRunner extends TestRunner {
    /**
     * Gets the TestCase being run
     *
     * @return the TestCase being run
     */

    public TestCase getTestCase();

    /**
     * Gets the accumulated results so far; each TestStep returns a
     * TestStepResult when running.
     *
     * @return the accumulated results so far
     */

    public List<TestStepResult> getResults();

    /**
     * Transfers execution of this TestRunner to the TestStep with the specified
     * index in the TestCase
     */

    public void gotoStep(int index);

    /**
     * Transfers execution of this TestRunner to the TestStep with the specified
     * name in the TestCase
     */

    public void gotoStepByName(String stepName);

    /**
     * Runs the specified TestStep and returns the result
     */

    public TestStepResult runTestStepByName(String name);

    /**
     * Returns the context used by this runner
     */

    public TestCaseRunContext getRunContext();
}
