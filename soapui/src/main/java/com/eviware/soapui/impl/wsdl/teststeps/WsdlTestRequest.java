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
import com.eviware.soapui.config.AttachmentConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertableConfig;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertionsSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WsdlRequest extension that adds WsdlAssertions
 *
 * @author Ole.Matzura
 */

public class WsdlTestRequest extends WsdlRequest implements Assertable, TestRequest {
    public static final String RESPONSE_PROPERTY = WsdlTestRequest.class.getName() + "@response";
    public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";

    private static ImageIcon validRequestIcon;
    private static ImageIcon failedRequestIcon;
    private static ImageIcon disabledRequestIcon;
    private static ImageIcon unknownRequestIcon;

    private AssertionStatus currentStatus;
    private final WsdlTestRequestStep testStep;

    private AssertionsSupport assertionsSupport;
    private WsdlResponseMessageExchange messageExchange;
    private final boolean forLoadTest;
    private PropertyChangeNotifier notifier;

    public WsdlTestRequest(WsdlOperation operation, WsdlRequestConfig callConfig, WsdlTestRequestStep testStep,
                           boolean forLoadTest) {
        super(operation, callConfig, forLoadTest);
        this.forLoadTest = forLoadTest;

        setSettings(new XmlBeansSettingsImpl(this, testStep.getSettings(), callConfig.getSettings()));

        this.testStep = testStep;

        initAssertions();

        if (!forLoadTest) {
            initIcons();
        }
    }

    public WsdlTestCase getTestCase() {
        return testStep.getTestCase();
    }

    public ModelItem getParent() {
        return getTestStep();
    }

    protected void initIcons() {
        if (validRequestIcon == null) {
            validRequestIcon = UISupport.createImageIcon("/valid_soap_request_step.png");
        }

        if (failedRequestIcon == null) {
            failedRequestIcon = UISupport.createImageIcon("/invalid_soap_request_step.png");
        }

        if (unknownRequestIcon == null) {
            unknownRequestIcon = UISupport.createImageIcon("/soap_request_step.png");
        }

        if (disabledRequestIcon == null) {
            disabledRequestIcon = UISupport.createImageIcon("/disabled_request.gif");
        }
    }

    @Override
    protected RequestIconAnimator<?> initIconAnimator() {
        return new TestRequestIconAnimator(this);
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

        if (getResponse() instanceof JMSResponse) {
            messageExchange = getResponse() == null ? null : new WsdlResponseMessageExchange(this) {
                @Override
                public boolean hasResponse() {// JMS tweak
                    String responseContent = getResponseContent();
                    return responseContent != null;
                }
            };
        } else {
            messageExchange = getResponse() == null ? null : new WsdlResponseMessageExchange(this);
        }

        if (messageExchange != null) {
            // assert!
            for (WsdlMessageAssertion assertion : assertionsSupport.getAssertionList()) {
                assertion.assertResponse(messageExchange, context);
            }
        }

        notifier.notifyChange();
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
                assertion.assertResponse(new WsdlResponseMessageExchange(this), new WsdlTestRunContext(testStep));
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
        WsdlMessageAssertion assertion = getAssertionAt(ix);
        PropertyChangeNotifier notifier = new PropertyChangeNotifier();

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
            /*
			 * if( !messageExchange.hasResponse() &&
			 * getOperation().isBidirectional() && !isWsaEnabled() ) {
			 * currentStatus = AssertionStatus.FAILED; }
			 */
        } else {
            return currentStatus;
        }

        int cnt = getAssertionCount();
        if (cnt == 0) {
            return currentStatus;
        }

        boolean hasEnabled = false;

        for (int c = 0; c < cnt; c++) {
            if (!getAssertionAt(c).isDisabled()) {
                hasEnabled = true;
            }

            if (getAssertionAt(c).getStatus() == AssertionStatus.FAILED) {
                currentStatus = AssertionStatus.FAILED;
                break;
            }
        }

        if (currentStatus == AssertionStatus.UNKNOWN && hasEnabled) {
            currentStatus = AssertionStatus.VALID;
        }

        return currentStatus;
    }

    @Override
    public ImageIcon getIcon() {
        if (forLoadTest || getIconAnimator() == null) {
            return null;
        }

        TestMonitor testMonitor = SoapUI.getTestMonitor();
        if (testMonitor != null && (testMonitor.hasRunningLoadTest(testStep.getTestCase()))) {
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

    public void addAssertionsListener(AssertionsListener listener) {
        assertionsSupport.addAssertionsListener(listener);
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        assertionsSupport.removeAssertionsListener(listener);
    }

    /**
     * Called when a testrequest is moved in a testcase
     */

    @Override
    public void updateConfig(WsdlRequestConfig request) {
        super.updateConfig(request);

        assertionsSupport.refresh();

        List<AttachmentConfig> attachmentConfigs = getConfig().getAttachmentList();
        for (int i = 0; i < attachmentConfigs.size(); i++) {
            AttachmentConfig config = attachmentConfigs.get(i);
            getAttachmentsList().get(i).updateConfig(config);
        }

    }

    @Override
    public void release() {
        super.release();
        assertionsSupport.release();
    }

    public String getAssertableContentAsXml() {
        return getAssertableContent();
    }

    public String getAssertableContent() {
        return getResponse() == null ? null : getResponse().getContentAsString();
    }

    public WsdlTestRequestStep getTestStep() {
        return testStep;
    }

    public WsdlInterface getInterface() {
        return getOperation().getInterface();
    }

    protected static class TestRequestIconAnimator extends RequestIconAnimator<WsdlTestRequest> {
        public TestRequestIconAnimator(WsdlTestRequest modelItem) {
            super(modelItem, "/soap_request.png", "/soap_request.png", 4);
        }

        @Override
        public boolean beforeSubmit(Submit submit, SubmitContext context) {
            if (SoapUI.getTestMonitor() != null
                    && (SoapUI.getTestMonitor().hasRunningLoadTest(getTarget().getTestCase()))) {
                return true;
            }

            return super.beforeSubmit(submit, context);
        }

        @Override
        public void afterSubmit(Submit submit, SubmitContext context) {
            if (submit.getRequest() == getTarget()) {
                stop();
            }
        }
    }

    public AssertableType getAssertableType() {
        return AssertableType.BOTH;
    }

    public String getInterfaceName() {
        return testStep.getInterfaceName();
    }

    public String getOperationName() {
        return testStep.getOperationName();
    }

    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return assertionsSupport.cloneAssertion(source, name);
    }

    public WsdlMessageAssertion importAssertion(WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
                                                String newName) {
        return assertionsSupport.importAssertion(source, overwrite, createCopy, newName);
    }

    public List<TestAssertion> getAssertionList() {
        return new ArrayList<TestAssertion>(assertionsSupport.getAssertionList());
    }

    public WsdlMessageAssertion getAssertionByName(String name) {
        return assertionsSupport.getAssertionByName(name);
    }

    public ModelItem getModelItem() {
        return testStep;
    }

    public Map<String, TestAssertion> getAssertions() {
        return assertionsSupport.getAssertions();
    }

    public String getDefaultAssertableContent() {
        return getOperation().createResponse(true);
    }

    public void resolve(ResolveContext<?> context) {
        super.resolve(context);

        assertionsSupport.resolve(context);
    }

    public boolean isDiscardResponse() {
        return getSettings().getBoolean("discardResponse");
    }

    public void setDiscardResponse(boolean discardResponse) {
        getSettings().setBoolean("discardResponse", discardResponse);
    }
}
