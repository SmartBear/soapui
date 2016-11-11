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
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.boundary.AbstractBoundary;
import com.eviware.soapui.security.boundary.BoundaryRestrictionUtill;
import com.eviware.soapui.security.boundary.enumeration.EnumerationValues;
import com.eviware.soapui.support.SecurityScanUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BoundarySecurityScan extends AbstractSecurityScanWithProperties {

    public static final String TYPE = "BoundaryScan";
    public static final String NAME = "Boundary Scan";
    private static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";
    private RestrictionLabel restrictionLabel = new RestrictionLabel();

    StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

    public BoundarySecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent, String icon) {
        super(testStep, config, parent, icon);
    }

    @Override
    public JComponent getComponent() {
        JPanel panel = UISupport.createEmptyPanel(5, 75, 0, 5);
        panel.add(restrictionLabel.getJLabel(), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected void execute(SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context) {
        PropertyMutation mutation = popMutation(context);
        if (mutation != null) {

            updateRequestProperty(testStep, mutation);
            MessageExchange message = (MessageExchange) testStep.run((TestCaseRunner) securityTestRunner, context);
            createMessageExchange(mutation.getMutatedParameters(), message, context);
        }
    }

    @SuppressWarnings("unchecked")
    private PropertyMutation popMutation(SecurityTestRunContext context) {
        Stack<PropertyMutation> requestMutationsStack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        return requestMutationsStack.empty() ? null : requestMutationsStack.pop();
    }

    private void extractMutations(TestStep testStep, SecurityTestRunContext context) throws XmlException, Exception {
        strategy = getExecutionStrategy().getStrategy();

        XmlObjectTreeModel model = null;// getXmlObjectTreeModel( testStep );
        List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
        StringToStringMap stsmap = new StringToStringMap();
        for (SecurityCheckedParameter scp : scpList) {
            if (scp.isChecked() && scp.getXpath().trim().length() > 0) {
                XmlTreeNode[] treeNodes = null;

                if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                    stsmap = new StringToStringMap();
                    model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                } else {
                    if (model == null) {
                        model = SecurityScanUtil.getXmlObjectTreeModel(testStep, scp);
                    }

                }
                treeNodes = model.selectTreeNodes(context.expand(scp.getXpath()));

                if (treeNodes.length > 0) {
                    XmlTreeNode mynode = treeNodes[0];

                    if (mynode.isLeaf()) {
                        if (mynode.getSchemaType() != null && mynode.getSchemaType().getEnumerationValues() != null
                                && mynode.getSchemaType().getEnumerationValues().length > 0) {
                            EnumerationValues nodeInfo = new EnumerationValues(mynode.getSchemaType().getBaseType()
                                    .getShortJavaName());
                            for (XmlAnySimpleType s : mynode.getSchemaType().getEnumerationValues()) {
                                nodeInfo.addValue(s.getStringValue());
                            }
                            updateEnumNodeValue(mynode, nodeInfo);
                            stsmap.put(scp.getLabel(), mynode.getNodeText());
                            // addToUpdated( context, scp.getLabel(),
                            // mynode.getNodeText() );
                            if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                                PropertyMutation pm = new PropertyMutation();
                                pm.setPropertyName(scp.getName());
                                pm.setPropertyValue(model.getXmlObject().toString());
                                stsmap = new StringToStringMap();
                                stsmap.put(scp.getLabel(), mynode.getNodeText());
                                pm.setMutatedParameters(stsmap);
                                addMutation(context, pm);
                            }
                        } else {
                            SchemaTypeImpl simpleType = (SchemaTypeImpl) mynode.getSchemaType();
                            XmlObjectTreeModel model2 = new XmlObjectTreeModel(simpleType.getTypeSystem(),
                                    simpleType.getParseObject());
                            extractRestrictions(model2, context, mynode, model, scp, stsmap);
                        }
                    }
                }
            }
        }

        if (model != null && strategy.equals(StrategyTypeConfig.ALL_AT_ONCE)) {
            PropertyMutation pm = new PropertyMutation();
            pm.setPropertyName("Request");
            pm.setPropertyValue(model.getXmlObject().toString());
            pm.setMutatedParameters(stsmap);
            addMutation(context, pm);
        }
    }

    @SuppressWarnings("unchecked")
    private void addMutation(SecurityTestRunContext context, PropertyMutation mutation) {
        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        stack.push(mutation);
    }

    private void updateRequestProperty(TestStep testStep, PropertyMutation mutation) {
        testStep.getProperty(mutation.getPropertyName()).setValue(mutation.getPropertyValue());

    }

    public String extractRestrictions(XmlObjectTreeModel model2, SecurityTestRunContext context,
                                      XmlTreeNode nodeToUpdate, XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap)
            throws XmlException, Exception {
        getNextChild(model2.getRootNode(), context, nodeToUpdate, model, scp, stsmap);

        return nodeToUpdate.getXmlObject().toString();
    }

    private void getNextChild(XmlTreeNode node, SecurityTestRunContext context, XmlTreeNode nodeToUpdate,
                              XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap) {
        String baseType = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            XmlTreeNode mynode = node.getChild(i);

            if ("xsd:restriction".equals(mynode.getParent().getNodeName())) {
                if (mynode.getNodeName().equals("@base")) {
                    baseType = mynode.getNodeText();
                } else {
                    createMutation(baseType, mynode, context, nodeToUpdate, model, scp, stsmap);
                }
            }
            getNextChild(mynode, context, nodeToUpdate, model, scp, stsmap);
        }
    }

    private void createMutation(String baseType, XmlTreeNode mynode, SecurityTestRunContext context,
                                XmlTreeNode nodeToUpdate, XmlObjectTreeModel model, SecurityCheckedParameter scp, StringToStringMap stsmap) {

        String value = null;
        String nodeName = mynode.getNodeName();
        String nodeValue = mynode.getChild(0).getNodeText();
        value = AbstractBoundary.outOfBoundaryValue(baseType, nodeName, nodeValue);

        if (value != null) {
            nodeToUpdate.setValue(1, value);
            PropertyMutation pm = new PropertyMutation();
            pm.setPropertyName(scp.getName());
            pm.setPropertyValue(model.getXmlObject().toString());

            if (strategy.equals(StrategyTypeConfig.ONE_BY_ONE)) {
                stsmap = new StringToStringMap();
                stsmap.put(scp.getLabel() + " (" + nodeName + "='" + nodeValue + "') ", value);
                pm.setMutatedParameters(stsmap);
                addMutation(context, pm);
            } else {
                stsmap.put(scp.getLabel() + " (" + nodeName + "='" + nodeValue + "') ", value);
            }

        } else {
            SoapUI.log.warn("No out of boundary value is created for restriction " + nodeName + " of baseType:"
                    + baseType);
        }
    }

    public void updateEnumNodeValue(XmlTreeNode mynode, EnumerationValues enumerationValues) {
        int size = EnumerationValues.maxLengthStringSize(enumerationValues.getValuesList());
        String value = EnumerationValues.createOutOfBoundaryValue(enumerationValues, size);
        if (value != null) {
            mynode.setValue(1, value);
        }
    }

    /**
     * this method uses context to handle list of mutated request
     */
    @SuppressWarnings("unchecked")
    protected boolean hasNext(TestStep testStep, SecurityTestRunContext context) {
        if (!context.hasProperty(REQUEST_MUTATIONS_STACK)) {
            Stack<PropertyMutation> requestMutationsList = new Stack<PropertyMutation>();
            context.put(REQUEST_MUTATIONS_STACK, requestMutationsList);
            try {
                extractMutations(testStep, context);
            } catch (Exception e) {
                SoapUI.logError(e);
            }

            return checkIfStackHasContent(context);
        }

        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        if (stack.empty()) {
            context.remove(REQUEST_MUTATIONS_STACK);
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean checkIfStackHasContent(SecurityTestRunContext context) {
        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        if (stack.empty()) {
            context.remove(REQUEST_MUTATIONS_STACK);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public String getConfigDescription() {
        return "Configuration for Boundary Security Scan";
    }

    @Override
    public String getConfigName() {
        return "Configuration for Boundary Security Scan";
    }

    @Override
    public String getHelpURL() {
        return "http://www.soapui.org/Security/boundary-scan.html";
    }

    public class RestrictionLabel {
        private String text = "";
        private JLabel jlabel = new JLabel();
        private int limit = 70;

        {
            setJlabel(text);
        }

        public void setJlabel(String text) {
            text = text.replace("[", "");
            text = text.replace("]", "");
            if (text.length() > limit) {
                jlabel.setToolTipText(text.length() < 250 ? text : text.substring(0, 249) + " ... ");
                jlabel.setText(text.substring(0, limit - 5) + " ... ");
            } else {
                jlabel.setText(text);
            }
        }

        public JLabel getJLabel() {
            return jlabel;
        }
    }

    public void refreshRestrictionLabel(int row) {
        if (row == -1) {
            restrictionLabel.setJlabel("- no parameter selected -");
            return;
        }

        SecurityCheckedParameter parameter = getParameterAt(row);
        if (parameter == null) {
            return;
        }

        String name = parameter.getName();
        String xpath = parameter.getXpath();
        TestProperty tp = getTestStep().getProperty(name);
        XmlObjectTreeModel xmlObjectTreeModel = null;
        if (tp.getSchemaType() != null && XmlUtils.seemsToBeXml(tp.getValue())) {
            try {
                // xmlObjectTreeModel = new XmlObjectTreeModel(
                // tp.getSchemaType().getTypeSystem(),
                // XmlObject.Factory.parse( tp.getValue() ) );
                xmlObjectTreeModel = new XmlObjectTreeModel(tp.getSchemaType().getTypeSystem(),
                        XmlUtils.createXmlObject(tp.getValue()));
            } catch (XmlException e) {
                SoapUI.logError(e);
            }

            XmlTreeNode[] treeNodes = xmlObjectTreeModel.selectTreeNodes(xpath);

            if (treeNodes.length == 0) {
                restrictionLabel.setJlabel("<html><pre>    </pre></html>");
                return;
            }
            List<String> list = null;
            if (treeNodes[0].getSchemaType() != null && treeNodes[0].getSchemaType().getEnumerationValues() != null) {
                list = BoundaryRestrictionUtill.extractEnums(treeNodes[0]);
                restrictionLabel.setJlabel(list.toString().replaceFirst(",", ""));
            } else {
                SchemaTypeImpl simpleType = (SchemaTypeImpl) treeNodes[0].getSchemaType();
                if (simpleType != null && !simpleType.isNoType()) {
                    XmlObjectTreeModel model2 = new XmlObjectTreeModel(simpleType.getTypeSystem(),
                            simpleType.getParseObject());
                    list = BoundaryRestrictionUtill.getRestrictions(model2.getRootNode(), new ArrayList<String>());
                    if (list.isEmpty()) {
                        list.add("No restrictions in schema are specified for this parameter!");
                    }
                    restrictionLabel.setJlabel(list.toString());
                } else {
                    restrictionLabel.setJlabel("parameter is missing type in schema");
                }
            }

        } else {
            restrictionLabel.setJlabel("- no parameter selected -");
        }
    }

}
