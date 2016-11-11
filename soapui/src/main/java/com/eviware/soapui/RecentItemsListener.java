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

package com.eviware.soapui;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.impl.actions.SwitchWorkspaceAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.WorkspaceListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Workspace/Deskopt Listener that updates the recent menus..
 *
 * @author ole.matzura
 */

public class RecentItemsListener extends WorkspaceListenerAdapter implements WorkspaceListener, DesktopListener {
    private static final String RECENT_WORKSPACES_SETTING = "RecentWorkspaces";
    private static final String RECENT_PROJECTS_SETTING = "RecentProjects";
    private static final String EMPTYMARKER = "- empty -";
    private static final String CLEAR_ITEMS = "Clear Items";
    private JMenu recentProjectsMenu;
    private JMenu recentWorkspacesMenu;
    private JMenu recentEditorsMenu;
    private boolean switchingWorkspace;

    public RecentItemsListener(JMenu recentWorkspacesMenu2, JMenu recentProjectsMenu2, JMenu recentEditorsMenu2) {
        recentWorkspacesMenu = recentWorkspacesMenu2;
        recentProjectsMenu = recentProjectsMenu2;
        recentEditorsMenu = recentEditorsMenu2;
        recentEditorsMenu.add(EMPTYMARKER).setEnabled(false);
        recentEditorsMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                int editorCount = 0;
                for (int c = 0; c < recentEditorsMenu.getItemCount(); c++) {
                    ShowEditorAction action = getShowEditorAction(recentEditorsMenu, c);
                    if (action == null) {
                        continue;
                    }

                    editorCount++;

                    if (action.isReleased()) {
                        recentEditorsMenu.remove(c);
                        c--;
                    } else {
                        try {
                            action.update();
                        } catch (Throwable e1) {
                            recentEditorsMenu.remove(c);
                            c--;
                        }
                    }
                }

                // Look for a Clear Items JMenuItem, and create it if not existing
                JMenuItem clearAllItem = null;
                for (int i = 0; i < recentEditorsMenu.getItemCount(); ++i) {
                    JMenuItem editorItem = recentEditorsMenu.getItem(i);
                    if (editorItem == null) {
                        continue;
                    }

                    if (editorItem.getText().equals(CLEAR_ITEMS)) {
                        clearAllItem = editorItem;
                    }
                }
                if (clearAllItem == null) {
                    clearAllItem = new JMenuItem(new ClearEditorsAction());
                    recentEditorsMenu.addSeparator();
                    recentEditorsMenu.add(clearAllItem);
                }

                clearAllItem.setEnabled(editorCount > 0);

                // Create Empty Marker if needed.
                if (editorCount == 0 && recentEditorsMenu.getItemCount() <= 2) {
                    recentEditorsMenu.add(new JMenuItem(EMPTYMARKER), 0).setEnabled(false);
                }

            }
        });

        updateRecentWorkspacesMenu();
        updateRecentProjectsMenu();
    }

    @SuppressWarnings("unchecked")
    private void updateRecentWorkspacesMenu() {
        String recent = SoapUI.getSettings().getString(RECENT_WORKSPACES_SETTING, null);
        StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml(recent);

        recentWorkspacesMenu.removeAll();

        if (history.size() > 0) {
            for (Map.Entry<String, String> entry : history.entrySet()) {
                String filePath = entry.getKey();
                DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
                        SwitchWorkspaceAction.SOAPUI_ACTION_ID, null, null, false, filePath);
                String wsName = entry.getValue();

                if (SoapUI.getWorkspace().getPath().equals(filePath)) {
                    continue;
                }

                mapping.setName(wsName);
                mapping.setDescription("Switches to the [" + wsName + "] workspace");

                AbstractAction delegate = new SwingActionDelegate(mapping, SoapUI.getWorkspace());
                recentWorkspacesMenu.add(new JMenuItem(delegate));
            }
        } else {
            recentWorkspacesMenu.add(EMPTYMARKER).setEnabled(false);
        }

        recentWorkspacesMenu.addSeparator();
        recentWorkspacesMenu.add(new ClearWorkspacesAction()).setEnabled(history.size() > 0);
    }

    @SuppressWarnings("unchecked")
    private void updateRecentProjectsMenu() {
        recentProjectsMenu.removeAll();

        String recent = SoapUI.getSettings().getString(RECENT_PROJECTS_SETTING, null);
        StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml(recent);

        if (history.size() > 0) {
            for (Map.Entry<String, String> entry : history.entrySet()) {
                String filePath = entry.getKey();
                DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
                        ImportWsdlProjectAction.SOAPUI_ACTION_ID, null, null, false, filePath);
                String wsName = entry.getValue();
                mapping.setName(wsName);
                mapping.setDescription("Switches to the [" + wsName + "] project");

                AbstractAction delegate = new SwingActionDelegate(mapping, SoapUI.getWorkspace());
                recentProjectsMenu.add(new JMenuItem(delegate));
            }
        } else {
            recentProjectsMenu.add(EMPTYMARKER).setEnabled(false);
        }

        recentProjectsMenu.addSeparator();
        recentProjectsMenu.add(new ClearProjectsAction()).setEnabled(history.size() > 0);
    }

    @SuppressWarnings("unchecked")
    public void projectAdded(Project project) {
        if (switchingWorkspace) {
            return;
        }

        String filePath = ((WsdlProject) project).getPath();
        if (filePath == null) {
            return;
        }

        String recent = SoapUI.getSettings().getString(RECENT_PROJECTS_SETTING, null);
        if (recent != null) {
            StringToStringMap history = StringToStringMap.fromXml(recent);
            history.remove(filePath);
            SoapUI.getSettings().setString(RECENT_PROJECTS_SETTING, history.toXml());
        }

        for (int c = 0; c < recentProjectsMenu.getItemCount() - 2; c++) {
            JMenuItem item = recentProjectsMenu.getItem(c);
            if (item == null) {
                continue;
            }

            Action action = item.getAction();
            if (!(action instanceof SwingActionDelegate)) {
                continue;
            }

            SwingActionDelegate actionDelegate = (SwingActionDelegate) action;
            if (actionDelegate == null) {
                continue;
            }

            SoapUIActionMapping mapping = actionDelegate.getMapping();
            if (filePath.equals(mapping.getParam())) {
                recentProjectsMenu.remove(c);
                break;
            }
        }

        if (recentProjectsMenu.getItemCount() == 2) {
            recentProjectsMenu.add(new JMenuItem(EMPTYMARKER), 0).setEnabled(false);
            recentProjectsMenu.getItem(recentProjectsMenu.getItemCount() - 1).setEnabled(false);
        }
    }

    public void projectChanged(Project project) {
    }

    @SuppressWarnings("unchecked")
    public void projectRemoved(Project project) {
        if (switchingWorkspace) {
            return;
        }

        String filePath = ((WsdlProject) project).getPath();

        String recent = SoapUI.getSettings().getString(RECENT_PROJECTS_SETTING, null);
        StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml(recent);
        history.put(filePath, project.getName());
        SoapUI.getSettings().setString(RECENT_PROJECTS_SETTING, history.toXml());

        DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
                ImportWsdlProjectAction.SOAPUI_ACTION_ID, null, null, false, filePath);
        mapping.setName(project.getName());
        mapping.setDescription("Switches to the [" + project.getName() + "] project");

        AbstractAction delegate = new SwingActionDelegate(mapping, SoapUI.getWorkspace());

        recentProjectsMenu.add(new JMenuItem(delegate), recentProjectsMenu.getItemCount() - 2);

        recentProjectsMenu.getItem(recentProjectsMenu.getItemCount() - 1).setEnabled(true);

        if (isEmptyMarker(recentProjectsMenu.getItem(0))) {
            recentProjectsMenu.remove(0);
        }

        removeProjectEditors(project);

    }

    private void removeProjectEditors(Project project) {
        for (int c = 0; c < recentEditorsMenu.getItemCount(); c++) {
            ShowEditorAction action = getShowEditorAction(recentEditorsMenu, c);
            if (action == null) {
                continue;
            }

            if (action.isReleased()) {
                recentEditorsMenu.remove(c);
                c--;
            } else {
                try {
                    action.update();
                    if (dependsOnProject(action.getModelItem(), project)) {
                        recentEditorsMenu.remove(c);
                        c--;
                    }
                } catch (Throwable e1) {
                    recentEditorsMenu.remove(c);
                    c--;
                }
            }
        }
    }

    private boolean dependsOnProject(ModelItem modelItem, Project project) {
        if (modelItem instanceof Interface) {
            return ((Interface) modelItem).getProject() == project;
        } else if (modelItem instanceof Operation) {
            return ((Operation) modelItem).getInterface().getProject() == project;
        } else if (modelItem instanceof Request) {
            return ((Request) modelItem).getOperation().getInterface().getProject() == project;
        } else if (modelItem instanceof TestSuite) {
            return ((TestSuite) modelItem).getProject() == project;
        } else if (modelItem instanceof TestCase) {
            return ((TestCase) modelItem).getTestSuite().getProject() == project;
        } else if (modelItem instanceof TestStep) {
            return ((TestStep) modelItem).getTestCase().getTestSuite().getProject() == project;
        } else if (modelItem instanceof LoadTest) {
            return ((LoadTest) modelItem).getTestCase().getTestSuite().getProject() == project;
        } else if (modelItem instanceof MockService) {
            return ((MockService) modelItem).getProject() == project;
        } else if (modelItem instanceof MockOperation) {
            return ((MockOperation) modelItem).getMockService().getProject() == project;
        } else if (modelItem instanceof MockResponse) {
            return ((MockResponse) modelItem).getMockOperation().getMockService().getProject() == project;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public void workspaceSwitched(Workspace workspace) {
        switchingWorkspace = false;

        String filePath = workspace.getPath();

        String recent = SoapUI.getSettings().getString(RECENT_WORKSPACES_SETTING, null);
        if (recent != null) {
            StringToStringMap history = StringToStringMap.fromXml(recent);
            history.remove(filePath);
            SoapUI.getSettings().setString(RECENT_WORKSPACES_SETTING, history.toXml());
        }

        for (int c = 0; c < recentWorkspacesMenu.getItemCount(); c++) {
            JMenuItem item = recentWorkspacesMenu.getItem(c);
            if (item == null) {
                continue;
            }

            Action action = item.getAction();
            if (!(action instanceof SwingActionDelegate)) {
                continue;
            }

            SwingActionDelegate actionDelegate = (SwingActionDelegate) action;
            if (actionDelegate == null) {
                continue;
            }

            SoapUIActionMapping mapping = actionDelegate.getMapping();
            if (filePath.equals(mapping.getParam())) {
                recentWorkspacesMenu.remove(c);
                break;
            }
        }

        if (recentWorkspacesMenu.getItemCount() == 2) {
            recentWorkspacesMenu.add(new JMenuItem(EMPTYMARKER), 0).setEnabled(false);
        }
    }

    @SuppressWarnings("unchecked")
    public void workspaceSwitching(Workspace workspace) {
        switchingWorkspace = true;
        recentEditorsMenu.removeAll();
        if (recentEditorsMenu.getItemCount() == 0) {
            recentEditorsMenu.add(EMPTYMARKER).setEnabled(false);
        }

        String filePath = workspace.getPath();
        DefaultActionMapping<WorkspaceImpl> mapping = new DefaultActionMapping<WorkspaceImpl>(
                SwitchWorkspaceAction.SOAPUI_ACTION_ID, null, null, false, filePath);
        mapping.setName(workspace.getName());
        mapping.setDescription("Switches to the [" + workspace.getName() + "] workspace");

        AbstractAction delegate = new SwingActionDelegate(mapping, SoapUI.getWorkspace());
        recentWorkspacesMenu.add(new JMenuItem(delegate), recentWorkspacesMenu.getItemCount() - 2);

        recentWorkspacesMenu.getItem(recentWorkspacesMenu.getItemCount() - 1).setEnabled(true);
        System.out.println(recentWorkspacesMenu.getItem(recentWorkspacesMenu.getItemCount() - 1).getText());

        String recent = SoapUI.getSettings().getString(RECENT_WORKSPACES_SETTING, null);
        StringToStringMap history = recent == null ? new StringToStringMap() : StringToStringMap.fromXml(recent);
        history.put(filePath, workspace.getName());
        SoapUI.getSettings().setString(RECENT_WORKSPACES_SETTING, history.toXml());

        if (isEmptyMarker(recentWorkspacesMenu.getItem(0))) {
            recentWorkspacesMenu.remove(0);
        }

        recentEditorsMenu.removeAll();
    }

    public void desktopPanelClosed(DesktopPanel desktopPanel) {
        ModelItem modelItem = desktopPanel.getModelItem();
        if (modelItem == null || recentEditorsMenu.getItemCount() == 0) {
            return;
        }

        if (isEmptyMarker(recentEditorsMenu.getItem(0))) {
            recentEditorsMenu.remove(0);
        }

        recentEditorsMenu.add(new JMenuItem(new ShowEditorAction(modelItem)), 0);
    }

    public void desktopPanelCreated(DesktopPanel desktopPanel) {
        for (int c = 0; c < recentEditorsMenu.getItemCount(); c++) {
            ShowEditorAction action = getShowEditorAction(recentEditorsMenu, c);
            if (action == null) {
                continue;
            }

            if (action.isReleased()) {
                recentEditorsMenu.remove(c);
                c--;
            } else if (action.getModelItem().equals(desktopPanel.getModelItem())) {
                recentEditorsMenu.remove(c);
                break;
            }
        }

        if (recentEditorsMenu.getItemCount() == 2) {
            recentEditorsMenu.add(new JMenuItem(EMPTYMARKER), 0).setEnabled(false);
        }
    }

    public void desktopPanelSelected(DesktopPanel desktopPanel) {
    }

    private static class ShowEditorAction extends AbstractAction {
        private Reference<ModelItem> ref;

        public ShowEditorAction(ModelItem modelItem) {
            super(modelItem.getName());

            putValue(Action.SHORT_DESCRIPTION, "Reopen editor for [" + modelItem.getName() + "]");
            ref = new WeakReference<ModelItem>(modelItem);
        }

        public ModelItem getModelItem() {
            return ref.get();
        }

        public void update() {
            ModelItem modelItem = ref.get();
            if (modelItem == null) {
                return;
            }

            putValue(Action.NAME, modelItem.getName());
            putValue(Action.SHORT_DESCRIPTION, "Reopen editor for [" + modelItem.getName() + "]");
        }

        public boolean isReleased() {
            return ref.get() == null;
        }

        public void actionPerformed(ActionEvent e) {
            ModelItem modelItem = ref.get();
            if (modelItem != null) {
                UISupport.showDesktopPanel(modelItem);
            } else {
                UISupport.showErrorMessage("Item [" + getValue(Action.NAME) + "] is no longer available");
            }
        }
    }

	/* Helper methods */

    /**
     * Confirms that the item of index i in menu is a ShowEditorAction, and
     * extracts the ShowEditorAction.
     *
     * @param menu The menu to get the JMenuItem, containing the action from.
     * @param i    The index where the JMenuItem is located in menu.
     * @return The ShowEditorAction if confirmed. Otherwise null.
     */
    private ShowEditorAction getShowEditorAction(JMenu menu, int i) {
        JMenuItem menuItem = recentEditorsMenu.getItem(i);
        if (menuItem == null) {
            return null;
        }

        Action unknownAction = menuItem.getAction();

        if (unknownAction == null || !(unknownAction instanceof ShowEditorAction)) {
            return null;
        }

        return (ShowEditorAction) unknownAction;
    }

    /**
     * Checks whether a JMenuItem is an Empty marker (marking an empty list).
     *
     * @param item The item to check.
     * @return True if item is an Empty marker. False otherwise (including if
     *         item is null).
     */
    private boolean isEmptyMarker(JMenuItem item) {
        if (item == null) {
            return false;
        }
        if (item.getText().equals(EMPTYMARKER)) {
            return true;
        }
        return false;
    }

	/*
     * Action classes for clearing all items in the Recent
	 * Editors/Projects/Workspaces submenu
	 */

    @SuppressWarnings("serial")
    private class ClearProjectsAction extends AbstractAction {
        public ClearProjectsAction() {
            super(CLEAR_ITEMS);
            putValue(Action.SHORT_DESCRIPTION, "Clear all recent projects");
        }

        public void actionPerformed(ActionEvent e) {
            if (!UISupport.confirm("Remove all Projects from this menu?", "Question")) {
                return;
            }

            StringToStringMap emptyMap = new StringToStringMap();

            SoapUI.getSettings().setString(RECENT_PROJECTS_SETTING, emptyMap.toXml());
            updateRecentProjectsMenu();

        }
    }

    @SuppressWarnings("serial")
    private class ClearWorkspacesAction extends AbstractAction {
        public ClearWorkspacesAction() {
            super(CLEAR_ITEMS);
            putValue(Action.SHORT_DESCRIPTION, "Clear all recent workspaces");
        }

        public void actionPerformed(ActionEvent e) {
            if (!UISupport.confirm("Remove all Workspaces from this menu?", "Question")) {
                return;
            }

            StringToStringMap emptyMap = new StringToStringMap();

            SoapUI.getSettings().setString(RECENT_WORKSPACES_SETTING, emptyMap.toXml());
            updateRecentWorkspacesMenu();

        }
    }

    @SuppressWarnings("serial")
    private class ClearEditorsAction extends AbstractAction {
        public ClearEditorsAction() {
            super(CLEAR_ITEMS);
            putValue(Action.SHORT_DESCRIPTION, "Clear all recent Editors");
        }

        public void actionPerformed(ActionEvent e) {
            if (!UISupport.confirm("Remove all Editors from this menu?", "Question")) {
                return;
            }

            recentEditorsMenu.removeAll();
            recentEditorsMenu.add(EMPTYMARKER).setEnabled(false);
            recentEditorsMenu.addSeparator();
            recentEditorsMenu.add(new ClearEditorsAction());
        }
    }

}
