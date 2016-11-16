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

import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;

public interface TestRunContext extends SubmitContext, PropertyExpansionContext {
    public final static String LOAD_TEST_RUNNER = "LoadTestRunner";
    public static final String THREAD_INDEX = "ThreadIndex";
    public static final String RUN_COUNT = "RunCount";
    public static final String TOTAL_RUN_COUNT = "TotalRunCount";
    public static final String LOAD_TEST_CONTEXT = "LoadTestContext";
    public static final String INTERACTIVE = "Interactive";

    public String expand(String content);

    public TestRunner getTestRunner();
}
