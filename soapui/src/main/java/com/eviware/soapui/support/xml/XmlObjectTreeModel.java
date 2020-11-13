/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.support.xml;

import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.types.StringToStringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlObjectTreeModel implements TreeTableModel {
    private XmlObject xmlObject;
    private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
    private XmlCursor cursor;
    private Map<XmlObject, XmlTreeNode> treeNodeMap = new HashMap<XmlObject, XmlTreeNode>();

    public final static Class<?> hierarchicalColumnClass = TreeTableModel.class;
    private SchemaTypeSystem typeSystem;
    private RootXmlTreeNode root;
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(XmlObjectTreeModel.class);

    public XmlObjectTreeModel(XmlObject xmlObject) {
        this(XmlBeans.getBuiltinTypeSystem(), xmlObject);
    }

    public XmlObjectTreeModel() {
        this(XmlObject.Factory.newInstance());
    }

    public XmlObjectTreeModel(SchemaTypeSystem typeSystem, XmlObject xmlObject) {
        if (typeSystem == null) {
            typeSystem = XmlBeans.getBuiltinTypeSystem();
        }

        this.typeSystem = typeSystem;
        this.xmlObject = xmlObject;
        init();
    }

    public XmlObjectTreeModel(SchemaTypeSystem typeSystem) {
        this(typeSystem, XmlObject.Factory.newInstance());
    }

    private void init() {
        cursor = null;

        if (xmlObject != null) {
            cursor = xmlObject.newCursor();
            cursor.toStartDoc();
        }

        root = new RootXmlTreeNode(cursor);
    }

    public SchemaTypeSystem getTypeSystem() {
        return typeSystem;
    }

    public void setTypeSystem(SchemaTypeSystem typeSystem) {
        if (typeSystem == null) {
            typeSystem = XmlBeans.getBuiltinTypeSystem();
        }

        this.typeSystem = typeSystem;
    }

    public XmlObject getXmlObject() {
        return xmlObject;
    }

    public void setXmlObject(XmlObject xmlObject) {
        if (cursor != null) {
            cursor.dispose();
        }

        this.xmlObject = xmlObject;
        init();

        XmlTreeNode xmlTreeNode = ((XmlTreeNode) getRoot());
        fireTreeStructureChanged(xmlTreeNode);
    }

    protected void fireTreeStructureChanged(XmlTreeNode rootNode) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(new XmlTreeTableModelEvent(this, rootNode.getTreePath(), -1));
        }
    }

    public Class<?> getColumnClass(int arg0) {
        return arg0 == 0 ? hierarchicalColumnClass : XmlTreeNode.class;
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int arg0) {
        return null;
    }

    public Object getValueAt(Object arg0, int arg1) {
        return arg0; // ((XmlTreeNode)arg0).getValue( arg1 );
    }

    public boolean isCellEditable(Object arg0, int arg1) {
        return ((XmlTreeNode) arg0).isEditable(arg1);
    }

    public void setValueAt(Object arg0, Object arg1, int arg2) {
        XmlTreeNode treeNode = (XmlTreeNode) arg1;
        if (treeNode.setValue(arg2, arg0)) {
            fireTreeNodeChanged(treeNode, arg2);
        }
    }

    protected void fireTreeNodeChanged(XmlTreeNode treeNode, int column) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(new XmlTreeTableModelEvent(this, treeNode.getTreePath(), column));
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public Object getChild(Object parent, int index) {
        return ((XmlTreeNode) parent).getChild(index);
    }

    public int getChildCount(Object parent) {
        return ((XmlTreeNode) parent).getChildCount();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((XmlTreeNode) parent).getIndexOfChild((XmlTreeNode) child);
    }

    public Object getRoot() {
        return getRootNode();
    }

    public RootXmlTreeNode getRootNode() {
        return root;
    }

    public boolean isLeaf(Object node) {
        return ((XmlTreeNode) node).isLeaf();
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    private class TreeBookmark extends XmlCursor.XmlBookmark {
    }

    public interface XmlTreeNode {
        public int getChildCount();

        public XmlTreeNode getChild(int ix);

        public int getIndexOfChild(XmlTreeNode childNode);

        public String getNodeName();

        public String getNodeText();

        public boolean isEditable(int column);

        public boolean isLeaf();

        public boolean setValue(int column, Object value);

        public XmlLineNumber getNodeLineNumber();

        public XmlLineNumber getValueLineNumber();

        public XmlObject getXmlObject();

        public Node getDomNode();

        public TreePath getTreePath();

        public XmlTreeNode getParent();

        public SchemaType getSchemaType();

        public String getDocumentation();
    }

    private abstract class AbstractXmlTreeNode implements XmlTreeNode {
        protected Node node;
        protected TreeBookmark bm;
        private final XmlTreeNode parent;
        private XmlLineNumber lineNumber;
        protected SchemaType schemaType;
        protected String documentation;

        @SuppressWarnings("unchecked")
        protected AbstractXmlTreeNode(XmlCursor cursor, XmlTreeNode parent) {
            this.parent = parent;

            if (cursor != null) {
                node = cursor.getDomNode();

                ArrayList list = new ArrayList();
                cursor.getAllBookmarkRefs(list);

                for (Object o : list) {
                    if (o instanceof XmlLineNumber) {
                        lineNumber = (XmlLineNumber) o;
                    }
                }

                bm = new TreeBookmark();
                cursor.setBookmark(bm);

                treeNodeMap.put(cursor.getObject(), this);
            }
        }

        protected SchemaType findSchemaType() {
            if (cursor == null) {
                return null;
            }

            positionCursor(cursor);

            SchemaType resultType = null;
            XmlObject xo = cursor.getObject();
            if (xo != null) {
                Node domNode = xo.getDomNode();

                // check for xsi:type
                if (domNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elm = (Element) domNode;
                    String xsiType = elm.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
                    if (xsiType != null && xsiType.length() > 0) {
                        resultType = findXsiType(xsiType);
                    }
                }

                if (resultType == null) {
                    resultType = typeSystem.findType(xo.schemaType().getName());
                }

                if (resultType == null) {
                    resultType = xo.schemaType();
                }

                if (resultType.isNoType()) {
                    QName nm = cursor.getName();

                    if (parent != null && parent.getSchemaType() != null) {
                        SchemaType parentSchemaType = parent.getSchemaType();
                        SchemaParticle contentModel = parentSchemaType.getContentModel();

                        if (contentModel != null) {
                            SchemaParticle[] children = contentModel.getParticleChildren();

                            for (int c = 0; children != null && c < children.length; c++) {
                                if (nm.equals(children[c].getName())) {
                                    resultType = children[c].getType();
                                    documentation = SchemaUtils.getDocumentation(resultType);
                                    break;
                                }
                            }

                            if (resultType.isNoType() && nm.equals(contentModel.getName())) {
                                resultType = contentModel.getType();
                            }

                            if (resultType.isNoType()) {
                                SchemaType[] anonymousTypes = parentSchemaType.getAnonymousTypes();
                                for (int c = 0; anonymousTypes != null && c < anonymousTypes.length; c++) {
                                    QName name = anonymousTypes[c].getName();
                                    if (name != null && name.equals(nm)) {
                                        resultType = anonymousTypes[c];
                                        break;
                                    } else if (anonymousTypes[c].getContainerField().getName().equals(nm)) {
                                        resultType = anonymousTypes[c].getContainerField().getType();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (resultType.isNoType()) {
                        SchemaGlobalElement elm = typeSystem.findElement(nm);
                        if (elm != null) {
                            resultType = elm.getType();
                        } else if (typeSystem.findDocumentType(nm) != null) {
                            resultType = typeSystem.findDocumentType(nm);
                        }
                    }
                }
            }

            if (resultType == null) {
                resultType = XmlAnyTypeImpl.type;
            }

            if (documentation == null) {
                documentation = SchemaUtils.getDocumentation(resultType);
            }

            return resultType;
        }

        @SuppressWarnings("unused")
        protected String getUserInfo(SchemaType schemaType) {
            if (schemaType.getAnnotation() != null) {
                XmlObject[] userInformation = schemaType.getAnnotation().getUserInformation();
                if (userInformation != null && userInformation.length > 0) {
                    return userInformation[0].toString(); // XmlUtils.getElementText(
                    // ( Element )
                    // userInformation[0].getDomNode());
                }
            }

            return null;
        }

        public String getDocumentation() {
            return documentation;
        }

        private SchemaType findXsiType(String xsiType) {
            SchemaType resultType;
            int ix = xsiType.indexOf(':');
            QName name = null;

            if (ix == -1) {
                name = new QName(xsiType);
                resultType = typeSystem.findType(name);
            } else {
                StringToStringMap map = new StringToStringMap();
                cursor.getAllNamespaces(map);

                name = new QName(map.get(xsiType.substring(0, ix)), xsiType.substring(ix + 1));
                resultType = typeSystem.findType(name);
            }

            return resultType;
        }

        public XmlTreeNode getParent() {
            return parent;
        }

        protected void positionCursor(XmlCursor cursor) {
            cursor.toBookmark(bm);
        }

        public XmlTreeNode getChild(int ix) {
            return null;
        }

        public int getChildCount() {
            return 0;
        }

        public int getIndexOfChild(XmlTreeNode childNode) {
            return -1;
        }

        @SuppressWarnings("unused")
        public Object getValue(int column) {
            if (column == 0) {
                return getNodeName();
            } else if (column == 1) {
                return getNodeText();
            }

            return null;
        }

        public Node getDomNode() {
            return node;
        }

        public String getNodeName() {
            return node == null ? null : node.getNodeName();
        }

        public String getNodeText() {
            if (node == null) {
                return null;
            }

            String nodeValue = node.getNodeValue();
            return nodeValue == null ? null : nodeValue.trim();
        }

        public boolean isEditable(int column) {
            return false;
        }

        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        public boolean setValue(int column, Object value) {
            return false;
        }

        public String toString() {
            return getNodeName();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof AbstractXmlTreeNode) {
                return ((AbstractXmlTreeNode) obj).node == this.node;
            } else {
                return super.equals(obj);
            }
        }

        public XmlLineNumber getNodeLineNumber() {
            return lineNumber;
        }

        public XmlLineNumber getValueLineNumber() {
            return lineNumber;
        }

        public XmlObject getXmlObject() {
            if (cursor != null && cursor.toBookmark(bm)) {
                XmlObject object = cursor.getObject();

                if (object != null) {
                    return object;
                } else if (parent != null) {
                    return parent.getXmlObject();
                }
            }

            return null;
        }

        public TreePath getTreePath() {
            List<XmlTreeNode> nodes = new ArrayList<XmlTreeNode>();
            nodes.add(this);

            XmlTreeNode node = this;

            while (node.getParent() != null) {
                nodes.add(0, node.getParent());
                node = node.getParent();
            }

            return new TreePath(nodes.toArray());
        }

        public SchemaType getSchemaType() {
            if (schemaType == null) {
                schemaType = findSchemaType();
            }

            return schemaType;
        }
    }

    public class RootXmlTreeNode extends AbstractXmlTreeNode {
        private ElementXmlTreeNode rootNode;

        protected RootXmlTreeNode(XmlCursor cursor) {
            super(cursor, null);

            if (cursor != null) {
                cursor.toFirstContentToken();
                rootNode = new ElementXmlTreeNode(cursor, this);
            }
        }

        public XmlTreeNode getChild(int ix) {
            return ix == 0 ? rootNode : null;
        }

        public int getChildCount() {
            return rootNode == null ? 0 : 1;
        }

        public int getIndexOfChild(XmlTreeNode childNode) {
            return childNode == rootNode ? 0 : -1;
        }
    }

    public class ElementXmlTreeNode extends AbstractXmlTreeNode {
        private LinkedList<XmlTreeNode> elements = new LinkedList<XmlTreeNode>();
        private TextXmlTreeNode textTreeNode;
        private int attrCount;

        protected ElementXmlTreeNode(XmlCursor cursor, XmlTreeNode parent) {
            super(cursor, parent);

            TokenType token = cursor.toNextToken();
            while (token == TokenType.ATTR || token == TokenType.NAMESPACE) {
                if (token == TokenType.ATTR) {
                    elements.add(new AttributeXmlTreeNode(cursor, this));
                }

                token = cursor.toNextToken();
            }

            attrCount = elements.size();

            positionCursor(cursor);
            cursor.toFirstContentToken();

            while (true) {
                while (cursor.isComment() || cursor.isProcinst()) {
                    cursor.toNextToken();
                }

                if (cursor.isContainer()) {
                    elements.add(new ElementXmlTreeNode(cursor, this));
                    cursor.toEndToken();
                    cursor.toNextToken();
                }

                if (cursor.isText()) {
                    elements.add(new TextXmlTreeNode(cursor, this));
                    cursor.toNextToken();
                }

                if (cursor.isEnd() || cursor.isEnddoc()) {
                    break;
                }
            }

            if (elements.size() == attrCount + 1 && (elements.get(attrCount) instanceof TextXmlTreeNode)) {
                textTreeNode = (TextXmlTreeNode) elements.remove(attrCount);
            } else {
                for (int c = attrCount; c < elements.size(); c++) {
                    if (elements.get(c) instanceof TextXmlTreeNode) {
                        TextXmlTreeNode treeNode = (TextXmlTreeNode) elements.get(c);
                        String text = treeNode.getNodeText().trim();
                        if (text.length() == 0) {
                            elements.remove(c);
                            c--;
                        }
                    }
                }
            }

            positionCursor(cursor);
        }

        public XmlTreeNode getChild(int ix) {
            return elements.get(ix);
        }

        public boolean isEditable(int column) {
            return column == 1 && elements.size() == attrCount;
        }

        public boolean setValue(int column, Object value) {
            if (column == 1) {
                if (textTreeNode != null) {
                    textTreeNode.setValue(1, value);
                } else {
                    positionCursor(cursor);
                    cursor.toEndToken();
                    cursor.insertChars(value.toString());
                    positionCursor(cursor);
                    cursor.toFirstContentToken();

                    textTreeNode = new TextXmlTreeNode(cursor, this);
                }
            }
            return column == 1;
        }

        public int getChildCount() {
            return elements.size();
        }

        public int getIndexOfChild(XmlTreeNode childNode) {
            return elements.indexOf(childNode);
        }

        public String getNodeText() {
            return textTreeNode == null ? "" : textTreeNode.getNodeText();
        }

        public XmlLineNumber getValueLineNumber() {
            return textTreeNode == null ? super.getValueLineNumber() : textTreeNode.getValueLineNumber();
        }
    }

    public class AttributeXmlTreeNode extends AbstractXmlTreeNode {
        private boolean checkedType;

        protected AttributeXmlTreeNode(XmlCursor cursor, ElementXmlTreeNode parent) {
            super(cursor, parent);
        }

        public String getNodeName() {
            return "@" + super.getNodeName();
        }

        public XmlLineNumber getNodeLineNumber() {
            return getParent().getNodeLineNumber();
        }

        public boolean isEditable(int column) {
            return column == 1;
        }

        public boolean setValue(int column, Object value) {
            if (column == 1) {
                node.setNodeValue(value.toString());
            }

            return column == 1;
        }

        public SchemaType getSchemaType() {
            if (schemaType == null && !checkedType) {
                SchemaType parentSchemaType = getParent().getSchemaType();
                if (parentSchemaType != null) {
                    positionCursor(cursor);
                    SchemaProperty attributeProperty = parentSchemaType.getAttributeProperty(cursor.getName());
                    if (attributeProperty != null) {
                        schemaType = attributeProperty.getType();
                        documentation = SchemaUtils.getDocumentation(schemaType);

                        // SchemaAnnotation annotation = schemaType.getAnnotation();
                        // if( annotation != null )
                        // {
                        // XmlObject[] userInformation =
                        // annotation.getUserInformation();
                        // if( userInformation != null && userInformation.length > 0 )
                        // {
                        // //userInformation[0].toString(); //XmlUtils.getElementText(
                        // ( Element ) userInformation[0].getDomNode());
                        // }
                        // }
                    }
                }

                checkedType = true;
            }

            return schemaType;
        }
    }

    public class TextXmlTreeNode extends AbstractXmlTreeNode {
        protected TextXmlTreeNode(XmlCursor cursor, ElementXmlTreeNode parent) {
            super(cursor, parent);
        }

        public boolean isEditable(int column) {
            return column == 1;
        }

        public boolean setValue(int column, Object value) {
            if (column == 1 && node != null) {
                node.setNodeValue(value.toString());
            }

            return column == 1;
        }

        public TreePath getTreePath() {
            return super.getTreePath().getParentPath();
        }
    }

    public TreePath findXmlTreeNode(int line, int column) {
        line++;

        XmlTreeNode treeNode = findXmlTreeNode(root, line, column);
        if (treeNode instanceof AttributeXmlTreeNode) {
            return treeNode.getParent().getTreePath();
        } else if (treeNode != null) {
            return treeNode.getTreePath();
        }

        return null;
    }

    private XmlTreeNode findXmlTreeNode(XmlTreeNode treeNode, int line, int column) {
        for (int c = 0; c < treeNode.getChildCount(); c++) {
            XmlTreeNode child = treeNode.getChild(c);
            XmlLineNumber ln = child.getNodeLineNumber();
            if (ln != null && (line < ln.getLine() || (line == ln.getLine() && column <= ln.getColumn()))) {
                if (c == 0) {
                    return treeNode;
                } else {
                    return findXmlTreeNode(treeNode.getChild(c - 1), line, column);
                }
            }
        }

        if (treeNode.getChildCount() > 0) {
            return findXmlTreeNode(treeNode.getChild(treeNode.getChildCount() - 1), line, column);
        }

        return treeNode;
    }

    public class XmlTreeTableModelEvent extends TreeModelEvent {
        private final int column;

        public XmlTreeTableModelEvent(Object source, Object[] path, int[] childIndices, Object[] children, int column) {
            super(source, path, childIndices, children);
            this.column = column;
        }

        public XmlTreeTableModelEvent(Object source, Object[] path, int column) {
            super(source, path);
            this.column = column;
        }

        public XmlTreeTableModelEvent(Object source, TreePath path, int[] childIndices, Object[] children, int column) {
            super(source, path, childIndices, children);
            this.column = column;
        }

        public XmlTreeTableModelEvent(Object source, TreePath path, int column) {
            super(source, path);
            this.column = column;
        }

        public int getColumn() {
            return column;
        }
    }

    public XmlTreeNode getXmlTreeNode(XmlObject object) {
        return treeNodeMap.get(object);
    }

    public XmlTreeNode[] selectTreeNodes(String xpath) {
        XmlObject[] nodes = xmlObject.selectPath(xpath);
        List<XmlTreeNode> result = new ArrayList<XmlTreeNode>();

        for (XmlObject xmlObject : nodes) {
            XmlTreeNode tn = getXmlTreeNode(xmlObject);
            if (tn != null) {
                result.add(tn);
            }
        }

        return result.toArray(new XmlTreeNode[result.size()]);
    }

    public void release() {
        typeSystem = null;
        treeNodeMap.clear();

        listeners.clear();
    }

    public int getHierarchicalColumn() {
        return 0;
    }
}
