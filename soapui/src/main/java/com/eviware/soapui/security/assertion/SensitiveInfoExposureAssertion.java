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
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.security.SensitiveInformationTableModel;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.SensitiveInformationPropertyHolder;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import org.apache.xmlbeans.XmlObject;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SensitiveInfoExposureAssertion extends WsdlMessageAssertion implements ResponseAssertion {
    private static final String PREFIX = "~";
    public static final String ID = "Sensitive Information Exposure";
    public static final String LABEL = "Sensitive Information Exposure";

    private List<String> assertionSpecificExposureList;

    private XFormDialog dialog;
    private static final String ASSERTION_SPECIFIC_EXPOSURE_LIST = "AssertionSpecificExposureList";
    private static final String INCLUDE_GLOBAL = "IncludeGlobal";
    private static final String INCLUDE_PROJECT_SPECIFIC = "IncludeProjectSpecific";
    public static final String DESCRIPTION = "Checks that the last received message does not expose an sensitive information about the target system. Applicable to REST, SOAP and HTTP TestSteps.";
    private boolean includeGlobal;
    private boolean includeProjectSpecific;
    private JPanel sensitiveInfoTableForm;
    private SensitiveInformationTableModel sensitiveInformationTableModel;
    private JXTable tokenTable;

    public SensitiveInfoExposureAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, false, true, false, true);

        init();
    }

    private void init() {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        includeGlobal = reader.readBoolean(INCLUDE_GLOBAL, true);
        includeProjectSpecific = reader.readBoolean(INCLUDE_PROJECT_SPECIFIC, true);
        assertionSpecificExposureList = StringUtils.toStringList(reader.readStrings(ASSERTION_SPECIFIC_EXPOSURE_LIST));
        extractTokenTable();
    }

    private void extractTokenTable() {
        SensitiveInformationPropertyHolder siph = new SensitiveInformationPropertyHolder();
        for (String str : assertionSpecificExposureList) {
            if ("###".equals(str)) {
                continue;
            }
            String[] tokens = str.split("###");
            if (tokens.length == 2) {
                siph.setPropertyValue(tokens[0], tokens[1]);
            } else if (tokens.length == 1) {
                siph.setPropertyValue(tokens[0], "");
            }
        }
        sensitiveInformationTableModel = new SensitiveInformationTableModel(siph);
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        Map<String, String> checkMap = createCheckMap(context);
        List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
        String response = messageExchange.getResponseContent();
        Set<String> messages = new HashSet<String>();

        try {
            for (Map.Entry<String, String> tokenEntry : checkMap.entrySet()) {
                String token = tokenEntry.getKey();
                boolean useRegexp = token.trim().startsWith(PREFIX);
                String description = !tokenEntry.getValue().equals("") ? tokenEntry.getValue() : token;
                if (useRegexp) {
                    token = token.substring(token.indexOf(PREFIX) + 1);
                }

                String match = SecurityScanUtil.contains(context, response, token, useRegexp);
                if (match != null) {
                    String message = description + " - Token [" + token + "] found [" + match + "]";
                    if (!messages.contains(message)) {
                        assertionErrorList.add(new AssertionError(message));
                        messages.add(message);
                    }
                }
            }
        } catch (Throwable e) {
            SoapUI.logError(e);
        }

        if (!messages.isEmpty()) {
            throw new AssertionException(assertionErrorList.toArray(new AssertionError[assertionErrorList.size()]));
        }

        return "OK";
    }

    //TODO check if this should be applicable to properties after all, it's not mapped for properties currently
    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {

        Map<String, String> checkMap = createCheckMap(context);
        List<AssertionError> assertionErrorList = new ArrayList<AssertionError>();
        String propertyValue = source.getPropertyValue(propertyName);
        Set<String> messages = new HashSet<String>();

        try {
            for (Map.Entry<String, String> tokenEntry : checkMap.entrySet()) {
                String token = tokenEntry.getKey();
                boolean useRegexp = token.trim().startsWith(PREFIX);
                String description = !tokenEntry.getValue().equals("") ? tokenEntry.getValue() : token;
                if (useRegexp) {
                    token = token.substring(token.indexOf(PREFIX) + 1);
                }

                String match = SecurityScanUtil.contains(context, propertyValue, token, useRegexp);
                if (match != null) {
                    String message = description + " - Token [" + token + "] found [" + match + "] in property "
                            + propertyName;
                    if (!messages.contains(message)) {
                        assertionErrorList.add(new AssertionError(message));
                        messages.add(message);
                    }
                }
            }
        } catch (Throwable e) {
            SoapUI.logError(e);
        }

        if (!messages.isEmpty()) {
            throw new AssertionException(assertionErrorList.toArray(new AssertionError[assertionErrorList.size()]));
        }

        return "OK";
    }

    private Map<String, String> createCheckMap(SubmitContext context) {
        Map<String, String> checkMap = new HashMap<String, String>();
        checkMap.putAll(createMapFromTable());
        if (includeProjectSpecific) {
            checkMap.putAll(SecurityScanUtil.projectEntriesList(this));
        }

        if (includeGlobal) {
            checkMap.putAll(SecurityScanUtil.globalEntriesList());
        }
        Map<String, String> expandedMap = propertyExpansionSupport(checkMap, context);
        return expandedMap;
    }

    private Map<String, String> propertyExpansionSupport(Map<String, String> checkMap, SubmitContext context) {
        Map<String, String> expanded = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : checkMap.entrySet()) {
            expanded.put(context.expand(entry.getKey()), context.expand(entry.getValue()));
        }
        return expanded;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        @SuppressWarnings("unchecked")
        public Factory() {
            super(SensitiveInfoExposureAssertion.ID, SensitiveInfoExposureAssertion.LABEL,
                    SensitiveInfoExposureAssertion.class, new Class[]{SecurityScan.class, AbstractHttpRequest.class});
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.SECURITY_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return SensitiveInfoExposureAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(SensitiveInfoExposureAssertion.ID, SensitiveInfoExposureAssertion.LABEL,
                    SensitiveInfoExposureAssertion.DESCRIPTION);
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return null;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(ASSERTION_SPECIFIC_EXPOSURE_LIST,
                assertionSpecificExposureList.toArray(new String[assertionSpecificExposureList.size()]));
        builder.add(INCLUDE_PROJECT_SPECIFIC, includeProjectSpecific);
        builder.add(INCLUDE_GLOBAL, includeGlobal);
        return builder.finish();
    }

    @Override
    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }
        if (dialog.show()) {
            assertionSpecificExposureList = createListFromTable();
            includeProjectSpecific = Boolean.valueOf(dialog.getFormField(
                    SensitiveInformationConfigDialog.INCLUDE_PROJECT_SPECIFIC).getValue());
            includeGlobal = Boolean.valueOf(dialog.getFormField(SensitiveInformationConfigDialog.INCLUDE_GLOBAL)
                    .getValue());
            setConfiguration(createConfiguration());

            return true;
        }
        return false;
    }

    private List<String> createListFromTable() {
        List<String> temp = new ArrayList<String>();
        for (TestProperty tp : sensitiveInformationTableModel.getHolder().getPropertyList()) {
            String tokenPlusDescription = tp.getName() + "###" + tp.getValue();
            temp.add(tokenPlusDescription);
        }
        return temp;
    }

    private Map<String, String> createMapFromTable() {
        Map<String, String> temp = new HashMap<String, String>();
        for (TestProperty tp : sensitiveInformationTableModel.getHolder().getPropertyList()) {
            temp.put(tp.getName(), tp.getValue());
        }
        return temp;
    }

    protected void buildDialog() {
        dialog = ADialogBuilder.buildDialog(SensitiveInformationConfigDialog.class);
        dialog.setBooleanValue(SensitiveInformationConfigDialog.INCLUDE_GLOBAL, includeGlobal);
        dialog.setBooleanValue(SensitiveInformationConfigDialog.INCLUDE_PROJECT_SPECIFIC, includeProjectSpecific);
        dialog.getFormField(SensitiveInformationConfigDialog.TOKENS).setProperty("component", getForm());
    }

    // TODO : update help URL
    @AForm(description = "Configure Sensitive Information Exposure Assertion", name = "Sensitive Information Exposure Assertion", helpUrl = HelpUrls.SECURITY_SENSITIVE_INFORMATION_EXPOSURE_ASSERTION_HELP)
    protected interface SensitiveInformationConfigDialog {

        @AField(description = "Sensitive informations to check. Use ~ as prefix for values that are regular expressions.", name = "Sensitive Information Tokens", type = AFieldType.COMPONENT)
        public final static String TOKENS = "Sensitive Information Tokens";

        @AField(description = "Include project specific sensitive information configuration", name = "Project Specific", type = AFieldType.BOOLEAN)
        public final static String INCLUDE_PROJECT_SPECIFIC = "Project Specific";

        @AField(description = "Include global sensitive information configuration", name = "Global Configuration", type = AFieldType.BOOLEAN)
        public final static String INCLUDE_GLOBAL = "Global Configuration";

    }

    @Override
    public void release() {
        if (dialog != null) {
            dialog.release();
        }

        super.release();
    }

    public JPanel getForm() {
        if (sensitiveInfoTableForm == null) {
            sensitiveInfoTableForm = new JPanel(new BorderLayout());

            JXToolBar toolbar = UISupport.createToolbar();

            toolbar.add(UISupport.createToolbarButton(new AddTokenAction()));
            toolbar.add(UISupport.createToolbarButton(new RemoveTokenAction()));

            tokenTable = JTableFactory.getInstance().makeJXTable(sensitiveInformationTableModel);
            tokenTable.setPreferredSize(new Dimension(200, 100));
            sensitiveInfoTableForm.add(toolbar, BorderLayout.NORTH);
            sensitiveInfoTableForm.add(new JScrollPane(tokenTable), BorderLayout.CENTER);
        }

        return sensitiveInfoTableForm;
    }

    class AddTokenAction extends AbstractAction {

        public AddTokenAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Adds a token to assertion");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String newToken = "";
            while (newToken.trim().length() == 0) {
                newToken = UISupport.prompt("Enter token", "New Token", newToken);

                if (newToken == null) {
                    return;
                }

                if (newToken.trim().length() == 0) {
                    UISupport.showErrorMessage("Enter token name!");
                }
            }

            String newValue = "";
            newValue = UISupport.prompt("Enter description", "New Description", newValue);

            if (newValue == null) {
                newValue = "";
            }

            sensitiveInformationTableModel.addToken(newToken, newValue);
        }

    }

    class RemoveTokenAction extends AbstractAction {

        public RemoveTokenAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(Action.SHORT_DESCRIPTION, "Removes token from assertion");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            sensitiveInformationTableModel.removeRows(tokenTable.getSelectedRows());
        }

    }
}
