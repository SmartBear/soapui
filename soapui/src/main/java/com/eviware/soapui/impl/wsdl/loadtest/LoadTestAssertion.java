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

package com.eviware.soapui.impl.wsdl.loadtest;

import com.eviware.soapui.config.LoadTestAssertionConfig;
import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.PropertyChangeNotifier;
import org.apache.xmlbeans.XmlObject;

import javax.swing.ImageIcon;

/**
 * Assertion for LoadTest runs
 *
 * @author Ole.Matzura
 */

public interface LoadTestAssertion extends PropertyChangeNotifier {
    public final static String NAME_PROPERTY = LoadTestAssertion.class.getName() + "@name";
    public final static String ICON_PROPERTY = LoadTestAssertion.class.getName() + "@icon";
    public final static String CONFIGURATION_PROPERTY = LoadTestAssertion.class.getName() + "@configuration";

    public static final String ALL_TEST_STEPS = "- Total -";
    public static final String ANY_TEST_STEP = "- Any -";

    public String getName();

    public ImageIcon getIcon();

    public XmlObject getConfiguration();

    public void updateConfiguration(LoadTestAssertionConfig configuration);

    public String assertResult(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestStepResult result,
                               TestCaseRunner testRunner, TestCaseRunContext runContext);

    public String assertResults(LoadTestRunner loadTestRunner, LoadTestRunContext context, TestCaseRunner testRunner,
                                TestCaseRunContext runContext);

    public String getTargetStep();

    public void setTargetStep(String name);

    public String getDescription();

    public void release();
}
