/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.IconAnimator;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.AnimatableItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JdbcRequest extends AbstractModelItem implements Assertable, TestRequest, AnimatableItem {
    private final JdbcRequestTestStep testStep;
    private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
    private JdbcResponse response;
    final static Logger logger = LogManager.getLogger(JdbcRequest.class);
    private ImageIcon validRequestIcon;
    private ImageIcon failedRequestIcon;
    private ImageIcon disabledRequestIcon;
    private ImageIcon unknownRequestIcon;
    private RequestIconAnimator<?> iconAnimator;
    private boolean forLoadTest;
    private AssertionStatus currentStatus;

    public JdbcRequest(JdbcRequestTestStep testStep, boolean forLoadTest) {
        this.testStep = testStep;

        if (!forLoadTest) {
            initIcons();
        }
    }

    public void addSubmitListener(SubmitListener listener) {
        submitListeners.add(listener);
    }

    public boolean dependsOn(ModelItem modelItem) {
        return ModelSupport.dependsOn(testStep, modelItem);
    }

    public Attachment[] getAttachments() {
        return null;
    }

    public String getEncoding() {
        return null;
    }

    public String getEndpoint() {
        return null;
    }

    public Operation getOperation() {
        return null;
    }

    public String getRequestContent() {
        return ((JdbcRequestTestStep) testStep).getQuery();
    }

    public MessagePart[] getRequestParts() {
        return null;
    }

    public MessagePart[] getResponseParts() {
        return null;
    }

    public String getTimeout() {
        return testStep.getQueryTimeout();
    }

    public void removeSubmitListener(SubmitListener listener) {
        submitListeners.remove(listener);
    }

    public void setEncoding(String string) {
    }

    public void setEndpoint(String string) {
    }

    public JdbcSubmit submit(SubmitContext submitContext, boolean async) throws SubmitException {
        return new JdbcSubmit(this, submitContext, async);
    }

    public List<? extends ModelItem> getChildren() {
        return null;
    }

    public String getDescription() {
        return testStep.getDescription();
    }

    // public ImageIcon getIcon()
    // {
    // return testStep.getIcon();
    // }
    //
    public String getId() {
        return testStep.getId();
    }

    public String getName() {
        return testStep.getName();
    }

    public ModelItem getParent() {
        return testStep.getParent();
    }

    public Settings getSettings() {
        return testStep.getSettings();
    }

    public SubmitListener[] getSubmitListeners() {
        return submitListeners.toArray(new SubmitListener[submitListeners.size()]);
    }

    public JdbcRequestTestStep getTestStep() {
        return testStep;
    }

    public WsdlMessageAssertion importAssertion(WsdlMessageAssertion source, boolean overwrite, boolean createCopy,
                                                String newName) {
        return testStep.importAssertion(source, overwrite, createCopy, newName);
    }

    public TestAssertion addAssertion(String selection) {
        return testStep.addAssertion(selection);
    }

    public void addAssertionsListener(AssertionsListener listener) {
        testStep.addAssertionsListener(listener);
    }

    public TestAssertion cloneAssertion(TestAssertion source, String name) {
        return testStep.cloneAssertion(source, name);
    }

    public String getAssertableContentAsXml() {
        return testStep.getAssertableContentAsXml();
    }

    public String getAssertableContent() {
        return testStep.getAssertableContent();
    }

    public AssertableType getAssertableType() {
        return testStep.getAssertableType();
    }

    public TestAssertion getAssertionAt(int c) {
        return testStep.getAssertionAt(c);
    }

    public TestAssertion getAssertionByName(String name) {
        return testStep.getAssertionByName(name);
    }

    public int getAssertionCount() {
        return testStep.getAssertionCount();
    }

    public List<TestAssertion> getAssertionList() {
        return testStep.getAssertionList();
    }

    public AssertionStatus getAssertionStatus() {
        currentStatus = AssertionStatus.UNKNOWN;

        if (getResponse() == null) {
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

    public Map<String, TestAssertion> getAssertions() {
        return testStep.getAssertions();
    }

    public String getDefaultAssertableContent() {
        return testStep.getDefaultAssertableContent();
    }

    public Interface getInterface() {
        return testStep.getInterface();
    }

    public ModelItem getModelItem() {
        return testStep.getModelItem();
    }

    public TestAssertion moveAssertion(int ix, int offset) {
        return testStep.moveAssertion(ix, offset);
    }

    public void removeAssertion(TestAssertion assertion) {
        testStep.removeAssertion(assertion);
    }

    public void removeAssertionsListener(AssertionsListener listener) {
        testStep.removeAssertionsListener(listener);
    }

    public void setResponse(JdbcResponse response) {
        this.response = response;
    }

    public JdbcResponse getResponse() {
        return response;
    }

    public void initIcons() {
        if (validRequestIcon == null) {
            validRequestIcon = UISupport.createImageIcon("/valid_jdbc_request_step.png");
        }

        if (failedRequestIcon == null) {
            failedRequestIcon = UISupport.createImageIcon("/invalid_jdbc_request_step.png");
        }

        if (unknownRequestIcon == null) {
            unknownRequestIcon = UISupport.createImageIcon("/jdbc_request_step.png");
        }

        if (disabledRequestIcon == null) {
            disabledRequestIcon = UISupport.createImageIcon("/disabled_jdbc_request_step.png");
        }

        setIconAnimator(new RequestIconAnimator<JdbcRequest>(this, "/jdbc_request.png", "/jdbc_request.png", 4));
    }

    protected RequestIconAnimator<?> initIconAnimator() {
        return new RequestIconAnimator<JdbcRequest>(this, "/jdbc_request.png", "/exec_jdbc_request", 4);
    }

    public static class RequestIconAnimator<T extends JdbcRequest> extends IconAnimator<T> implements
            SubmitListener {
        public RequestIconAnimator(T modelItem, String baseIcon, String baseAnimateIcon, int iconCount) {
            super(modelItem, baseIcon, baseAnimateIcon, iconCount);
        }

        public boolean beforeSubmit(Submit submit, SubmitContext context) {
            if (isEnabled() && submit.getRequest() == getTarget()) {
                start();
            }
            return true;
        }

        public void afterSubmit(Submit submit, SubmitContext context) {
            if (submit.getRequest() == getTarget()) {
                stop();
            }
        }
    }

    public RequestIconAnimator<?> getIconAnimator() {
        return iconAnimator;
    }

    public void setIconAnimator(RequestIconAnimator<?> iconAnimator) {
        if (this.iconAnimator != null) {
            removeSubmitListener(this.iconAnimator);
        }

        this.iconAnimator = iconAnimator;
        addSubmitListener(this.iconAnimator);
    }

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

    @Override
    public void setIcon(ImageIcon icon) {
        getTestStep().setIcon(icon);
    }

    public String getPassword() {
        return null;
    }

    public String getUsername() {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    public boolean isDiscardResponse() {
        return getSettings().getBoolean("discardResponse");
    }

    public void setDiscardResponse(boolean discardResponse) {
        getSettings().setBoolean("discardResponse", discardResponse);
    }
}
