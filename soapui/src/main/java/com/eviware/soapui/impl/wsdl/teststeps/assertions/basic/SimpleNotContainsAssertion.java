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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingXFormImpl;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.xmlbeans.XmlObject;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Assertion that checks for the non-existence of a specified text token in the
 * associated WsdlTestRequests response message
 *
 * @author Ole.Matzura
 */

public class SimpleNotContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion {
    private String token;
    private boolean ignoreCase;
    private XFormDialog dialog;
    private boolean useRegEx;
    public static final String ID = "Simple NotContains";
    private static final String CONTENT = "Content";
    private static final String IGNORE_CASE = "Ignore Case";
    private static final String USE_REGEX = "Regular Expression";
    public static final String LABEL = "Not Contains";
    public static final String DESCRIPTION = "Searches for the non-existence of a string token in the property value, supports regular expressions. Applicable to any property.";
    private static CellConstraints cc = new CellConstraints();

    public SimpleNotContainsAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, true, true, true, true);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        token = reader.readString("token", null);
        ignoreCase = reader.readBoolean("ignoreCase", false);
        useRegEx = reader.readBoolean("useRegEx", false);
    }

    public String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return assertContent(context, messageExchange.getResponseContent(), "Response");
    }

    @Override
    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        assertContent(context, source.getPropertyValue(propertyName), propertyName);
        return "OK";
    }

    private String assertContent(SubmitContext context, String content, String type) throws AssertionException {
        if (token == null) {
            token = "";
        }
        if (content == null) {
            content = "";
        }

        String replToken = PropertyExpander.expandProperties(context, token);
        if (replToken == null) {
            replToken = "";
        }

        replToken = normalize(replToken);
        content = normalize(content);

        if (replToken.length() > 0) {
            int ix = -1;

            if (useRegEx) {
                if (content.matches(replToken)) {
                    ix = 0;
                }
            } else {
                ix = ignoreCase ? content.toUpperCase().indexOf(replToken.toUpperCase()) : content.indexOf(replToken);
            }

            if (ix != -1) {
                throw new AssertionException(new AssertionError(type + " contains token [" + replToken + "]"));
            }
        }

        return type + " does not contain token [" + replToken + "]";
    }

    private String normalize(String string) {
        if (!StringUtils.isNullOrEmpty(string)) {
            string = string.replaceAll("\r\n", "\n");
        }
        return string;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(CONTENT, token);
        values.put(IGNORE_CASE, ignoreCase);
        values.put(USE_REGEX, useRegEx);

        values = dialog.show(values);
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            token = values.get(CONTENT);
            ignoreCase = values.getBoolean(IGNORE_CASE);
            useRegEx = values.getBoolean(USE_REGEX);
        }

        setConfiguration(createConfiguration());
        return true;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("token", token);
        builder.add("ignoreCase", ignoreCase);
        builder.add("useRegEx", useRegEx);
        return builder.finish();
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("NotContains Assertion");
        XForm mainForm = builder.createForm("Basic", new FormLayout("5px,left:pref,5px,fill:default:grow(1.0),5px"));
        JPanel mainFormPanel = ((SwingXFormImpl) mainForm).getPanel();
        FormLayout mainFormLayout = (FormLayout) mainFormPanel.getLayout();

        mainForm.addTextField(CONTENT, "Content to check for", XForm.FieldType.TEXTAREA).setWidth(40);

        mainFormLayout.setRowSpec(mainFormLayout.getRowCount(), new RowSpec("top:default:grow(1.0)"));
        mainFormPanel.add(mainFormPanel.getComponent(mainFormPanel.getComponents().length-1),cc.xy(4,mainFormLayout.getRowCount(),"fill,fill"));

        mainForm.addCheckBox(IGNORE_CASE, "Ignore case in comparison");
        mainForm.addCheckBox(USE_REGEX, "Use token as Regular Expression");

        dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.SIMPLE_NOT_CONTAINS_HELP_URL),
                "Specify options", UISupport.OPTIONS_ICON);
    }

    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return assertContent(context, messageExchange.getRequestContent(), "Request");
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        setConfiguration(createConfiguration());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        setConfiguration(createConfiguration());
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(getAssertable().getModelItem(), this, "token"));

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(SimpleNotContainsAssertion.ID, SimpleNotContainsAssertion.LABEL, SimpleNotContainsAssertion.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.VALIDATE_RESPONSE_CONTENT_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return SimpleNotContainsAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(SimpleNotContainsAssertion.ID, SimpleNotContainsAssertion.LABEL,
                    SimpleNotContainsAssertion.DESCRIPTION);
        }

        @Override
        public boolean canAssert(TestPropertyHolder modelItem, String property) {
            String content = modelItem.getPropertyValue(property);
            //			return !StringUtils.isNullOrEmpty( content );
            return true;
        }
    }
}
