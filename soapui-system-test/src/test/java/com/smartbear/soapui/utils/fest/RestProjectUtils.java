/*
 * Copyright 2004-2014 SmartBear Software
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
package com.smartbear.soapui.utils.fest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel;
import com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel;
import com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.MovePropertyDownAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.MovePropertyUpAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.RemovePropertyAction;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.driver.JTableComboBoxEditorCellWriter;
import org.fest.swing.fixture.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REST_PARAMS_TABLE;
import static com.eviware.soapui.impl.rest.panels.resource.RestParamsTable.REVERT_PARAMETER_VALUES;
import static com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction.ADD_PARAM_ACTION_NAME;
import static org.fest.swing.data.TableCell.row;

/**
 * Utility class used for generic operations on a REST project
 */
public final class RestProjectUtils {
    private static final int NEW_PROJECT_TIMEOUT = 2000;
    private static final String URI = "http://soapui.org";
    private static final String NEW_REST_PROJECT_MENU_ITEM = "New REST Project";
    private static final String NEW_REST_PROJECT_DIALOG_NAME = "New REST Project";
    private static final String OK_BUTTON_NAME = "OK";

    private static final int REST_RESOURCE_POSITION_IN_TREE = 2;
    private static final int REST_REQUEST_POSITION_IN_TREE = 4;
    private static final int REST_METHOD_POSITION_IN_TREE = 3;


    private RestProjectUtils() {
        throw new AssertionError();
    }

    public static void createNewRestProject(FrameFixture rootWindow, Robot robot) {
        openCreateNewRestProjectDialog(rootWindow);
        enterURIandClickOk(robot);
    }

    public static void createNewRestProjectWithUri(FrameFixture rootWindow, Robot robot, String uri) {
        openCreateNewRestProjectDialog(rootWindow);
        enterURIandClickOk(robot, uri);
    }

    public static JPanelFixture findRequestEditor(FrameFixture rootWindow, int projectIndexInTree, Robot robot) {
        closeAlreadyOpenedDesktopEditors(rootWindow, RestRequestDesktopPanel.REST_REQUEST_EDITOR);
        openPanelByClickingOnTheNavigationElement(projectIndexInTree, rootWindow, REST_REQUEST_POSITION_IN_TREE, robot);
        return locateRequestEditor(rootWindow);
    }

    public static void closeAlreadyOpenedDesktopEditors(FrameFixture rootWindow, String panelName) {
        JPanelFixture panel = null;
        try {
            panel = rootWindow.panel(panelName);

        } catch (Exception ex) {
            //There is no open editors
        }

        if (panel == null) {
            return;
        }

        JInternalFrame fixture = (JInternalFrame) panel.target.getParent().getParent().getParent();
        fixture.doDefaultCloseAction();
    }

    public static JPanelFixture locateRequestEditor(FrameFixture rootWindow) {
        return rootWindow.panel(RestRequestDesktopPanel.REST_REQUEST_EDITOR);
    }

    public static JTreeRowFixture findRestRequestPopupMenu(FrameFixture rootWindow, int projectIndexInTree) {
        return (JTreeRowFixture) findTreeNode(projectIndexInTree, rootWindow, REST_REQUEST_POSITION_IN_TREE).rightClick();
    }

    public static JPanelFixture findMethodEditor(FrameFixture rootWindow, int projectIndexInTree, Robot robot) {
        closeAlreadyOpenedDesktopEditors(rootWindow, RestMethodDesktopPanel.REST_METHOD_EDITOR);
        openPanelByClickingOnTheNavigationElement(projectIndexInTree, rootWindow, REST_METHOD_POSITION_IN_TREE, robot);
        return locateMethodEditor(rootWindow);
    }

    public static JPanelFixture locateMethodEditor(FrameFixture rootWindow) {
        return rootWindow.panel(RestMethodDesktopPanel.REST_METHOD_EDITOR);
    }


    public static JPanelFixture findResourceEditor(FrameFixture rootWindow, int projectIndexInTree, Robot robot) {
        closeAlreadyOpenedDesktopEditors(rootWindow, RestResourceDesktopPanel.REST_RESOURCE_EDITOR);
        openPanelByClickingOnTheNavigationElement(projectIndexInTree, rootWindow, REST_RESOURCE_POSITION_IN_TREE, robot);
        return locateResourceEditor(rootWindow);
    }

    public static JPanelFixture locateResourceEditor(FrameFixture rootWindow) {
        return rootWindow.panel(RestResourceDesktopPanel.REST_RESOURCE_EDITOR);
    }

    public static void addNewParameter(JPanelFixture parentPanel, Robot robot, String paramName, String paramValue) {
        parentPanel.button(ADD_PARAM_ACTION_NAME).click();
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);

        robot.waitForIdle();
        int rowNumToEdit = restParamsTable.target.getRowCount() - 1;
        editTableCell(paramName, restParamsTable, robot, rowNumToEdit, 0);
        editTableCell(paramValue, restParamsTable, robot, rowNumToEdit, 1);
    }

    public static void deleteParameter(JPanelFixture parentPanel, int rowNum) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        restParamsTable.cell(row(rowNum).column(0)).click();
        parentPanel.button(RemovePropertyAction.DELETE_PARAM_ACTON_NAME).click();
    }

    public static void deleteParameter(JPanelFixture parentPanel, String paramName) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowNum = 0; rowNum < restParamsTable.rowCount(); rowNum++) {
            JTableCellFixture cell = restParamsTable.cell(row(rowNum).column(0));
            if (cell.value().equals(paramName)) {
                cell.click();
                break;
            }
        }
        parentPanel.button(RemovePropertyAction.DELETE_PARAM_ACTON_NAME).click();
    }

    public static void moveUpParameter(JPanelFixture parentPanel, String paramName) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowNum = 0; rowNum < restParamsTable.rowCount(); rowNum++) {
            JTableCellFixture cell = restParamsTable.cell(row(rowNum).column(0));
            if (cell.value().equals(paramName)) {
                cell.click();
                break;
            }
        }
        parentPanel.button(MovePropertyUpAction.MOVE_PROPERTY_UP_ACTION_NAME).click();
    }

    public static void moveDownParameter(JPanelFixture parentPanel, String paramName) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowNum = 0; rowNum < restParamsTable.rowCount(); rowNum++) {
            JTableCellFixture cell = restParamsTable.cell(row(rowNum).column(0));
            if (cell.value().equals(paramName)) {
                cell.click();
                break;
            }
        }
        parentPanel.button(MovePropertyDownAction.MOVE_PROPERTY_DOWN_ACTION_NAME).click();
    }

    public static void revertParameterValue(JPanelFixture parentPanel) {
        parentPanel.button(REVERT_PARAMETER_VALUES).click();

    }

    public static void changeParameterLevel(JPanelFixture parentPanel, String parameterName, String newLevel,
                                            Robot robot) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowCounter = 0; rowCounter < restParamsTable.rowCount(); rowCounter++) {
            String paramNameAtIndex = restParamsTable.cell(row(rowCounter).column(0)).value();
            if (paramNameAtIndex.equals(parameterName)) {
//				JTableCellFixture cell = restParamsTable.cell( row( rowCounter ).column( 3 ) );
//				cell.enterValue( newLevel );
                JTableComboBoxEditorCellWriter cellWriter = new JTableComboBoxEditorCellWriter(robot);
                cellWriter.enterValue(restParamsTable.target, rowCounter, 3, newLevel);
                return;
            }
        }
    }

    public static void changeParameterStyle(JPanelFixture parentPanel, String parameterName, String newStyle,
                                            Robot robot) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowCounter = 0; rowCounter < restParamsTable.rowCount(); rowCounter++) {
            String paramNameAtIndex = restParamsTable.cell(row(rowCounter).column(0)).value();
            if (paramNameAtIndex.equals(parameterName)) {
                JTableComboBoxEditorCellWriter cellWriter = new JTableComboBoxEditorCellWriter(robot);
                cellWriter.enterValue(restParamsTable.target, rowCounter, 2, newStyle);
                return;
            }
        }
    }

    public static void changeParameterValue(JPanelFixture parentPanel, String parameterValue, String newValue,
                                            Robot robot) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowCounter = 0; rowCounter < restParamsTable.rowCount(); rowCounter++) {
            JTableCellFixture cell = restParamsTable.cell(row(rowCounter).column(1));
            String paramNameAtIndex = cell.value();
            if (paramNameAtIndex.equals(parameterValue)) {
                cell.doubleClick();
                editTableCell(newValue, restParamsTable, robot, rowCounter, 1);
                return;
            }
        }
    }

    public static void changeParameterName(JPanelFixture parentPanel, String parameterName, String newName,
                                           Robot robot) {
        JTableFixture restParamsTable = parentPanel.table(REST_PARAMS_TABLE);
        for (int rowCounter = 0; rowCounter < restParamsTable.rowCount(); rowCounter++) {
            JTableCellFixture cell = restParamsTable.cell(row(rowCounter).column(0));
            String paramNameAtIndex = cell.value();
            if (paramNameAtIndex.equals(parameterName)) {
                cell.doubleClick();
                editTableCell(newName, restParamsTable, robot, rowCounter, 0);
                return;
            }
        }
    }

    private static void enterURIandClickOk(Robot robot) {
        enterURIandClickOk(robot, URI);
    }

    public static void enterURIandClickOk(Robot robot, String uri) {
        DialogFixture newProjectDialog = FestMatchers.dialogWithTitle(NEW_REST_PROJECT_DIALOG_NAME)
                .withTimeout(NEW_PROJECT_TIMEOUT)
                .using(robot);

        newProjectDialog.textBox().focus();
        newProjectDialog.textBox().click();
        newProjectDialog.textBox().setText(uri);

        JButtonFixture buttonOK = newProjectDialog.button(FestMatchers.buttonWithText(OK_BUTTON_NAME));
        buttonOK.click();
    }

    private static void openCreateNewRestProjectDialog(FrameFixture rootWindow) {
        JPopupMenuFixture workspace = WorkspaceUtils.rightClickOnWorkspace(rootWindow);
        workspace.menuItem(FestMatchers.menuItemWithText(NEW_REST_PROJECT_MENU_ITEM)).click();
    }

    private static void openPanelByClickingOnTheNavigationElement(int newProjectIndexInTree, FrameFixture frame,
                                                                  int panelPositionInNavigationTree, Robot robot) {
        findTreeNode(newProjectIndexInTree, frame, panelPositionInNavigationTree).click();
        robot.pressAndReleaseKeys(KeyEvent.VK_ENTER);
    }

    private static void rightClickOnTreeNode(int newProjectIndexInTree, FrameFixture frame, int panelPositionInNavigationTree) {
        findTreeNode(newProjectIndexInTree, frame, panelPositionInNavigationTree).rightClick();
    }

    private static JTreeNodeFixture findTreeNode(int newProjectIndexInTree, FrameFixture frame, int panelPositionInNavigationTree) {
        return WorkspaceUtils.getNavigatorPanel(frame).tree().node(newProjectIndexInTree + panelPositionInNavigationTree);
    }

    private static void editTableCell(String paramValue, JTableFixture table, Robot robot, int row, int column) {
        robot.waitForIdle();
        JTextField tableCellEditor = (JTextField) table.cell(row(row).column(column)).editor();
        new JTextComponentFixture(robot, tableCellEditor)
                .setText(paramValue)
                .pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));
    }

    public static void addCustomProperty(int newProjectIndexInNavigationTree, FrameFixture rootWindow,
                                         String propertyName, String propertyValue, Robot robot) {
        findTreeNode(newProjectIndexInNavigationTree, rootWindow, 0).click();
        JTabbedPaneFixture propertiesPaneFixture = new JTabbedPaneFixture(robot, SoapUI.PROPERTIES_TAB_PANEL_NAME);
        propertiesPaneFixture.selectTab("Custom Properties");

        JPanel tablePanel = (JPanel) propertiesPaneFixture.target.getComponent(1);

        JPanelFixture panelFixture = new JPanelFixture(robot, tablePanel);
        panelFixture.button(ADD_PARAM_ACTION_NAME).click();

        JTableFixture table = panelFixture.table(PropertyHolderTable.PROPERTIES_HOLDER_TABLE_NAME);

        robot.waitForIdle();
        int rowNumToEdit = table.target.getRowCount() - 1;
        editTableCell(propertyName, table, robot, rowNumToEdit, 0);
        editTableCell(propertyValue, table, robot, rowNumToEdit, 1);

    }

    public static void createNewRESTMethod(Robot robot, String methodName) {
        DialogFixture newMethodDialog = FestMatchers.dialogWithTitle("New REST Method").using(robot);
        newMethodDialog.textBox().setText(methodName);
        newMethodDialog.button(FestMatchers.buttonWithText("OK")).click();
    }

    public static void addNewRESTMethodAtPath(Robot robot, FrameFixture rootWindow, String methodName, String path) {
        JTreeFixture navigationTree = WorkspaceUtils.getNavigationTree(rootWindow);
        JPopupMenuFixture popupMenuFixture = navigationTree.showPopupMenuAt(path);
        popupMenuFixture.menuItem(FestMatchers.menuItemWithText("New Method")).click();

        createNewRESTMethod(robot, methodName);
    }
}