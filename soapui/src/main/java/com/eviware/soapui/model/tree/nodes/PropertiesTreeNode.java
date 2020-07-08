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

package com.eviware.soapui.model.tree.nodes;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesTreeNode<T extends ModelItem> extends AbstractModelItemTreeNode<T> {
    private List<PropertyTreeNode> propertyNodes = new ArrayList<PropertyTreeNode>();
    private Map<String, PropertyTreeNode> propertyMap = new HashMap<String, PropertyTreeNode>();
    private InternalTestPropertyListener testPropertyListener;
    private final TestPropertyHolder holder;

    public PropertiesTreeNode(T modelItem, ModelItem parentItem, TestPropertyHolder holder, SoapUITreeModel treeModel) {
        super(modelItem, parentItem, treeModel);
        this.holder = holder;

        for (String name : holder.getPropertyNames()) {
            PropertyTreeNode propertyTreeNode = new PropertyTreeNode(holder.getProperty(name), getModelItem(), holder,
                    treeModel);
            propertyNodes.add(propertyTreeNode);
            propertyMap.put(name, propertyTreeNode);
            getTreeModel().mapModelItem(propertyTreeNode);
        }

        testPropertyListener = new InternalTestPropertyListener();
        holder.addTestPropertyListener(testPropertyListener);
    }

    public static PropertiesTreeNode<?> createDefaultPropertiesNode(TestModelItem modelItem, SoapUITreeModel treeModel) {
        return new PropertiesTreeNode<PropertiesModelItem>(
                new PropertiesModelItem(modelItem), modelItem, modelItem, treeModel);
    }

    public int getChildCount() {
        return getTreeModel().isShowProperties() ? propertyNodes.size() : 0;
    }

    public SoapUITreeNode getChildNode(int index) {
        return propertyNodes.get(index);
    }

    public int getIndexOfChild(Object child) {
        return propertyNodes.indexOf(child);
    }

    public void release() {
        super.release();

        holder.removeTestPropertyListener(testPropertyListener);

        for (PropertyTreeNode node : propertyNodes) {
            getTreeModel().unmapModelItem(node.getModelItem());
        }

        propertyNodes.clear();
        propertyMap.clear();
    }

    private class InternalTestPropertyListener implements TestPropertyListener {
        public void propertyAdded(String name) {
            PropertyTreeNode propertyTreeNode = new PropertyTreeNode(holder.getProperty(name), getModelItem(), holder,
                    getTreeModel());
            propertyNodes.add(propertyTreeNode);
            propertyMap.put(name, propertyTreeNode);
            getTreeModel().notifyNodeInserted(propertyTreeNode);

            if (getModelItem() instanceof PropertiesModelItem) {
                ((PropertiesModelItem) getModelItem()).updateName();
            }
        }

        public void propertyRemoved(String name) {
            SoapUITreeNode treeNode = getTreeModel().getTreeNode(propertyMap.get(name).getModelItem());
            if (propertyNodes.contains(treeNode)) {
                getTreeModel().notifyNodeRemoved(treeNode);
                propertyNodes.remove(treeNode);
                propertyMap.remove(name);

                if (getModelItem() instanceof PropertiesModelItem) {
                    ((PropertiesModelItem) getModelItem()).updateName();
                }
            } else {
                throw new RuntimeException("Removing unkown property");
            }
        }

        public void propertyRenamed(String oldName, String newName) {
            PropertyTreeNode propertyTreeNode = propertyMap.remove(oldName);
            propertyMap.put(newName, propertyTreeNode);
            propertyTreeNode.getModelItem().setName(PropertyTreeNode.buildName(holder.getProperty(newName)));
        }

        public void propertyValueChanged(String name, String oldValue, String newValue) {
            PropertyTreeNode propertyTreeNode = propertyMap.get(name);
            if (propertyTreeNode != null) {
                propertyTreeNode.getModelItem().setName(PropertyTreeNode.buildName(holder.getProperty(name)));
            }
        }

        public void propertyMoved(String name, int oldIndex, int newIndex) {
            PropertyTreeNode node = propertyNodes.get(oldIndex);
            getTreeModel().notifyNodeRemoved(node, false);

            propertyNodes.remove(oldIndex);
            if (newIndex >= propertyNodes.size()) {
                propertyNodes.add(node);
            } else {
                propertyNodes.add(newIndex, node);
            }

            getTreeModel().notifyNodeInserted(node);
        }
    }

    public ActionList getActions() {
        if (getModelItem() instanceof PropertiesModelItem && holder instanceof MutableTestPropertyHolder) {
            DefaultActionList actions = new DefaultActionList();
            actions.addAction(new AddPropertyAction());
            return actions;
        }

        return super.getActions();
    }

    private class AddPropertyAction extends AbstractAction {
        public AddPropertyAction() {
            super("Add Property");
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(Action.SHORT_DESCRIPTION, "Adds a property to the property list");
        }

        public void actionPerformed(ActionEvent e) {
            String name = UISupport.prompt("Specify unique property name", "Add Property", "");
            if (StringUtils.hasContent(name)) {
                if (holder.hasProperty(name)) {
                    UISupport.showErrorMessage("Property name [" + name + "] already exists..");
                    return;
                }

                ((MutableTestPropertyHolder) holder).addProperty(name);
            }
        }
    }
}
