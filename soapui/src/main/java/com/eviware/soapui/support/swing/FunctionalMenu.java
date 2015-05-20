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
import javax.swing.*;
import java.awt.*;


public class FunctionalMenu {
    public static JMenu getMenu(String name){
        for (int i = 0; i<SoapUI.getMenuBar().getMenuCount(); i++){
            if (SoapUI.getMenuBar().getMenu(i).getText().equals(name)){
                return SoapUI.getMenuBar().getMenu(i);
            }
        }
        return null;
    }

    public static JMenu buildMenuForWorkspace(JMenu menu, String actionGroup) {
        ActionList actions = buildActionsForActionGroup(actionGroup);
        if(menu.getText().equals(SoapUI.STEP)){
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
            disableEnableMenuSubItems(component, false);
            component.setEnabled(false);
        }
        return menu;
    }

    protected static ActionList buildActionsForActionGroup(String actionGroup) {
        return ActionListBuilder.buildActions(actionGroup, null);
    }

    private static void disableMenuItems(String groupId) {
            JMenu menu = getMenu(groupId);
            for (Component component : menu.getMenuComponents()) {
                disableEnableMenuSubItems(component, false);
                component.setEnabled(false);
            }
    }

    private static void enableMenuItems(String groupId) {
        JMenu menu = getMenu(groupId);
        for (Component component : menu.getMenuComponents()) {
            disableEnableMenuSubItems(component, true);
            component.setEnabled(true);
        }
    }

    private static void disableEnableMenuSubItems(Component component, boolean bEnable) {
        if (component instanceof JMenu) {
            for (Component curComponent : ((JMenu) component).getMenuComponents()) {
                curComponent.setEnabled(bEnable);
            }
        }
    }

    public static void buildMenu(ModelItem curModelItem, String menuName, SoapUITreeNode path) {
        String[] groupsId = {SoapUI.STEP, SoapUI.CASE, SoapUI.SUITE, SoapUI.PROJECT};
        if(curModelItem instanceof Workspace){
            for (int i = 0; i<groupsId.length; i++) {
                disableMenuItems(groupsId[i]);
            }
        }else if (ModelSupport.isOneOf(curModelItem, WsdlTestSuite.class, WsdlTestCase.class,
                WsdlTestStep.class, WsdlProject.class)) {
            ActionList actionList = ActionListBuilder.buildActions(curModelItem);
            JMenu curMenu = FunctionalMenu.getMenu(menuName);
            curMenu.removeAll();
            ActionSupport.addActions(actionList, curMenu);
            for (int i = 0; !groupsId[i].equals(menuName); i++) {
                disableMenuItems(groupsId[i]);
            }
            while (!(curModelItem.getParent() instanceof Workspace)) {
                curModelItem = curModelItem.getParent();
                ActionList parentActionList = ActionListBuilder.buildActions(curModelItem);
                JMenu parentMenu = FunctionalMenu.getMenu(selectMenuName(curModelItem));
                parentMenu.removeAll();
                ActionSupport.addActions(parentActionList, parentMenu);
                enableMenuItems(selectMenuName(curModelItem));
            }
        } else {
            SoapUITreeNode node = path.getParentTreeNode();
            curModelItem = node.getModelItem();
            while (!(curModelItem instanceof Workspace)) {
                if (curModelItem instanceof WsdlTestCase) {
                    buildMenu(curModelItem, SoapUI.CASE, null);
                    break;
                }
                if (curModelItem instanceof WsdlProject) {
                    buildMenu(curModelItem, SoapUI.PROJECT, null);
                    break;
                }
                node = node.getParentTreeNode();
                curModelItem = node.getModelItem();
            }
        }
    }

    public static String selectMenuName(ModelItem modelItem){
        if (modelItem instanceof WsdlTestSuite){
            return SoapUI.SUITE;
        }else if (modelItem instanceof WsdlTestStep){
            return SoapUI.STEP;
        }else if (modelItem instanceof WsdlProject) {
            return SoapUI.PROJECT;
        }else {
            return SoapUI.CASE;
        }
    }
}
