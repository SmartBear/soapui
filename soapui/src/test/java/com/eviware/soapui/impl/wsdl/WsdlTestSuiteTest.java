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

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.config.TestSuiteConfig;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.ModelItemMatchers.belongsTo;
import static com.eviware.soapui.utils.ModelItemMatchers.hasATestCaseNamed;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author manne
 */
public class WsdlTestSuiteTest {

    private WsdlTestSuite suite;
    private WsdlProject project;

    @Before
    public void setUp() throws Exception {
        project = mock(WsdlProject.class);
        suite = new WsdlTestSuite(project, TestSuiteConfig.Factory.newInstance());
    }

    @Test
    public void referencesProject() {
        assertThat(suite, belongsTo(project));
    }

    @Test
    public void addsTestCasesForNames() throws Exception {
        String testCaseName = "Frakking big test case";
        suite.addNewTestCase(testCaseName);
        assertThat(suite, hasATestCaseNamed(testCaseName));

    }

    @Test
    public void doesNotAddTestCaseWithNullName() throws Exception {
        try {
            suite.addNewTestCase(null);
        } catch (Exception e) {

        }
        assertThat(suite, not(hasATestCaseNamed(null)));

    }

}
