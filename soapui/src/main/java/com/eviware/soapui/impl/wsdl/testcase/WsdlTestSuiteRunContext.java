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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * TestRunContext for WsdlTestCase runners
 *
 * @author Ole.Matzura
 */

public class WsdlTestSuiteRunContext extends AbstractSubmitContext<WsdlTestSuite> implements TestSuiteRunContext {
    private final WsdlTestSuiteRunner testRunner;
    private TestSuite testSuite;

    public WsdlTestSuiteRunContext(TestSuiteRunner testRunner, StringToObjectMap properties) {
        super((WsdlTestSuite) testRunner.getTestSuite(), properties);
        this.testRunner = (WsdlTestSuiteRunner) testRunner;
    }

    public TestSuiteRunner getTestRunner() {
        return testRunner;
    }

    public TestSuite getTestSuite() {
        return testRunner.getTestSuite();
    }

    @Override
    public Object get(Object key) {
        if ("currentTestCase".equals(key)) {
            return getCurrentTestCase();
        }

        if ("currentTestCaseIndex".equals(key)) {
            return getCurrentTestCaseIndex();
        }

        if ("settings".equals(key)) {
            return getSettings();
        }

        if ("testSuite".equals(key)) {
            return getTestSuite();
        }

        if ("testRunner".equals(key)) {
            return getTestRunner();
        }

        return super.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = get(key);
        setProperty(key, value);
        return oldValue;
    }

    public void reset() {
        resetProperties();
    }

    public String expand(String content) {
        return PropertyExpander.expandProperties(this, content);
    }

    public Settings getSettings() {
        return testSuite == null ? SoapUI.getSettings() : testSuite.getSettings();
    }

    public TestCase getCurrentTestCase() {
        return testRunner.getCurrentTestCase();
    }

    public int getCurrentTestCaseIndex() {
        return testRunner.getCurrentTestCaseIndex();
    }

    public TestSuiteRunner getTestSuiteRunner() {
        return testRunner;
    }

    public Object getProperty(String name) {
        return super.get(name);
    }
}
