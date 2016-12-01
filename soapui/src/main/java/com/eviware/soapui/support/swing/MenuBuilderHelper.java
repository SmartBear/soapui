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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.support.ShowDesktopPanelAction;
import com.eviware.soapui.impl.wsdl.actions.teststep.ToggleDisableTestStepAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.Component;


public class MenuBuilderHelper {
    public static JMenu getMenu(String name) {
        JMenuBar menuBar = SoapUI.getMenuBar();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            if (menuBar.getMenu(i).getText().equals(name)) {
                return menuBar.getMenu(i);
            }
        }
        return null;
    }

    public static JMenu buildMenuForWorkspace(JMenu menu, String actionGroup) {
        ActionList actions = buildActionsForActionGroup(actionGroup);
        if (menu.getText().equals(SoapUI.STEP)) {
            SoapUIActionMapping<WsdlTestStep> toggleDisabledActionMapping = null;
            DefaultActionMapping<WsdlTestStep> actionMapping = new DefaultActionMapping<WsdlTestStep>(
                    ShowDesktopPanelAction.SOAPUI_ACTION_ID, "ENTER", null, true, null);
            actionMapping.setName("Open Editor");
            actionMapping.setDescription("Opens the editor for this TestStep");
            SwingActionDelegate actionDelegate = new SwingActionDelegate(actionMapping, null);
            menu.add(actionDelegate);

            toggleDisabledActionMapping = new DefaultActionMapping<WsdlTestStep>(
                    ToggleDisableTestStepAction.SOAPUI_ACTION_ID, null, null, false, null);

            SwingActionDelegate actionDelegateToggle = new SwingActionDelegate(toggleDisabledActionMapping, null);
            menu.add(actionDelegateToggle);
        }
        ActionSupport.addActions(actions, menu);
        for (Component component : menu.getMenuComponents()) {
            activateMenuSubItems(component, false);
            component.setEnabled(false);
        }
        return menu;
    }

    protected static ActionList buildActionsForActionGroup(String actionGroup) {
        return ActionListBuilder.buildActions(actionGroup, null);
    }

    private static void activateMenuItems(String groupId, boolean activate) {
        JMenu menu = getMenu(groupId);
        for (Component component : menu.getMenuComponents()) {
            activateMenuSubItems(component, activate);
            component.setEnabled(activate);
        }
    }

    private static void activateMenuSubItems(Component component, boolean bEnable) {
        if (component instanceof JMenu) {
            for (Component curComponent : ((JMenu) component).getMenuComponents()) {
                curComponent.setEnabled(bEnable);
            }
        }
    }

    private static void buildMenu(ModelItem curModelItem, String menuName, SoapUITreeNode path) {
        String[] groupsId = {SoapUI.STEP, SoapUI.CASE, SoapUI.SUITE, SoapUI.PROJECT};
        if (curModelItem instanceof Workspace) {
            for (String groupId : groupsId) {
                activateMenuItems(groupId, false);
            }
        } else if (ModelSupport.isOneOf(curModelItem, WsdlTestSuite.class, WsdlTestCase.class,
                WsdlTestStep.class, WsdlProject.class)) {
            ActionList actionList = ActionListBuilder.buildActions(curModelItem);
            JMenu curMenu = MenuBuilderHelper.getMenu(menuName);
            curMenu.removeAll();
            ActionSupport.addActions(actionList, curMenu);
            for (String groupId : groupsId) {
                if (groupId.equals(menuName)) {
                    break;
                }
                activateMenuItems(groupId, false);
            }
            while (!(curModelItem.getParent() instanceof Workspace)) {
                curModelItem = curModelItem.getParent();
                ActionList parentActionList = ActionListBuilder.buildActions(curModelItem);
                JMenu parentMenu = MenuBuilderHelper.getMenu(getMenuNameForModelItem(curModelItem));
                parentMenu.removeAll();
                ActionSupport.addActions(parentActionList, parentMenu);
                activateMenuItems(getMenuNameForModelItem(curModelItem), true);
            }
        } else {
            SoapUITreeNode node = path.getParentTreeNode();
            curModelItem = node.getModelItem();
            while (!(curModelItem instanceof Workspace)) {
                if (curModelItem instanceof WsdlTestCase) {
                    buildMenu(curModelItem, SoapUI.CASE, null);
                    break;
                } else if (curModelItem instanceof WsdlProject) {
                    buildMenu(curModelItem, SoapUI.PROJECT, null);
                    break;
                }
                node = node.getParentTreeNode();
                curModelItem = node.getModelItem();
            }
        }
    }

    public static void buildTreeNodeMenu(SoapUITreeNode treeNode) {
        ModelItem modelItem = treeNode.getModelItem();
        buildMenu(modelItem, getMenuNameForModelItem(modelItem), treeNode);
    }

    private static String getMenuNameForModelItem(ModelItem modelItem) {
        if (modelItem instanceof WsdlTestSuite) {
            return SoapUI.SUITE;
        } else if (modelItem instanceof WsdlTestStep) {
            return SoapUI.STEP;
        } else if (modelItem instanceof WsdlProject) {
            return SoapUI.PROJECT;
        } else {
            return SoapUI.CASE;
        }
    }
}
