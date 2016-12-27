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

package com.eviware.soapui.tools;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;

public abstract class AbstractSoapUITestRunner extends AbstractSoapUIRunner implements TestRunListener {
    private String endpoint;
    private String domain;
    private String password;
    private String username;
    private String host;
    private String wssPasswordType;
    private String projectPassword;

    public AbstractSoapUITestRunner(String title) {
        super(title);
    }

    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }

    /**
     * Sets the host to use by all test-requests, the existing endpoint port and
     * path will be used
     *
     * @param host the host to use by all requests
     */

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the domain to use for any authentications
     *
     * @param domain the domain to use for any authentications
     */

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the password to use for any authentications
     *
     * @param password the password to use for any authentications
     */

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the WSS password-type to use for any authentications. Setting this
     * will result in the addition of WS-Security UsernamePassword tokens to any
     * outgoing request containing the specified username and password.
     *
     * @param wssPasswordType the wss-password type to use, either 'Text' or 'Digest'
     */

    public void setWssPasswordType(String wssPasswordType) {
        this.wssPasswordType = wssPasswordType;
    }

    /**
     * Sets the username to use for any authentications
     *
     * @param username the username to use for any authentications
     */

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    /**
     * Sets the endpoint to use for all test requests
     *
     * @param endpoint the endpoint to use for all test requests
     */

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint.trim();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public String getWssPasswordType() {
        return wssPasswordType;
    }

    protected void prepareRequestStep(HttpRequestTestStep requestStep) {
        AbstractHttpRequest<?> httpRequest = requestStep.getHttpRequest();
        if (StringUtils.hasContent(endpoint)) {
            httpRequest.setEndpoint(endpoint);
        } else if (StringUtils.hasContent(host)) {
            try {
                String ep = Tools.replaceHost(httpRequest.getEndpoint(), host);
                httpRequest.setEndpoint(ep);
            } catch (Exception e) {
                log.error("Failed to set host on endpoint", e);
            }
        }

        if (StringUtils.hasContent(username)) {
            httpRequest.setUsername(username);
        }

        if (StringUtils.hasContent(password)) {
            httpRequest.setPassword(password);
        }

        if (StringUtils.hasContent(domain)) {
            httpRequest.setDomain(domain);
        }

        if (httpRequest instanceof WsdlRequest) {

            if (wssPasswordType != null && wssPasswordType.length() > 0) {
                ((WsdlRequest) httpRequest)
                        .setWssPasswordType(wssPasswordType.equals("Digest") ? WsdlTestRequest.PW_TYPE_DIGEST
                                : WsdlTestRequest.PW_TYPE_TEXT);
            }
        }
    }

    public void beforeRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

    public final void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

    public void beforeStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep currentStep) {
        if (currentStep instanceof HttpRequestTestStep) {
            prepareRequestStep((HttpRequestTestStep) currentStep);
        } else if (currentStep instanceof WsdlRunTestCaseTestStep) {
            ((WsdlRunTestCaseTestStep) currentStep).addTestRunListener(this);
        }
    }

    public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
        TestStep currentStep = runContext.getCurrentStep();
        if (currentStep instanceof WsdlRunTestCaseTestStep) {
            ((WsdlRunTestCaseTestStep) currentStep).removeTestRunListener(this);
        }
    }

    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
    }

}
