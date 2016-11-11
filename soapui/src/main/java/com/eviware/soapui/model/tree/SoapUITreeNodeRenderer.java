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

package com.eviware.soapui.model.tree;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.Tools;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

/**
 * TreeCellRenderer for SoapUITreeNodes
 *
 * @author Ole.Matzura
 */

public class SoapUITreeNodeRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        ModelItem modelItem = ((SoapUITreeNode) value).getModelItem();
        if (modelItem instanceof Project) {
            Project project = (Project) modelItem;
            if (!project.isOpen() && !project.isDisabled()) {
                leaf = false;
                expanded = false;
            }
        }

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        ImageIcon icon = modelItem.getIcon();
        setIcon(icon);

        if (modelItem instanceof TestStep && ((TestStep) modelItem).isDisabled()) {
            setEnabled(false);
        } else if (modelItem instanceof TestCase && ((TestCase) modelItem).isDisabled()) {
            setEnabled(false);
        } else if (modelItem instanceof TestSuite && ((TestSuite) modelItem).isDisabled()) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }

        String toolTipText = tree.getToolTipText();
        if (toolTipText == null) {
            String description = modelItem.getDescription();
            if (description == null || description.trim().length() == 0) {
                description = modelItem.getName();
            }

            if (description != null && description.trim().indexOf('\n') > 0) {
                description = Tools.convertToHtml(description);
            }

            setToolTipText(description);
        } else {
            setToolTipText(toolTipText.length() > 0 ? toolTipText : null);
        }

        return this;
    }
}
