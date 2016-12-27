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
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.config.XPathInjectionConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JStringListFormField;
import org.apache.xmlbeans.XmlException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XPathInjectionSecurityScan extends AbstractSecurityScanWithProperties {

    public static final String TYPE = "XPathInjectionSecurityScan";
    public static final String NAME = "XPath Injection";

    private XPathInjectionConfig xpathList;

    private Map<SecurityCheckedParameter, ArrayList<String>> parameterMutations = new HashMap<SecurityCheckedParameter, ArrayList<String>>();

    String[] defaultXPathInjectionStrings = {" or name(//users/LoginID[1]) = 'LoginID' or 'a'='b", "' or '1'='1",
            "1/0", "'%20o/**/r%201/0%20--", "' o/**/r 1/0 --", ";", "'%20and%201=2%20--", "' and 1=2 --",
            "test�%20UNION%20select%201,%20@@version,%201,%201;�", "test� UNION select 1, @@version, 1, 1;�"};

    private boolean mutation;
    private JFormDialog dialog;

    public XPathInjectionSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(testStep, config, parent, icon);

        if (config.getConfig() == null || !(config.getConfig() instanceof XPathInjectionConfig)) {
            initXPathInjectionConfig();
        } else {
            xpathList = (XPathInjectionConfig) getConfig().getConfig();
        }
    }

    @Override
    public void updateSecurityConfig(SecurityScanConfig config) {
        super.updateSecurityConfig(config);

        if (xpathList != null) {
            xpathList = (XPathInjectionConfig) getConfig().getConfig();
        }
    }

    private void initXPathInjectionConfig() {
        getConfig().setConfig(XPathInjectionConfig.Factory.newInstance());
        xpathList = (XPathInjectionConfig) getConfig().getConfig();

        xpathList.setXpathListArray(defaultXPathInjectionStrings);
    }

    @Override
    public JComponent getComponent() {
        JPanel p = UISupport.createEmptyPanel(5, 75, 0, 5);
        p.add(new JLabel("Strings for XPath injection can be changed under advanced settings"));
        return p;
    }

    @Override
    protected void execute(SecurityTestRunner runner, TestStep testStep, SecurityTestRunContext context) {
        try {
            StringToStringMap paramsUpdated = update(testStep, context);
            MessageExchange message = (MessageExchange) testStep.run((TestCaseRunner) runner, context);
            createMessageExchange(paramsUpdated, message, context);
        } catch (XmlException e) {
            SoapUI.logError(e, "[XPathInjectionSecurityScan]XPath seems to be invalid!");
            reportSecurityScanException("Property value is not XML or XPath is wrong!");
        } catch (Exception e) {
            SoapUI.logError(e, "[XPathInjectionSecurityScan]Property value is not valid xml!");
            reportSecurityScanException("Property value is not XML or XPath is wrong!");
        }
    }

    private StringToStringMap update(TestStep testStep, SecurityTestRunContext context) throws XmlException, Exception {
        StringToStringMap params = new StringToStringMap();

        if (parameterMutations.size() == 0) {
            mutateParameters(testStep, context);
        }

        if (getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE) {
            /*
			 * Idea is to drain for each parameter mutations.
			 */
            for (SecurityCheckedParameter param : getParameterHolder().getParameterList()) {
                if (parameterMutations.containsKey(param)) {
                    if (parameterMutations.get(param).size() > 0) {
                        TestProperty property = getTestStep().getProperties().get(param.getName());
                        String value = context.expand(property.getValue());
                        if (param.getXpath() == null || param.getXpath().trim().length() == 0) {
                            testStep.getProperties().get(param.getName())
                                    .setValue(parameterMutations.get(param).get(0));
                            params.put(param.getLabel(), parameterMutations.get(param).get(0));
                            parameterMutations.get(param).remove(0);
                        } else {
                            // no value, do nothing.
                            if (value == null || value.trim().equals("")) {
                                continue;
                            }
                            // XmlObjectTreeModel model = new XmlObjectTreeModel(
                            // property.getSchemaType().getTypeSystem(),
                            // XmlObject.Factory.parse( value ) );
                            XmlObjectTreeModel model = new XmlObjectTreeModel(property.getSchemaType().getTypeSystem(),
                                    XmlUtils.createXmlObject(value));
                            XmlTreeNode[] nodes = model.selectTreeNodes(context.expand(param.getXpath()));
                            for (XmlTreeNode node : nodes) {
                                node.setValue(1, parameterMutations.get(param).get(0));
                            }
                            params.put(param.getLabel(), parameterMutations.get(param).get(0));
                            parameterMutations.get(param).remove(0);

                            testStep.getProperties().get(param.getName()).setValue(model.getXmlObject().toString());
                        }

                        break;
                    }
                }
            }
        } else {
            for (TestProperty property : testStep.getPropertyList()) {

                String value = context.expand(property.getValue());
                if (XmlUtils.seemsToBeXml(value)) {
                    XmlObjectTreeModel model = null;
                    // model = new XmlObjectTreeModel(
                    // property.getSchemaType().getTypeSystem(),
                    // XmlObject.Factory.parse( value ) );
                    model = new XmlObjectTreeModel(property.getSchemaType().getTypeSystem(),
                            XmlUtils.createXmlObject(value));
                    for (SecurityCheckedParameter param : getParameterHolder().getParameterList()) {
                        if (!param.isChecked()) {
                            continue;
                        }

                        if (param.getXpath() == null || param.getXpath().trim().length() == 0) {
                            if (parameterMutations.containsKey(param)) {
                                testStep.getProperties().get(param.getName())
                                        .setValue(parameterMutations.get(param).get(0));
                                params.put(param.getLabel(), parameterMutations.get(param).get(0));
                                parameterMutations.get(param).remove(0);
                            }
                        } else {
                            // no value, do nothing.
                            if (value == null || value.trim().equals("")) {
                                continue;
                            }
                            if (param.getName().equals(property.getName())) {
                                XmlTreeNode[] nodes = model.selectTreeNodes(context.expand(param.getXpath()));
                                if (parameterMutations.containsKey(param)) {
                                    if (parameterMutations.get(param).size() > 0) {
                                        for (XmlTreeNode node : nodes) {
                                            node.setValue(1, parameterMutations.get(param).get(0));
                                        }
                                        params.put(param.getLabel(), parameterMutations.get(param).get(0));
                                        parameterMutations.get(param).remove(0);
                                    }
                                }
                            }
                        }
                    }
                    if (model != null) {
                        property.setValue(model.getXmlObject().toString());
                    }
                }

            }
        }
        return params;
    }

    private void mutateParameters(TestStep testStep, SecurityTestRunContext context) throws XmlException, Exception {
        mutation = true;
        // for each parameter
        for (SecurityCheckedParameter parameter : getParameterHolder().getParameterList()) {
            if (parameter.isChecked()) {
                TestProperty property = testStep.getProperties().get(parameter.getName());
                // check parameter does not have any xpath
                // than mutate whole parameter
                if (parameter.getXpath() == null || parameter.getXpath().trim().length() == 0) {
                    for (String xpathInjectionString : xpathList.getXpathListList()) {

                        if (!parameterMutations.containsKey(parameter)) {
                            parameterMutations.put(parameter, new ArrayList<String>());
                        }
                        parameterMutations.get(parameter).add(xpathInjectionString);

                    }
                } else {
                    // we have xpath but do we have xml which need to mutate
                    // ignore if there is no value, since than we'll get exception
                    if (property.getValue() == null && property.getDefaultValue() == null) {
                        continue;
                    }
                    // get value of that property
                    String value = context.expand(property.getValue());

                    // we have something that looks like xpath, or hope so.

                    XmlObjectTreeModel model = null;

                    // model = new XmlObjectTreeModel(
                    // property.getSchemaType().getTypeSystem(),
                    // XmlObject.Factory.parse( value ) );
                    model = new XmlObjectTreeModel(property.getSchemaType().getTypeSystem(),
                            XmlUtils.createXmlObject(value));

                    XmlTreeNode[] nodes = model.selectTreeNodes(context.expand(parameter.getXpath()));

                    // for each invalid type set all nodes

                    for (String xpathInjectionString : xpathList.getXpathListList()) {

                        if (nodes.length > 0) {
                            if (!parameterMutations.containsKey(parameter)) {
                                parameterMutations.put(parameter, new ArrayList<String>());
                            }
                            parameterMutations.get(parameter).add(xpathInjectionString);
                        }

                    }

                }
            }
        }

    }

    @Override
    public String getConfigDescription() {
        return "Configures XPath Injection Security Scan";
    }

    @Override
    public String getConfigName() {
        return "XPath Injection Security Scan";
    }

    @Override
    public String getHelpURL() {
        return "http://soapui.org/Security/xpath-injection.html";
    }

    @Override
    public String getType() {
        return TYPE;
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
        if (!hasNext) {
            parameterMutations.clear();
            mutation = false;
        }
        return hasNext;
    }

    @Override
    public JComponent getAdvancedSettingsPanel() {
        dialog = (JFormDialog) ADialogBuilder.buildDialog(AdvancedSettings.class);
        JStringListFormField stringField = (JStringListFormField) dialog
                .getFormField(AdvancedSettings.INJECTION_STRINGS);
        stringField.setOptions(xpathList.getXpathListList().toArray());
        stringField.setProperty("dimension", new Dimension(470, 150));
        stringField.getComponent().addPropertyChangeListener("options", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String[] newOptions = (String[]) evt.getNewValue();
                String[] oldOptions = (String[]) evt.getOldValue();
                // // added
                // if( newOptions.length > oldOptions.length )
                // {
                // // new element is always added to the end
                // String[] newValue = ( String[] )evt.getNewValue();
                // String itemToAdd = newValue[newValue.length - 1];
                // xpathList.addXpathList( itemToAdd );
                // }
                // // removed
                // if( newOptions.length < oldOptions.length )
                // {
                // /*
                // * items with same index should me same. first one in oldOptions
                // * that does not match is element that is removed.
                // */
                // for( int cnt = 0; cnt < oldOptions.length; cnt++ )
                // {
                // if( cnt < newOptions.length )
                // {
                // if( newOptions[cnt] != oldOptions[cnt] )
                // {
                // xpathList.removeXpathList( cnt );
                // break;
                // }
                // }
                // else
                // {
                // // this is border case, last lement in array is removed.
                // xpathList.removeXpathList( oldOptions.length - 1 );
                // }
                // }
                // }

                xpathList.setXpathListArray(newOptions);
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

    @AForm(description = "XPath Injection Strings", name = "XPath Injection Strings")
    protected interface AdvancedSettings {

        @AField(description = "XPath Strings", name = "###Injection Strings", type = AFieldType.STRINGLIST)
        public final static String INJECTION_STRINGS = "###Injection Strings";

    }

    @Override
    protected void clear() {
        parameterMutations.clear();
        mutation = false;
    }
}
