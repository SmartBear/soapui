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
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.XmlBombSecurityScanConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.ui.XmlBombSecurityScanConfigPanel;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlString;

import javax.swing.JComponent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlBombSecurityScan extends AbstractSecurityScanWithProperties {

    public static final String TYPE = "XmlBombSecurityScan";
    public static final String NAME = "XML Bomb";
    private static final String DEFAULT_PREFIX = "xmlbomb";

    private int currentIndex = 0;
    private XmlBombSecurityScanConfig xmlBombConfig;
    private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();
    private boolean mutation;

    public XmlBombSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(testStep, config, parent, icon);
        if (config.getConfig() == null || !(config.getConfig() instanceof XmlBombSecurityScanConfig)) {
            initXmlBombConfig();
        } else {
            xmlBombConfig = (XmlBombSecurityScanConfig) config.getConfig();
        }

        getExecutionStrategy().setImmutable(true);
    }

    private void initXmlBombConfig() {
        getConfig().setConfig(XmlBombSecurityScanConfig.Factory.newInstance());
        xmlBombConfig = (XmlBombSecurityScanConfig) getConfig().getConfig();

        xmlBombConfig.setAttachXmlBomb(false);
        xmlBombConfig.setXmlAttachmentPrefix(DEFAULT_PREFIX);

        initDefaultVectors();

    }

    private void initDefaultVectors() {
        try {
            InputStream in = SoapUI.class
                    .getResourceAsStream("/com/eviware/soapui/resources/security/xmlbomb/BillionLaughsAttack.xml.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            StringBuffer value = new StringBuffer();
            while ((strLine = br.readLine()) != null) {
                value.append(strLine).append('\n');
            }
            in.close();
            XmlString bomb = xmlBombConfig.addNewXmlBombs();
            bomb.setStringValue(value.toString());
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        try {
            InputStream in = SoapUI.class
                    .getResourceAsStream("/com/eviware/soapui/resources/security/xmlbomb/QuadraticBlowup.xml.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            StringBuffer value = new StringBuffer();
            while ((strLine = br.readLine()) != null) {
                value.append(strLine).append('\n');
            }
            in.close();
            XmlString bomb = xmlBombConfig.addNewXmlBombs();
            bomb.setStringValue(value.toString());
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        try {
            InputStream in = SoapUI.class
                    .getResourceAsStream("/com/eviware/soapui/resources/security/xmlbomb/ExternalEntity.dtd.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            StringBuffer value = new StringBuffer();
            while ((strLine = br.readLine()) != null) {
                value.append(strLine).append('\n');
            }
            in.close();
            XmlString bomb = xmlBombConfig.addNewXmlBombs();
            bomb.setStringValue(value.toString());
        } catch (Exception e) {
            SoapUI.logError(e);
        }

    }

    @Override
    protected void execute(SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context) {
        try {
            StringToStringMap updatedParams = update(testStep, context);
            addAttachement(testStep);
            WsdlTestRequestStepResult message = (WsdlTestRequestStepResult) testStep.run(
                    (TestCaseRunner) securityTestRunner, context);
            message.setRequestContent("", false);
            createMessageExchange(updatedParams, message, context);
        } catch (XmlException e) {
            SoapUI.logError(e, "[XmlBombSecurityScan]XPath seems to be invalid!");
            reportSecurityScanException("Property value is not XML or XPath is wrong!");
        } catch (Exception e) {
            SoapUI.logError(e, "[XmlBombSecurityScan]Property value is not valid xml!");
            reportSecurityScanException("Property value is not XML or XPath is wrong!");
        }
    }

    private StringToStringMap update(TestStep testStep, SecurityTestRunContext context) throws XmlException, Exception {
        StringToStringMap params = new StringToStringMap();

        if (parameterMutations.size() == 0) {
            mutateParameters(testStep, context);
        }

		/*
         * Idea is to drain for each parameter mutations.
		 */
        for (SecurityCheckedParameter param : getParameterHolder().getParameterList()) {
            ArrayList<String> mutations = parameterMutations.get(param);
            if (mutations != null && !mutations.isEmpty()) {
                testStep.getProperties().get(param.getName()).setValue(mutations.get(0));
                params.put(param.getLabel(), mutations.get(0));
                mutations.remove(0);
                break;
            }
        }

        return params;
    }

    private void mutateParameters(TestStep testStep, SecurityTestRunContext context) throws XmlException, Exception {
        mutation = true;

        // for each parameter
        for (SecurityCheckedParameter parameter : getParameterHolder().getParameterList()) {

            if (parameter.isChecked()) {
                for (String bomb : xmlBombConfig.getXmlBombsList()) {
                    if (!parameterMutations.containsKey(parameter)) {
                        parameterMutations.put(parameter, new ArrayList<String>());
                    }
                    parameterMutations.get(parameter).add(bomb);
                }

            }
        }

    }

    @Override
    public JComponent getAdvancedSettingsPanel() {
        return new XmlBombSecurityScanConfigPanel(this);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public boolean isAttachXmlBomb() {
        return xmlBombConfig.getAttachXmlBomb();
    }

    public void setAttachXmlBomb(boolean attach) {
        xmlBombConfig.setAttachXmlBomb(attach);
    }

    private Attachment addAttachement(TestStep testStep) {
        Attachment attach = null;
        if (isAttachXmlBomb()) {
            WsdlRequest request = (WsdlRequest) getRequest(testStep);

            if (currentIndex < getXmlBombList().size()) {
                String bomb = getXmlBombList().get(currentIndex);
                try {
                    File bombFile = File.createTempFile(getAttachmentPrefix(), ".xml");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(bombFile));
                    writer.write(bomb);
                    writer.flush();
                    request.setInlineFilesEnabled(false);
                    attach = request.attachFile(bombFile, false);
                    attach.setContentType("text/xml;");
                    currentIndex++;
                } catch (IOException e) {
                    SoapUI.logError(e);
                }
            }
        }
        return attach;
    }

    public List<String> getXmlBombList() {
        return xmlBombConfig.getXmlBombsList();
    }

    protected void setBombList(List<String> bombList) {
        xmlBombConfig.setXmlBombsArray(bombList.toArray(new String[1]));
    }

    public String getAttachmentPrefix() {
        return xmlBombConfig.getXmlAttachmentPrefix();
    }

    public void setAttachmentPrefix(String prefix) {
        xmlBombConfig.setXmlAttachmentPrefix(prefix);
    }

    @Override
    protected boolean hasNext(TestStep testStep, SecurityTestRunContext context) {
        boolean hasNext = false;
        if ((parameterMutations == null || parameterMutations.size() == 0) && !mutation) {
            if (getParameterHolder().getParameterList().size() > 0) {
                hasNext = true;
            } else {
                hasNext = false;
            }
        } else {
            for (SecurityCheckedParameter param : parameterMutations.keySet()) {
                if (parameterMutations.get(param).size() > 0) {
                    hasNext = true;
                    break;
                }
            }
        }
        // even if all params are mutaded there could be some attachemnt to send.
        if (isAttachXmlBomb()) {
            hasNext = currentIndex < getXmlBombList().size();
        }
        if (!hasNext) {
            parameterMutations.clear();
            mutation = false;
            currentIndex = 0;
        }

        return hasNext;
    }

    @Override
    protected void clear() {
        parameterMutations.clear();
        mutation = false;
        currentIndex = 0;
    }

    @Override
    public String getConfigDescription() {
        return "Configures Xml bomb security scan";
    }

    @Override
    public String getConfigName() {
        return "XML Bomb Security Scan";
    }

    @Override
    public String getHelpURL() {
        return "http://soapui.org/Security/xml-bomb.html";
    }
}
