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

import com.eviware.soapui.config.FuzzerScanConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.FuzzerScanAdvancedConfigPanel;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.impl.swing.JFormDialog;
import org.apache.commons.lang.RandomStringUtils;

import javax.swing.JComponent;

public class FuzzerSecurityScan extends AbstractSecurityScanWithProperties {

    public static final String TYPE = "FuzzingScan";
    public static final String NAME = "Fuzzing Scan";
    public static final int DEFAULT_MINIMAL = 5;
    public static final int DEFAULT_MAXIMAL = 15;
    public static final int DEFAULT_NUMBER_OF_REQUESTS = 100;
    private JFormDialog dialog;
    private FuzzerScanConfig fuzzerScanConfig;
    private Integer numberOfRequests;
    private int minimal;
    private int maximal;

    public FuzzerSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(testStep, config, parent, icon);
        if (config.getConfig() == null || !(config.getConfig() instanceof FuzzerScanConfig)) {
            initConfig();
        } else {
            fuzzerScanConfig = (FuzzerScanConfig) getConfig().getConfig();
        }

        getExecutionStrategy().setStrategy(StrategyTypeConfig.ALL_AT_ONCE);
        getExecutionStrategy().setImmutable(true);
    }

    private void initConfig() {
        getConfig().setConfig(FuzzerScanConfig.Factory.newInstance());
        fuzzerScanConfig = (FuzzerScanConfig) getConfig().getConfig();
        fuzzerScanConfig.setMinimal(DEFAULT_MINIMAL);
        fuzzerScanConfig.setMaximal(DEFAULT_MAXIMAL);
        fuzzerScanConfig.setNumberOfRequest(DEFAULT_NUMBER_OF_REQUESTS);
    }

    @Override
    protected void execute(SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context) {
        StringToStringMap parameters = new StringToStringMap();
        XmlObjectTreeModel model = null;
        for (SecurityCheckedParameter scp : getParameterHolder().getParameterList()) {
            if (scp.isChecked()) {
                if (scp.getXpath().trim().length() > 0) {
                    model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                    XmlTreeNode[] treeNodes = null;
                    treeNodes = model.selectTreeNodes(context.expand(scp.getXpath()));
                    if (treeNodes.length > 0) {
                        XmlTreeNode mynode = treeNodes[0];
                        String fuzzed = fuzzedValue();
                        mynode.setValue(1, fuzzed);
                        parameters.put(scp.getLabel(), fuzzed);
                    }
                    updateRequestProperty(testStep, scp.getName(), model.getXmlObject().toString());

                } else {
                    String fuzzed = fuzzedValue();
                    parameters.put(scp.getLabel(), fuzzed);
                    updateRequestProperty(testStep, scp.getName(), fuzzed);
                }
            }

            MessageExchange message = (MessageExchange) testStep.run((TestCaseRunner) runner, context);
            createMessageExchange(parameters, message, context);
        }
    }

    private String fuzzedValue() {
        int count = (int) (Math.random() * (maximal + 1 - minimal)) + minimal;
        return RandomStringUtils.randomAlphanumeric(count);
    }

    private void updateRequestProperty(TestStep testStep, String propertyName, String propertyValue) {
        testStep.getProperty(propertyName).setValue(propertyValue);

    }

    @Override
    protected boolean hasNext(TestStep testStep2, SecurityTestRunContext context) {
        if (numberOfRequests == null) {
            numberOfRequests = fuzzerScanConfig.getNumberOfRequest();
            minimal = fuzzerScanConfig.getMinimal();
            maximal = fuzzerScanConfig.getMaximal();
        }

        if (numberOfRequests > 0) {
            numberOfRequests--;
            return true;
        } else {
            numberOfRequests = null;
            return false;
        }
    }

    @Override
    public JComponent getAdvancedSettingsPanel() {
        FuzzerScanAdvancedConfigPanel configPanel = new FuzzerScanAdvancedConfigPanel(fuzzerScanConfig);
        dialog = configPanel.getDialog();
        return dialog.getPanel();
    }

    @Override
    public void release() {
        if (dialog != null) {
            dialog.release();
        }

        super.release();
    }

    @Override
    public String getConfigDescription() {
        return "Configuration for Fuzzing Security Scan";
    }

    @Override
    public String getConfigName() {
        return "Configuration for Fuzzing Security Scan";
    }

    @Override
    public String getHelpURL() {
        // TODO: change to proper help url
        return HelpUrls.SECURITY_SCANS_OVERVIEW;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    protected void clear() {
        numberOfRequests = null;
    }

}
