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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.tree.AbstractModelItemTreeNode;
import com.eviware.soapui.model.tree.SoapUITreeModel;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class PropertyTreeNode extends AbstractModelItemTreeNode<PropertyModelItem> {
    private boolean readOnly;
    private final TestProperty property;

    protected PropertyTreeNode(TestProperty property, ModelItem parent, TestPropertyHolder holder,
                               SoapUITreeModel treeModel) {
        super(new PropertyModelItem(property, property.isReadOnly()), parent, treeModel);
        this.property = property;
        readOnly = property.isReadOnly();
    }

    public static String buildName(TestProperty property) {
        String name = property.getName();
        String value = property.getValue();
        if (value == null) {
            value = "";
        } else {
            if (value.length() > 12) {
                value = value.substring(0, 12) + "..";
            }

            value = "'" + value + "'";
        }

        return name + " : " + value;
    }

    @Override
    public ActionList getActions() {
        if (!readOnly) {
            DefaultActionList actions = new DefaultActionList();
            SetPropertyValueAction setPropertyValueAction = new SetPropertyValueAction();
            actions.addAction(setPropertyValueAction);
            actions.setDefaultAction(setPropertyValueAction);
            return actions;
        } else {
            return super.getActions();
        }
    }

    private class SetPropertyValueAction extends AbstractAction {
        public SetPropertyValueAction() {
            super("Set Value");
            putValue(Action.SHORT_DESCRIPTION, "Prompts to set the value of this property");
        }

        public void actionPerformed(ActionEvent e) {
            String value = UISupport.prompt("Specify property value", "Set Value", property.getValue());
            if (StringUtils.hasContent(value)) {
                property.setValue(value);
            }
        }
    }
}
