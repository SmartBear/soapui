/*
 * Copyright 2004-2019 SmartBear Software
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

package com.smartbear.soapui.other.testondemand;

import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.testondemand.Location;
import com.eviware.soapui.testondemand.TestOnDemandCaller;
import com.google.common.base.Strings;
import com.smartbear.soapui.utils.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         Integration test to test the communication between SoapUI and the
 *         AlertSite Rest API.
 */
@Category(IntegrationTest.class)
public class TestOnDemandCallerTest {
    private static final String FIRST_LOCATION_NAME = "Fort Lauderdale, FL";
    private static final String FIRST_LOCATION_CODE = "10";
    private static final String[] FIRST_SERVER_IP_ADDRESSES = {"10.0.48.17", "127.0.0.1"};

    private static final String SECOND_LOCATION_NAME = "Washington, D.C.";
    private static final String SECOND_LOCATION_CODE = "40";

    private TestOnDemandCaller caller;
    private WsdlTestCase testCase;
    private static final String NOT_THE_RIGHT_HOST = "You need to specify the host name of the test server";

    public final static Logger log = LogManager.getLogger(TestOnDemandCallerTest.class);

    @Before
    public void setUp() throws Exception {
        WsdlProject project = new WsdlProject(TestOnDemandCallerTest.class.getResource(
                "/soapui-projects/sample-soapui-project.xml").getPath());
        WsdlTestSuite testSuite = project.getTestSuiteByName("Test Suite");
        testCase = testSuite.getTestCaseByName("Test Conversions");
        caller = new TestOnDemandCaller();
    }

    @Test
    public void testGetLocations() throws Exception {
        if (System.getProperty(SoapUISystemProperties.TEST_ON_DEMAND_HOST) == null) {
            log.warn(NOT_THE_RIGHT_HOST);
            return;
        }

        List<Location> locations = caller.getLocations();

        Location firstLocation = locations.get(0);
        assertEquals(firstLocation.getName(), FIRST_LOCATION_NAME);
        assertEquals(firstLocation.getCode(), FIRST_LOCATION_CODE);

        Location secondLocation = locations.get(1);
        assertEquals(secondLocation.getName(), SECOND_LOCATION_NAME);
        assertEquals(secondLocation.getCode(), SECOND_LOCATION_CODE);
    }

    @Test
    public void testSendProject() throws Exception {
        if (System.getProperty(SoapUISystemProperties.TEST_ON_DEMAND_HOST) == null) {
            log.warn(NOT_THE_RIGHT_HOST);
            return;
        }

        String redirectUrl = caller.sendTestCase(testCase, new Location(FIRST_LOCATION_CODE, FIRST_LOCATION_CODE,
                FIRST_SERVER_IP_ADDRESSES));
        assert !Strings.isNullOrEmpty(redirectUrl);
    }
}