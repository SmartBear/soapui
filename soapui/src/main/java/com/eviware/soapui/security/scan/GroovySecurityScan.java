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
import com.eviware.soapui.config.GroovySecurityScanConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.GroovySecurityScanPanel;
import com.eviware.soapui.security.ui.SecurityScanConfigPanel;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

import java.util.List;
import java.util.Map;

/**
 * @author soapui team
 */

public class GroovySecurityScan extends AbstractSecurityScanWithProperties {

    public static final String SCRIPT_PROPERTY = GroovySecurityScan.class.getName() + "@script";
    public static final String TYPE = "GroovySecurityScan";
    public static final String NAME = "Custom Script";
    private GroovySecurityScanConfig groovyscc;
    private Boolean hasNext = true;
    private Object scriptResult;
    private SoapUIScriptEngine scriptEngine;

    private StringToStringMap parameters;
    // private TestStepResult stepResult;

    // private TestProperty response;

    private static final String PARAMETERS_INITIALIZED = "parameterInitialized";

    public GroovySecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {

        super(testStep, config, parent, icon);
        if (config.getConfig() == null) {
            groovyscc = GroovySecurityScanConfig.Factory.newInstance();
            groovyscc.setExecuteScript(ScriptConfig.Factory.newInstance());
            groovyscc.getExecuteScript().setLanguage("groovy");
            groovyscc.getExecuteScript().setStringValue("");
            config.setConfig(groovyscc);
        } else {
            groovyscc = (GroovySecurityScanConfig) config.getConfig();
            if (groovyscc.getExecuteScript() == null) {
                groovyscc.setExecuteScript(ScriptConfig.Factory.newInstance());
                groovyscc.getExecuteScript().setLanguage("groovy");
                groovyscc.getExecuteScript().setStringValue("");
            }
        }

        scriptEngine = SoapUIScriptEngineRegistry.create(this);

        getExecutionStrategy().setImmutable(true);
    }

    @Override
    protected boolean hasNext(TestStep testStep, SecurityTestRunContext context) {
        if (!context.hasProperty(PARAMETERS_INITIALIZED)) {
            parameters = new StringToStringMap();
            initParameters(parameters);
            context.put(PARAMETERS_INITIALIZED, "true");
            hasNext = true;
        }

        if (!hasNext) {
            context.remove(PARAMETERS_INITIALIZED);
            scriptEngine.clearVariables();
        }

        return hasNext;
    }

    private void initParameters(StringToStringMap parameters2) {
        List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
        for (SecurityCheckedParameter scp : scpList) {
            parameters.put(scp.getLabel(), null);
        }
    }

    @Override
    protected void execute(SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context) {
        scriptEngine.setScript(groovyscc.getExecuteScript().getStringValue());
        scriptEngine.setVariable("context", context);
        scriptEngine.setVariable("testStep", testStep);
        scriptEngine.setVariable("securityScan", this);
        scriptEngine.setVariable("parameters", parameters);
        scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());

        try {
            scriptResult = scriptEngine.run();
            hasNext = castResultToBoolean(scriptResult);
            XmlObjectTreeModel model = null;
            for (SecurityCheckedParameter scp : getParameterHolder().getParameterList()) {
                if (parameters.containsKey(scp.getLabel()) && parameters.get(scp.getLabel()) != null) {
                    if (scp.isChecked() && scp.getXpath().trim().length() > 0) {
                        model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                        XmlTreeNode[] treeNodes = null;
                        treeNodes = model.selectTreeNodes(context.expand(scp.getXpath()));
                        if (treeNodes.length > 0) {
                            XmlTreeNode mynode = treeNodes[0];
                            mynode.setValue(1, parameters.get(scp.getLabel()));
                        }
                        updateRequestProperty(testStep, scp.getName(), model.getXmlObject().toString());

                    } else {
                        updateRequestProperty(testStep, scp.getName(), parameters.get(scp.getLabel()));
                    }
                } else if (parameters.containsKey(scp.getLabel()) && parameters.get(scp.getLabel()) == null) {// clears null values form parameters
                    parameters.remove(scp.getLabel());
                }

            }

            MessageExchange message = (MessageExchange) testStep.run((TestCaseRunner) securityTestRunner, context);
            createMessageExchange(clearNullValues(parameters), message, context);

        } catch (Exception e) {
            SoapUI.logError(e);
            hasNext = false;
        } finally {
            // if( scriptResult != null )
            // {
            // getTestStep().getProperty( "Request" ).setValue( ( String
            // )scriptResult );
            //
            // getTestStep().run( ( TestCaseRunner )securityTestRunner,
            // ( TestCaseRunContext )securityTestRunner.getRunContext() );
            // }

        }

    }

    private Boolean castResultToBoolean(Object scriptResult2) {
        try {
            hasNext = (Boolean) scriptResult2;
            if (hasNext == null) {
                hasNext = false;
                SoapUI.ensureGroovyLog().error("You must return Boolean value from groovy script!");
            }
        } catch (Exception e) {
            hasNext = false;
            SoapUI.ensureGroovyLog().error("You must return Boolean value from groovy script!");
        }
        return hasNext;
    }

    private StringToStringMap clearNullValues(StringToStringMap parameters) {
        StringToStringMap params = new StringToStringMap();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        return params;
    }

    private void updateRequestProperty(TestStep testStep, String propertyName, String propertyValue) {
        testStep.getProperty(propertyName).setValue(propertyValue);

    }

    public void setExecuteScript(String script) {
        String old = getExecuteScript();
        groovyscc.getExecuteScript().setStringValue(script);
        notifyPropertyChanged(SCRIPT_PROPERTY, old, script);
    }

    public String getExecuteScript() {
        return groovyscc.getExecuteScript().getStringValue();
    }

    @Override
    public SecurityScanConfigPanel getComponent() {
        return new GroovySecurityScanPanel(this);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getConfigDescription() {
        return "Configuration for Custom Script Security Scan";
    }

    @Override
    public String getConfigName() {
        return "Configuration for Custom Script Security Scan";
    }

    @Override
    public String getHelpURL() {
        return "http://soapui.org/Security/script-custom-scan.html";
    }

}
