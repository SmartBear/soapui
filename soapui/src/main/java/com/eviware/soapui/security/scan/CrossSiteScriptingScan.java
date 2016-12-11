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

package com.eviware.soapui.security.scan;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CrossSiteScriptingScanConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.teststeps.RestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.assertion.CrossSiteScriptAssertion;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JStringListFormField;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 *
 * @author nebojsa.tasic
 */

public class CrossSiteScriptingScan extends AbstractSecurityScanWithProperties {

    public static final String TYPE = "CrossSiteScriptingScan";
    public static final String NAME = "Cross Site Scripting";
    public static final String PARAMETER_EXPOSURE_SCAN_CONFIG = "CrossSiteScriptingScanConfig";
    public static final String TEST_CASE_RUNNER = "testCaseRunner";
    public static final String TEST_STEP = "testStep";
    private CrossSiteScriptingScanConfig cssConfig;
    StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

    List<String> defaultParameterExposureStrings = new ArrayList<String>();
    private JFormDialog dialog;

    public CrossSiteScriptingScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(testStep, config, parent, icon);
        if (config.getConfig() == null || !(config.getConfig() instanceof CrossSiteScriptingScanConfig)) {
            initConfig();
        } else {
            cssConfig = (CrossSiteScriptingScanConfig) getConfig().getConfig();
        }

    }

    private void initDefaultVectors() {
        try {
            InputStream in = SoapUI.class.getResourceAsStream("/com/eviware/soapui/resources/security/XSS-vectors.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                defaultParameterExposureStrings.add(strLine);
            }
            in.close();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

    }

    @Override
    protected void initAssertions() {
        super.initAssertions();

        if (assertionsSupport.getAssertionByName(CrossSiteScriptAssertion.LABEL) == null) {
            assertionsSupport.addWsdlAssertion(CrossSiteScriptAssertion.LABEL);
        }
    }

    private void initConfig() {
        initDefaultVectors();
        getConfig().setConfig(CrossSiteScriptingScanConfig.Factory.newInstance());
        cssConfig = (CrossSiteScriptingScanConfig) getConfig().getConfig();

        cssConfig.setParameterExposureStringsArray(defaultParameterExposureStrings
                .toArray(new String[defaultParameterExposureStrings.size()]));
    }

    @Override
    public void updateSecurityConfig(SecurityScanConfig config) {
        super.updateSecurityConfig(config);

        if (cssConfig != null) {
            cssConfig = (CrossSiteScriptingScanConfig) getConfig().getConfig();
        }
    }

    @Override
    protected void execute(SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context) {
        sendToContext(context, testStep, securityTestRunner);
        PropertyMutation mutation = PropertyMutation.popMutation(context);
        if (mutation != null) {
            if (testStep instanceof RestTestRequestStep) {
                RestRequestStepResult message = (RestRequestStepResult) mutation.getTestStep().run(
                        (TestCaseRunner) securityTestRunner, context);
                message.setRequestContent("");
                createMessageExchange(mutation.getMutatedParameters(), message, context);
            } else {
                MessageExchange message = (MessageExchange) mutation.getTestStep().run(
                        (TestCaseRunner) securityTestRunner, context);
                if (message instanceof WsdlTestRequestStepResult) {
                    ((WsdlTestRequestStepResult) message).setRequestContent("", false);
                }

                createMessageExchange(mutation.getMutatedParameters(), message, context);
            }
        }
    }

    private void sendToContext(SecurityTestRunContext context, TestStep testStep, SecurityTestRunner securityTestRunner) {
        context.put(TEST_CASE_RUNNER, securityTestRunner);
        context.put(TEST_STEP, testStep);
    }

    private void removeFromContext(SecurityTestRunContext context) {
        context.remove(TEST_CASE_RUNNER);
        context.remove(TEST_STEP);
    }

    @Override
    public JComponent getComponent() {
        JPanel p = UISupport.createEmptyPanel(5, 75, 0, 5);
        p.add(new JLabel("Strings for Cross Site Scripting can be configured under Advanced Settings"));
        return p;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean hasNext(TestStep testStep, SecurityTestRunContext context) {
        if (!context.hasProperty(PropertyMutation.REQUEST_MUTATIONS_STACK)) {
            Stack<PropertyMutation> requestMutationsList = new Stack<PropertyMutation>();
            context.put(PropertyMutation.REQUEST_MUTATIONS_STACK, requestMutationsList);
            context.put(PARAMETER_EXPOSURE_SCAN_CONFIG, cssConfig);
            try {
                extractMutations(testStep, context);
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            return checkIfEmptyStack(context);
        }

        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(PropertyMutation.REQUEST_MUTATIONS_STACK);
        if (stack.empty()) {
            context.remove(PropertyMutation.REQUEST_MUTATIONS_STACK);
            context.remove(PARAMETER_EXPOSURE_SCAN_CONFIG);
            removeFromContext(context);
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean checkIfEmptyStack(SecurityTestRunContext context) {
        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(PropertyMutation.REQUEST_MUTATIONS_STACK);
        if (stack.empty()) {
            return false;
        } else {
            return true;
        }
    }

    private void extractMutations(TestStep testStep, SecurityTestRunContext context) {
        strategy = getExecutionStrategy().getStrategy();
        for (String value : cssConfig.getParameterExposureStringsList()) {
            // property expansion support
            value = context.expand(value);

            PropertyMutation allAtOncePropertyMutation = new PropertyMutation();
            TestStep testStepCopy = null;
            XmlObjectTreeModel model = null;
            List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
            StringToStringMap stsmap = new StringToStringMap();
            for (SecurityCheckedParameter scp : scpList) {

                if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                    stsmap = new StringToStringMap();
                    model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                    testStepCopy = SecurityTestRunnerImpl.cloneTestStepForSecurityScan((WsdlTestStep) testStep);
                } else {
                    if (model == null) {
                        model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                    }
                    if (testStepCopy == null) {
                        testStepCopy = SecurityTestRunnerImpl.cloneTestStepForSecurityScan((WsdlTestStep) testStep);
                    }
                }

                // if parameter is xml
                if (scp.isChecked() && scp.getXpath().trim().length() > 0) {
                    XmlTreeNode[] treeNodes = null;

                    treeNodes = model.selectTreeNodes(context.expand(scp.getXpath()));

                    if (treeNodes.length > 0) {
                        XmlTreeNode mynode = treeNodes[0];

                        // work only for simple types
                        if (mynode.isLeaf()) {
                            mynode.setValue(1, value);

                            if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                                PropertyMutation oneByOnePropertyMutation = new PropertyMutation();
                                oneByOnePropertyMutation.setPropertyName(scp.getName());
                                oneByOnePropertyMutation.setPropertyValue(unescapEscaped(model.getXmlObject().toString()));
                                stsmap.put(scp.getLabel(), mynode.getNodeText());
                                oneByOnePropertyMutation.setMutatedParameters(stsmap);
                                oneByOnePropertyMutation.updateRequestProperty(testStepCopy);
                                oneByOnePropertyMutation.setTestStep(testStepCopy);
                                oneByOnePropertyMutation.addMutation(context);
                            } else {
                                allAtOncePropertyMutation.setPropertyName(scp.getName());
                                allAtOncePropertyMutation.setPropertyValue(unescapEscaped(model.getXmlObject().toString()));
                                stsmap.put(scp.getLabel(), mynode.getNodeText());
                                allAtOncePropertyMutation.setMutatedParameters(stsmap);
                                allAtOncePropertyMutation.updateRequestProperty(testStepCopy);
                                allAtOncePropertyMutation.setTestStep(testStepCopy);

                            }
                        }
                    }
                }
                // non xml parameter
                else {
                    if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                        PropertyMutation oneByOnePropertyMutation = new PropertyMutation();
                        oneByOnePropertyMutation.setPropertyName(scp.getName());
                        oneByOnePropertyMutation.setPropertyValue(value);
                        stsmap.put(scp.getLabel(), value);
                        oneByOnePropertyMutation.setMutatedParameters(stsmap);
                        oneByOnePropertyMutation.updateRequestProperty(testStepCopy);
                        oneByOnePropertyMutation.setTestStep(testStepCopy);
                        oneByOnePropertyMutation.addMutation(context);
                    } else {
                        allAtOncePropertyMutation.setPropertyName(scp.getName());
                        allAtOncePropertyMutation.setPropertyValue(value);
                        stsmap.put(scp.getLabel(), value);
                        allAtOncePropertyMutation.setMutatedParameters(stsmap);
                        allAtOncePropertyMutation.updateRequestProperty(testStepCopy);
                        allAtOncePropertyMutation.setTestStep(testStepCopy);
                    }
                }
            }

            if (strategy.equals(StrategyTypeConfig.ALL_AT_ONCE)) {
                allAtOncePropertyMutation.addMutation(context);
            }
        }
    }

    private String unescapEscaped(String value) {
        return value.replaceAll("&lt;", "<");
    }

    @Override
    public String getConfigDescription() {
        return "Configures parameter exposure security scan";
    }

    @Override
    public String getConfigName() {
        return "Cross Site Scripting Scan";
    }

    @Override
    public String getHelpURL() {
        return "http://soapui.org/Security/cross-site-scripting.html";
    }

    @Override
    public JComponent getAdvancedSettingsPanel() {
        dialog = (JFormDialog) ADialogBuilder.buildDialog(AdvancedSettings.class);
        JStringListFormField stringField = (JStringListFormField) dialog
                .getFormField(AdvancedSettings.PARAMETER_EXPOSURE_STRINGS);
        stringField.setOptions(cssConfig.getParameterExposureStringsList().toArray());
        stringField.setProperty("dimension", new Dimension(470, 150));
        stringField.getComponent().addPropertyChangeListener("options", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String[] newOptions = (String[]) evt.getNewValue();
                String[] oldOptions = (String[]) evt.getOldValue();
                // added
                if (newOptions.length > oldOptions.length) {
                    // new element is always added to the end
                    String[] newValue = (String[]) evt.getNewValue();
                    String itemToAdd = newValue[newValue.length - 1];
                    cssConfig.addParameterExposureStrings(itemToAdd);
                }
                // removed
                if (newOptions.length < oldOptions.length) {
                    /*
					 * items with same index should me same. first one in oldOptions
					 * that does not match is element that is removed.
					 */
                    for (int cnt = 0; cnt < oldOptions.length; cnt++) {
                        if (cnt < newOptions.length) {
                            if (newOptions[cnt] != oldOptions[cnt]) {
                                cssConfig.removeParameterExposureStrings(cnt);
                                break;
                            }
                        } else {
                            // this is border case, last lement in array is removed.
                            cssConfig.removeParameterExposureStrings(oldOptions.length - 1);
                        }
                    }
                }
            }
        });

        return dialog.getPanel();
    }

    @Override
    public void release() {
        if (dialog != null) {
            dialog.release();
        }

        super.release();
    }

    @AForm(description = "Cross Site Scripting", name = "Cross Site Scripting")
    protected interface AdvancedSettings {

        @AField(description = "Cross Site Scripting Vectors", name = "###Cross Site Scripting", type = AFieldType.STRINGLIST)
        public final static String PARAMETER_EXPOSURE_STRINGS = "###Cross Site Scripting";

    }
}
