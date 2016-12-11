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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunListener;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class ProjectRunListenerAdapter implements ProjectRunListener {
    public void afterRun(ProjectRunner projectRunner, ProjectRunContext runContext) {
    }

    public void afterTestSuite(ProjectRunner projectRunner, ProjectRunContext runContext, TestSuiteRunner testRunner) {
    }

    public void beforeRun(ProjectRunner projectRunner, ProjectRunContext runContext) {
    }

    public void beforeTestSuite(ProjectRunner projectRunner, ProjectRunContext runContext, TestSuite testRunnable) {
    }
}
