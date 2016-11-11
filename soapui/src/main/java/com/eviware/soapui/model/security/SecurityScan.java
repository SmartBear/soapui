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

package com.eviware.soapui.model.security;

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ExecutionStrategyHolder;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.result.SecurityScanResult;
import org.apache.xmlbeans.XmlObject;

import javax.swing.JComponent;

public interface SecurityScan extends ModelItem, Assertable {

    public static final String SECURITY_SCAN_REQUEST_RESULT = "SecurityScanRequestResult";
    public static final String SECURITY_CHECK_RESPONSE_RESULT = "SecurityScanResponseResult";
    public static final String STATUS_PROPERTY = SecurityScan.class.getName() + "@status";

    void updateSecurityConfig(SecurityScanConfig config);

    SecurityScanResult run(TestStep testStep, SecurityTestRunContext context, SecurityTestRunner securityTestRunner);

    boolean isConfigurable();

    /**
     * Gets desktop configuration for specific SecurityCheck
     *
     * @param TestStep the TestStep to create the config for, could be null for
     *                 HttpMonitor checks
     * @return
     */
    JComponent getComponent();

    /**
     * The type of this check
     *
     * @return
     */
    String getType();

    TestStep getTestStep();

    void setTestStep(TestStep step);

    // Object runTearDownScript( SecurityTestRunner runner,
    // SecurityTestRunContext context ) throws Exception;
    //
    // Object runSetupScript( SecurityTestRunner runner, SecurityTestRunContext
    // context ) throws Exception;

    /**
     * Checks if the test is disabled
     *
     * @return true if disabled
     */
    boolean isDisabled();

    /**
     * Disables or Enables the check
     *
     * @param disabled
     */
    void setDisabled(boolean disabled);

    ExecutionStrategyHolder getExecutionStrategy();

    void setExecutionStrategy(ExecutionStrategyHolder executionStrategyHolder);

    // String getSetupScript();
    //
    // void setSetupScript( String text );
    //
    // String getTearDownScript();
    //
    // void setTearDownScript( String text );

    // name used in configuration panel
    String getConfigName();

    // description usd in configuration panel
    String getConfigDescription();

    // help url used for configuration panel ( help for this check )
    String getHelpURL();

    /**
     * Advanced setting panel for configuration
     *
     * @return
     */
    JComponent getAdvancedSettingsPanel();

    public SecurityScanResult getSecurityScanResult();

    XmlObject getConfig();

    void copyConfig(SecurityScanConfig backupCheckConfig);

    void addWsdlAssertion(String assertionLabel);

    boolean isApplyForFailedStep();

    void setApplyForFailedTestStep(boolean apply);

    boolean isRunOnlyOnce();

    void setRunOnlyOnce(boolean runOnlyOnce);

    /*
     * indicates in case of runOnlyOnce set if the scan was already run that once
     */
    boolean isSkipFurtherRunning();

    void setSkipFurtherRunning(boolean skipFurtherRunning);

    void release();
}
