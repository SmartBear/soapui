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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpTestRequest extends HttpRequest implements HttpTestRequestInterface<HttpRequestConfig> {
    private final boolean forLoadTest;
    private HttpTestRequestStep testStep;
    private AssertionsSupport assertionsSupport;
    private HttpResponseMessageExchange messageExchange;
    private PropertyChangeNotifier notifier;
    private AssertionStatus currentStatus;

    private ImageIcon validRequestIcon;
    private ImageIcon failedRequestIcon;
    private ImageIcon disabledRequestIcon;
    private ImageIcon unknownRequestIcon;

    protected HttpTestRequest(HttpRequestConfig config, HttpTestRequestStep testStep, boolean forLoadTest) {
        super(config, forLoadTest);
        this.forLoadTest = forLoadTest;

        setSettings(new XmlBeansSettingsImpl(this, testStep.getSettings(), config.getSettings()));

        this.testStep = testStep;

        initAssertions();
        if (!forLoadTest) {
            initIcons();
        }
    }

    protected void initIcons() {
        validRequestIcon = UISupport.createImageIcon("/valid_http_request_step.png");
        failedRequestIcon = UISupport.createImageIcon("/invalid_http_request_step.png");
        unknownRequestIcon = UISupport.createImageIcon("/http_request_step.png");
        disabledRequestIcon = UISupport.createImageIcon("/disabled_http_request_step.png");

        setIconAnimator(new RequestIconAnimator<HttpTestRequest>(this, "/http_request_step.png", "/http_request.png", 4));
    }

    private void initAssertions() {
        assertionsSupport = new AssertionsSupport(testStep, new AssertableConfig() {
            public TestAssertionConfig addNewAssertion() {
                return getConfig().addNewAssertion();
            }

            public List<TestAssertionConfig> getAssertionList() {
                return getConfig().getAssertionList();
            }

            public void removeAssertion(int ix) {
                getConfig().removeAssertion(ix);
            }

            public TestAssertionConfig insertAssertion(TestAssertionConfig source, int ix) {
                TestAssertionConfig conf = getConfig().insertNewAssertion(ix);
                conf.set(source);
                return conf;
            }
        });
    }

    public int getAssertionCount() {
        return assertionsSupport.getAssertionCount();
    }

    public WsdlMessageAssertion getAssertionAt(int c) {
        return assertionsSupport.getAssertionAt(c);
    }

    public void setResponse(HttpResponse response, SubmitContext context) {
        super.setResponse(response, context);
        assertResponse(context);
    }

    public void assertResponse(SubmitContext context) {
        if (notifier == null) {
            notifier = new PropertyChangeNotifier();
        }

        messageExchange = getResponse() == null ? null : new HttpResponseMessageExchange(this);

        if (messageExchange != null) {
            // assert!
            for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()) {
                assertion.assertResponse(messageExchange, context);
            }
        }

        notifier.notifyChange();
    }

    @Override
    public ImageIcon getIcon() {
        if (forLoadTest || getIconAnimator() == null) {
            return null;
        }

        TestMonitor testMonitor = SoapUI.getTestMonitor();
        if (testMonitor != null
                && (testMonitor.hasRunningLoadTest(getTestStep().getTestCase()) || testMonitor
                .hasRunningSecurityTest(getTestStep().getTestCase()))) {
            return disabledRequestIcon;
        }

        ImageIcon icon = getIconAnimator().getIcon();
        if (icon == getIconAnimator().getBaseIcon()) {
            AssertionStatus status = getAssertionStatus();
            if (status == AssertionStatus.VALID) {
                return validRequestIcon;
            } else if (status == AssertionStatus.FAILED) {
                return failedRequestIcon;
            } else if (status == AssertionStatus.UNKNOWN) {
                return unknownRequestIcon;
            }
        }

        return icon;
    }

    private class PropertyChangeNotifier {
        private AssertionStatus oldStatus;
        private ImageIcon oldIcon;

        public PropertyChangeNotifier() {
            oldStatus = getAssertionStatus();
            oldIcon = getIcon();
        }

        public void notifyChange() {
            AssertionStatus newStatus = getAssertionStatus();
            ImageIcon newIcon = getIcon();

            if (oldStatus != newStatus) {
                notifyPropertyChanged(STATUS_PROPERTY, oldStatus, newStatus);
            }

            if (oldIcon != newIcon) {
                notifyPropertyChanged(ICON_PROPERTY, oldIcon, getIcon());
            }

            oldStatus = newStatus;
            oldIcon = newIcon;
        }
    }

    public WsdlMessageAssertion addAssertion(String assertionLabel) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            WsdlMessageAssertion assertion = assertionsSupport.addWsdlAssertion(assertionLabel);
            if (assertion == null) {
                return null;
            }

            if (getResponse() != null) {
                assertion.assertResponse(new HttpResponseMessageExchange(this), new WsdlTestRunContext(testStep));
                notifier.notifyChange();
            }

            return assertion;
        } catch (Exception e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public void removeAssertion(TestAssertion assertion) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

        try {
            assertionsSupport.removeAssertion((WsdlMessageAssertion) assertion);

        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    public TestAssertion moveAssertion(int ix, int offset) {
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();
        WsdlMessageAssertion assertion = getAssertionAt(ix);
        try {
            return assertionsSupport.moveAssertion(ix, offset);
        } finally {
            ((WsdlMessageAssertion) assertion).release();
            notifier.notifyChange();
        }
    }

    public AssertionStatus getAssertionStatus() {
        currentStatus = AssertionStatus.UNKNOWN;

        if (messageExchange != null) {
            if (!messageExchange.hasResponse() && getOperation() != null && getOperation().isBidirectional()) {
                currentStatus = AssertionStatus.FAILED;
            }
        } else {
            return currentStatus;
        }

        int cnt = getAssertionCount();
        if (cnt == 0) {
            return currentStatus;
        }

        for (int c = 0; c < cnt; c++) {
            if (getAssertionAt(c).getStatus() == AssertionStatus.FAILED) {
                currentStatus = AssertionStatus.FAILED;
                break;
            }
        }

        if (currentStatus == AssertionStatus.UNKNOWN) {
            currentStatus = AssertionStatus.VALID;
        }

        return currentStatus;
    }

    public void addAssertionsListener(AssertionsListener listener) {
        assertionsSupport.addAssertionsListener(listener);
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    public String getResponseContentAsString() {
        return getResponse() == null ? null : getResponse().getContentAsString();
    }

    public String getAssertableContentAsXml() {
        return getResponseContentAsXml();
    }

    public String getAssertableContent() {
        return getResponseContentAsString();
    }

    public HttpTestRequestStep getTestStep() {
        return testStep;
    }

    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return assertionsSupport.cloneAssertion(source, name);
    }

    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    public WsdlMessageAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    public Map<String, TestAssertion> getAssertions() {
        return assertionsSupport.getAssertions();
    }

    public String getDefaultAssertableContent() {
        return "";
    }

    public AssertableType getAssertableType() {
        return AssertableType.RESPONSE;
    }

    public Interface getInterface() {
        return null;
    }

    public void updateConfig(HttpRequestConfig request) {
        super.updateConfig(request);

        assertionsSupport.refresh();
    }

    public WsdlTestCase getTestCase() {
        return testStep.getTestCase();
    }

    public ModelItem getParent() {
        return getTestStep();
    }

    public WsdlMessageAssertion importAssertion(WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
                                                String newName) {
        return assertionsSupport.importAssertion(source, overwrite, createCopy, newName);
    }

    public boolean isDiscardResponse() {
        return getSettings().getBoolean("discardResponse");
    }

    public void setDiscardResponse(boolean discardResponse) {
        getSettings().setBoolean("discardResponse", discardResponse);
    }
}
