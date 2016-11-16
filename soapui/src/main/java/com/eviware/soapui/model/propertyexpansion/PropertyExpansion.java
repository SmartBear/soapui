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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.model.testsuite.TestProperty;

public interface PropertyExpansion {
    // scope specifiers
    public static final String SYSTEM_REFERENCE = "#System#";
    public static final String ENV_REFERENCE = "#Env#";
    public static final String GLOBAL_REFERENCE = "#Global#";
    public static final String PROJECT_REFERENCE = "#Project#";
    public static final String TESTSUITE_REFERENCE = "#TestSuite#";
    public static final String TESTCASE_REFERENCE = "#TestCase#";
    public static final String MOCKSERVICE_REFERENCE = "#MockService#";
    public static final String MOCKRESPONSE_REFERENCE = "#MockResponse#";
    public static final String SECURITYTEST_REFERENCE = "#SecurityTest#";

    public static final char PROPERTY_SEPARATOR = '#';
    public static final char XPATH_SEPARATOR = '#';
    public static final char SCOPE_PREFIX = '#';

    public TestProperty getProperty();

    public String toString();

    public String getXPath();

    public String getContainerInfo();
}
