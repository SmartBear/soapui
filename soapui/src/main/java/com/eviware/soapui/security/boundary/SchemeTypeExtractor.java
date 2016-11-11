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

package com.eviware.soapui.security.boundary;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;

import javax.swing.tree.TreePath;
import javax.wsdl.Definition;
import java.util.TreeMap;

public class SchemeTypeExtractor {

    private WsdlRequest request;
    private TreeMap<String, NodeInfo> nodes;

    public SchemeTypeExtractor(WsdlTestRequest request) {
        this.request = request;
    }

    public SchemeTypeExtractor(TestStep testStep) {
        if (testStep instanceof WsdlTestRequestStep) {
            request = ((WsdlTestRequestStep) testStep).getHttpRequest();
        }

    }

    public String getTypeFor(String name) throws Exception {
        NodeInfo node = nodes.get(name);
        if (node == null) {
            return null;
        }
        return node.getType();
    }

    public TreeMap<String, NodeInfo> getParams() {
        return nodes;
    }

    public TreeMap<String, NodeInfo> extract() throws XmlException, Exception {
        // XmlObjectTreeModel model = new XmlObjectTreeModel(
        // request.getOperation().getInterface().getDefinitionContext()
        // .getSchemaTypeSystem(), XmlObject.Factory.parse(
        // request.getRequestContent() ) );
        XmlObjectTreeModel model = new XmlObjectTreeModel(request.getOperation().getInterface().getDefinitionContext()
                .getSchemaTypeSystem(), XmlUtils.createXmlObject(request.getRequestContent()));

        nodes = getElements(model.getRootNode());

        // these two lines used for testing
        for (NodeInfo node : nodes.values()) {
            printNode(node);
        }
        return nodes;
    }

    /*
     * used for testing
     */
    private void printNode(NodeInfo node) {
        XmlTreeNode mynode = node.node;

        System.out.println(mynode.getNodeName());
        System.out.println(node.getType());
        System.out.println(node.xpath);
    }

    public TreeMap<String, NodeInfo> getVariableSet() {
        return nodes;
    }

    /**
     * Recursive look for leafs which types are primitive type. Those elements
     * actualy carry values.
     */
    TreeMap<String, NodeInfo> getElements(XmlTreeNode rootXmlTreeNode) {
        TreeMap<String, NodeInfo> result = new TreeMap<String, NodeInfo>();
        for (int cnt = 0; cnt < rootXmlTreeNode.getChildCount(); cnt++) {
            XmlTreeNode xmlTreeNodeChild = (XmlTreeNode) rootXmlTreeNode.getChild(cnt);

            if (xmlTreeNodeChild.getChildCount() > 0) {
                result.putAll(getElements(rootXmlTreeNode.getChild(cnt)));
            } else {
                if (xmlTreeNodeChild.getSchemaType() != null && xmlTreeNodeChild.getSchemaType().isPrimitiveType()) {
                    result.put(xmlTreeNodeChild.getDomNode().getLocalName(), new NodeInfo(rootXmlTreeNode.getChild(cnt)));
                }
            }
        }
        return result;
    }

    private String declareXPathNamespaces(Definition definition) {
        StringBuilder result = new StringBuilder();
        for (Object shortName : definition.getNamespaces().keySet()) {
            result.append("declare namespace ").append(shortName.toString()).append("=\'")
                    .append(definition.getNamespaces().get(shortName).toString()).append("\';");
        }
        return result.toString();
    }

    public class NodeInfo {

        private String name;
        private String text;
        private TreePath treePath;
        private XmlTreeNode node;
        private String xpath;
        private String type;
        private boolean selected = false;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public TreePath getTreePath() {
            return treePath;
        }

        public String getXPath() {
            return xpath;

        }

        public NodeInfo(String name, String text, TreePath treePath, XmlTreeNode node, String xpath) {
            this.name = name;
            this.text = text;
            this.treePath = treePath;
            this.node = node;
            this.xpath = xpath;

        }

        public NodeInfo(XmlTreeNode child) {
            this.name = child.getNodeName();
            this.text = child.getNodeText();
            this.treePath = child.getTreePath();
            this.type = child.getSchemaType().toString();
            this.node = child;
            this.xpath = XmlUtils.createXPath(child.getDomNode(), true, false, false, null);
        }

        public String getType() {
            return type;
        }

        public String getSimpleName() {
            return node.getDomNode().getLocalName();
        }
    }

}
