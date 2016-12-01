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

package com.eviware.soapui.security.assertion;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CrossSiteScriptingScanConfig;
import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.security.scan.CrossSiteScriptingScan;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import org.apache.xmlbeans.XmlObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CrossSiteScriptAssertion extends WsdlMessageAssertion implements ResponseAssertion {
    public static final String ID = "CrosSiteScript";
    public static final String LABEL = "Cross Site Scripting Detection";
    public static final String DESCRIPTION = "Cross Site Scripting....assertion for...";
    public static final String GROOVY_SCRIPT = "groovyScript";
    public static final String CHECK_RESPONSE = "checkResponse";
    public static final String CHECK_SEPARATE_HTML = "checkSeparateHTML";

    private XFormDialog dialog;
    private String script;
    private GroovyEditorModel groovyEditorModel;
    private SoapUIScriptEngine scriptEngine;

    MessageExchange messageExchange;
    SubmitContext context;

    private boolean checkResponse;
    private boolean checkSeparateHTML;

    public CrossSiteScriptAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, false, true, false, true);
        groovyEditorModel = new GroovyEditorModel(this);
        init();
        scriptEngine = SoapUIScriptEngineRegistry.create(this);
    }

    private void init() {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        script = reader.readString(GROOVY_SCRIPT, "");
        checkResponse = reader.readBoolean(CHECK_RESPONSE, true);
        checkSeparateHTML = reader.readBoolean(CHECK_SEPARATE_HTML, false);
        groovyEditorModel.setScript(script);
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        TestStep testStep = (TestStep) context.getProperty(CrossSiteScriptingScan.TEST_STEP);
        testStep = SecurityTestRunnerImpl.cloneTestStepForSecurityScan((WsdlTestStep) testStep);
        SecurityTestRunner securityTestRunner = (SecurityTestRunner) context
                .getProperty(CrossSiteScriptingScan.TEST_CASE_RUNNER);

        List<String> urls = submitScript(messageExchange, context);

        CrossSiteScriptingScanConfig parameterExposureCheckConfig = (CrossSiteScriptingScanConfig) context
                .getProperty(CrossSiteScriptingScan.PARAMETER_EXPOSURE_SCAN_CONFIG);

        List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
        boolean throwExceptionCheckResponse = false;

        if (checkResponse) {
            throwExceptionCheckResponse = checkResponse(messageExchange, context, parameterExposureCheckConfig,
                    assertionErrorList);
        }

        boolean throwExceptionCheckSeparateHTML = false;
        if (checkSeparateHTML) {
            throwExceptionCheckSeparateHTML = checkSeparateHTML(messageExchange, context, testStep, securityTestRunner,
                    urls, parameterExposureCheckConfig, assertionErrorList);
        }

        if (throwExceptionCheckResponse || throwExceptionCheckSeparateHTML) {
            throw new AssertionException(assertionErrorList.toArray(new AssertionError[assertionErrorList.size()]));
        }

        return "OK";
    }

    private boolean checkSeparateHTML(MessageExchange messageExchange, SubmitContext context, TestStep testStep,
                                      SecurityTestRunner securityTestRunner, List<String> urls,
                                      CrossSiteScriptingScanConfig parameterExposureCheckConfig, List<AssertionError> assertionErrorList) {
        boolean throwException = false;
        for (String url : urls) {
            HttpTestRequestStep httpRequest = createHttpRequest((WsdlTestStep) testStep, url);
            MessageExchange messageExchange2 = (MessageExchange) httpRequest.run((TestCaseRunner) securityTestRunner,
                    (SecurityTestRunContext) context);

            for (String value : parameterExposureCheckConfig.getParameterExposureStringsList()) {
                value = context.expand(value);// property expansion support
                String match = SecurityScanUtil.contains(context, new String(messageExchange2.getRawResponseData()),
                        value, false);
                if (match != null) {
                    String shortValue = value.length() > 25 ? value.substring(0, 22) + "... " : value;
                    String message = "XSS content sent in request '" + shortValue + "' is exposed in response on link "
                            + url + " . Possibility for XSS script attack in: " + messageExchange.getModelItem().getName();
                    assertionErrorList.add(new AssertionError(message));
                    throwException = true;
                }
            }
        }
        return throwException;
    }

    private boolean checkResponse(MessageExchange messageExchange, SubmitContext context,
                                  CrossSiteScriptingScanConfig parameterExposureCheckConfig, List<AssertionError> assertionErrorList) {
        boolean throwException = false;
        for (String value : parameterExposureCheckConfig.getParameterExposureStringsList()) {
            value = context.expand(value);// property expansion support
            String match = SecurityScanUtil.contains(context, new String(messageExchange.getRawResponseData()), value,
                    false);
            if (match != null) {
                String shortValue = value.length() > 25 ? value.substring(0, 22) + "... " : value;
                String message = "Content that is sent in request '" + shortValue
                        + "' is exposed in response. Possibility for XSS script attack in: "
                        + messageExchange.getModelItem().getName();
                assertionErrorList.add(new AssertionError(message));
                throwException = true;
            }
        }
        return throwException;
    }

    private List<String> submitScript(MessageExchange messageExchange, SubmitContext context) {
        List<String> urls = new ArrayList<String>();
        scriptEngine.setScript(script);
        scriptEngine.setVariable("urls", urls);
        scriptEngine.setVariable("messageExchange", messageExchange);
        this.messageExchange = messageExchange;
        scriptEngine.setVariable("context", context);
        this.context = context;
        scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());

        try {
            Object result = scriptEngine.run();
            if (result instanceof List) {
                urls = (List<String>) result;
            }
        } catch (Exception ex) {
            SoapUI.logError(ex);
        } finally {
            scriptEngine.clearVariables();
        }
        return urls;
    }

    private HttpTestRequestStep createHttpRequest(WsdlTestStep testStep2, String url) {
        HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
        httpRequest.setEndpoint(HttpUtils.completeUrlWithHttpIfProtocolIsNotHttpOrHttpsOrPropertyExpansion(url));
        httpRequest.setMethod("GET");

        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(HttpRequestStepFactory.HTTPREQUEST_TYPE);
        testStepConfig.setConfig(httpRequest);
        testStepConfig.setName("Separate Request");

        WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory(
                (HttpRequestStepFactory.HTTPREQUEST_TYPE));
        return (HttpTestRequestStep) factory.buildTestStep((WsdlTestCase) testStep2.getTestCase(), testStepConfig,
                false);

    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        //		return internalAssertResponse( messageExchange, context );
        return null;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(CrossSiteScriptAssertion.ID, CrossSiteScriptAssertion.LABEL, CrossSiteScriptAssertion.class,
                    CrossSiteScriptingScan.class);

        }

        @Override
        public String getCategory() {
            return "";
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return CrossSiteScriptAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(CrossSiteScriptAssertion.ID, CrossSiteScriptAssertion.LABEL,
                    CrossSiteScriptAssertion.DESCRIPTION);
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return null;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(GROOVY_SCRIPT, script);
        builder.add(CHECK_RESPONSE, checkResponse);
        builder.add(CHECK_SEPARATE_HTML, checkSeparateHTML);
        return builder.finish();
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        dialog.show();
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            checkResponse = Boolean.valueOf(dialog.getFormField(CrossSiteScripSeparateHTMLConfigDialog.CHECK_RESPONSE)
                    .getValue());
            checkSeparateHTML = Boolean.valueOf(dialog.getFormField(
                    CrossSiteScripSeparateHTMLConfigDialog.CHECK_SEPARATE_HTML).getValue());
            setConfiguration(createConfiguration());
        }
        return true;
    }

    private class GroovyEditorModel extends AbstractGroovyEditorModel {
        @Override
        public Action createRunAction() {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Object result = null;
                    List<String> urls = new ArrayList<String>();
                    scriptEngine.setScript(script);
                    scriptEngine.setVariable("urls", urls);
                    scriptEngine.setVariable("messageExchange", messageExchange);
                    scriptEngine.setVariable("context", context);
                    scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());

                    try {
                        result = scriptEngine.run();
                        if (result instanceof List) {
                            urls = (List<String>) result;
                        }
                        String generatedUrls = "";
                        for (String url : urls) {
                            generatedUrls += "\n" + url;
                        }
                        UISupport.showInfoMessage("Generated urls :" + generatedUrls + " \n\nScript result"
                                + ((result == null) ? "" : ": " + result + ""));
                    } catch (Exception ex) {
                        SoapUI.logError(ex);
                    } finally {
                        scriptEngine.clearVariables();
                    }
                }
            };
        }

        public GroovyEditorModel(ModelItem modelItem) {
            super(new String[]{"urls", "log", "context", "messageExchange"}, modelItem, "");
        }

        public String getScript() {
            return script;
        }

        public void setScript(String text) {
            script = text;
        }
    }

    protected GroovyEditorComponent buildGroovyPanel() {
        return new GroovyEditorComponent(groovyEditorModel, null);
    }

    protected void buildDialog() {
        dialog = ADialogBuilder.buildDialog(CrossSiteScripSeparateHTMLConfigDialog.class);
        dialog.setSize(600, 600);
        dialog.setBooleanValue(CrossSiteScripSeparateHTMLConfigDialog.CHECK_RESPONSE, checkResponse);
        dialog.setBooleanValue(CrossSiteScripSeparateHTMLConfigDialog.CHECK_SEPARATE_HTML, checkSeparateHTML);
        final GroovyEditorComponent groovyEditorComponent = buildGroovyPanel();
        dialog.getFormField(CrossSiteScripSeparateHTMLConfigDialog.GROOVY).setProperty("component",
                new JScrollPane(groovyEditorComponent));
        dialog.getFormField(CrossSiteScripSeparateHTMLConfigDialog.GROOVY).setProperty("dimension",
                new Dimension(450, 400));
        dialog.getFormField(CrossSiteScripSeparateHTMLConfigDialog.CHECK_SEPARATE_HTML).addFormFieldListener(
                new XFormFieldListener() {

                    @Override
                    public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                        groovyEditorComponent.setEnabled(new Boolean(newValue));
                    }

                });

        groovyEditorComponent.setEnabled(checkSeparateHTML);
    }

    @Override
    public void release() {
        if (dialog != null) {
            dialog.release();
        }

        super.release();
    }

    @AForm(description = "", name = "Cross Site Scripting on Separate HTML", helpUrl = HelpUrls.SECURITY_XSS_ASSERTION_HELP)
    protected interface CrossSiteScripSeparateHTMLConfigDialog {
        @AField(description = "Check Imediate Response", name = "###Check Response", type = AFieldType.BOOLEAN)
        public final static String CHECK_RESPONSE = "###Check Response";

        @AField(description = "Check Response from URLs specified in Custom Script", name = "###Check Separate HTML", type = AFieldType.BOOLEAN)
        public final static String CHECK_SEPARATE_HTML = "###Check Separate HTML";

        @AField(description = "", name = "Enter Custom Script that returns a list of URLs to check for Cross Site Scripts ", type = AFieldType.LABEL)
        public final static String LABEL = "Enter Custom Script that returns a list of URLs to check for Cross Site Scripts ";

        @AField(description = "Groovy script", name = "###Groovy url list", type = AFieldType.COMPONENT)
        public final static String GROOVY = "###Groovy url list";
    }
}
