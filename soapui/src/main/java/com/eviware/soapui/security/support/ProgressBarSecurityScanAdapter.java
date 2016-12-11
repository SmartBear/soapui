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

package com.eviware.soapui.security.support;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.panels.SecurityScanNode;
import com.eviware.soapui.security.result.SecurityResult.ResultStatus;
import com.eviware.soapui.security.result.SecurityScanRequestResult;
import com.eviware.soapui.security.result.SecurityScanResult;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeModel;
import java.awt.Color;
import java.awt.Dimension;

public class ProgressBarSecurityScanAdapter extends SecurityTestRunListenerAdapter {

    private static final String SKIPPED_VALUE = "SKIPPED";
    private static final Color OK_COLOR = new Color(0, 204, 102);
    private static final Color FAILED_COLOR = new Color(255, 102, 0);
    private static final Color MISSING_ASSERTION_COLOR = new Color(204, 153, 255);

    private static final String STATE_RUN = "No Alerts";
    private static final String STATE_FAIL = "Alerts";
    private static final String STATE_CANCEL = "Canceled";
    private static final String STATE_MISSING_ASSERTIONS = "Missing Assertions";
    private static final String STATE_MISSING_PARAMETERS = "Missing Parameters";

    private JTree tree;
    private SecurityScanNode node;
    private JProgressBar progressBar;
    private SecurityScan securityCheck;
    private SecurityTest securityTest;
    private int alertsCounter = 0;
    private String prePostFix = " ";

    private JLabel cntLabel;
    private Color defaultBackground;

    public ProgressBarSecurityScanAdapter(JTree tree, SecurityScanNode node, JProgressBar progressBar,
                                          SecurityScan securityCheck, SecurityTest securityTest, JLabel cntLabel) {
        this.tree = tree;
        this.node = node;
        this.progressBar = progressBar;
        this.defaultBackground = progressBar.getBackground();
        this.progressBar.setMaximum(100);
        this.securityCheck = securityCheck;
        this.securityTest = securityTest;

        this.securityTest.addSecurityTestRunListener(this);

        this.cntLabel = cntLabel;
        this.cntLabel.setPreferredSize(new Dimension(50, 18));
        this.cntLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        this.cntLabel.setHorizontalAlignment(SwingConstants.CENTER);

    }

    public void release() {
        securityTest.removeSecurityTestRunListener(this);
        securityTest = null;
        securityCheck = null;
    }

    @Override
    public void afterSecurityScanRequest(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                         SecurityScanRequestResult securityCheckReqResult) {

        if (securityCheckReqResult.getSecurityScan().getTestStep().getId()
                .equals(this.securityCheck.getTestStep().getId())
                && this.securityCheck.getName().equals(securityCheckReqResult.getSecurityScan().getName())) {
            if (securityCheck.getAssertionCount() == 0) {
                progressBar.setForeground(MISSING_ASSERTION_COLOR);
                progressBar.setString(STATE_MISSING_ASSERTIONS);
                if (progressBar.getValue() >= progressBar.getMaximum() * .9) {
                    progressBar.setMaximum(progressBar.getMaximum() + 5);
                }
                progressBar.setValue(progressBar.getValue() + 1);
            } else if (this.securityCheck instanceof AbstractSecurityScanWithProperties) {
                if (((AbstractSecurityScanWithProperties) this.securityCheck).getParameterHolder() != null
                        && ((AbstractSecurityScanWithProperties) this.securityCheck).getParameterHolder()
                        .getParameterList() != null
                        && ((AbstractSecurityScanWithProperties) this.securityCheck).getParameterHolder()
                        .getParameterList().size() == 0) {
                    progressBar.setForeground(MISSING_ASSERTION_COLOR);
                    progressBar.setString(STATE_MISSING_PARAMETERS);
                    progressBar.setValue(progressBar.getMaximum());
                } else {
                    if (securityCheckReqResult.getStatus() == ResultStatus.FAILED) {
                        progressBar.setForeground(FAILED_COLOR);
                        progressBar.setString(STATE_FAIL);
                        alertsCounter++;
                    } else if (securityCheckReqResult.getStatus() == ResultStatus.OK) {
                        if (!progressBar.getForeground().equals(FAILED_COLOR)) {
                            progressBar.setForeground(OK_COLOR);
                            progressBar.setString(STATE_RUN);
                        }
                    }

                    if (alertsCounter != 0) {
                        cntLabel.setOpaque(true);
                        cntLabel.setBackground(FAILED_COLOR);
                        cntLabel.setText(prePostFix + alertsCounter + prePostFix);
                    }

                    if (progressBar.getValue() >= progressBar.getMaximum() * .9) {
                        progressBar.setMaximum(progressBar.getMaximum() + 5);
                    }
                    progressBar.setValue(progressBar.getValue() + 1);

                }
            }
            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
        }
    }

    @Override
    public void beforeRun(TestCaseRunner testRunner, SecurityTestRunContext runContext) {
        progressBar.setIndeterminate(false);

        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setForeground(OK_COLOR);
        cntLabel.setOpaque(false);
        cntLabel.setText("");
        alertsCounter = 0;
        ((DefaultTreeModel) tree.getModel()).nodeChanged(node);

    }

    @Override
    public void afterSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                  SecurityScanResult securityCheckResult) {
        if (securityCheckResult.getSecurityScan().getTestStep().getId().equals(this.securityCheck.getTestStep().getId())
                && this.securityCheck.getName().equals(securityCheckResult.getSecurityScan().getName())) {
            if (securityCheckResult.getStatus() != ResultStatus.CANCELED) {

                if (securityCheck.getAssertionCount() == 0) {
                    progressBar.setForeground(MISSING_ASSERTION_COLOR);
                    progressBar.setString(STATE_MISSING_ASSERTIONS);
                } else {
                    if (this.securityCheck instanceof AbstractSecurityScanWithProperties) {
                        AbstractSecurityScanWithProperties scanWithProperties = (AbstractSecurityScanWithProperties) this.securityCheck;
                        if (scanWithProperties.getParameterHolder() != null
                                && (scanWithProperties.getParameterHolder().getParameterList() != null && (scanWithProperties
                                .getParameterHolder().getParameterList().size() == 0))) {
                            progressBar.setForeground(MISSING_ASSERTION_COLOR);
                            progressBar.setString(STATE_MISSING_PARAMETERS);
                        } else if (securityCheckResult.getStatus() == ResultStatus.SKIPPED) {
                            progressBar.setForeground(defaultBackground);
                            progressBar.setString(SKIPPED_VALUE);
                        }
                    } else {
                        if (securityCheckResult.getStatus() == ResultStatus.SKIPPED) {
                            progressBar.setForeground(defaultBackground);
                            progressBar.setString(SKIPPED_VALUE);
                        }
                    }
                }
                if (progressBar.getString().equals(SKIPPED_VALUE)) {
                    progressBar.setValue(0);
                } else {
                    progressBar.setValue(progressBar.getMaximum());
                }
                if (progressBar.getString().equals("")) {
                    progressBar.setString(STATE_RUN);
                }
            } else {
                progressBar.setString(STATE_CANCEL);
            }
            progressBar.setBackground(defaultBackground);
            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
        }
    }

    @Override
    public void beforeSecurityScan(TestCaseRunner testRunner, SecurityTestRunContext runContext,
                                   SecurityScan securityCheck) {
        if (securityCheck.getTestStep().getId().equals(this.securityCheck.getTestStep().getId())
                && this.securityCheck.getName().equals(securityCheck.getName())) {
            progressBar.setString(STATE_RUN);
            progressBar.setBackground(Color.white);
        }
    }

}
