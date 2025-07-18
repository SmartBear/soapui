/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import javax.swing.*;

import static com.eviware.soapui.analytics.SoapUIActions.SWITCH_THEME;

/**
 * Action to switch dark or light mode
 *
 * @author rrivero
 */

public class SwitchThemeAction extends AbstractSoapUIAction<WorkspaceImpl> implements WorkspaceListener {
    public static final String SOAPUI_ACTION_ID = "SwitchThemeAction";
    
    // Static variable to store pending theme change
    private static Boolean pendingDarkModeState = null;
    private static boolean hasPendingThemeChange = false;

    public SwitchThemeAction() {
        super("Switch theme", "Switches theme");

        Workspace workspace = SoapUI.getWorkspace();
        if (workspace == null) {
            setEnabled(true);
        } else {
            setEnabled(workspace.getProjectCount() > 0);
            workspace.addWorkspaceListener(this);
        }
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        try {
            boolean currentDarkModeState = SoapUI.getSettings().getBoolean("UISettings.DARK_MODE", false);
            boolean newDarkModeState = !currentDarkModeState;

            // Ask for confirmation before making the change
            SwingUtilities.invokeLater(() -> {
                String[] options = { "OK", "Cancel" };
                int result = JOptionPane.showOptionDialog(
                        SoapUI.getFrame(),
                        "Switch to " + (newDarkModeState ? "dark" : "light") + " mode?\n" +
                                "The theme will be applied when you restart the application.",
                        "Switch Theme",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (result == 0) { // "OK" selected
                    try {
                        // Store the pending theme change instead of saving immediately
                        pendingDarkModeState = newDarkModeState;
                        hasPendingThemeChange = true;

                        // Show confirmation message
                        JOptionPane.showMessageDialog(
                                SoapUI.getFrame(),
                                "Theme change scheduled!\n" +
                                        "The " + (newDarkModeState ? "dark" : "light") + " theme will be applied when you restart SoapUI.",
                                "Theme Change Scheduled",
                                JOptionPane.INFORMATION_MESSAGE);

                        Analytics.trackAction(SWITCH_THEME);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                SoapUI.getFrame(),
                                "Error scheduling theme change: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                // If "Cancel" is selected or dialog is closed, do nothing
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to be called when the application is closing to apply pending theme changes
     */
    public static void applyPendingThemeChange() {
        if (hasPendingThemeChange && pendingDarkModeState != null) {
            try {
                SoapUI.getSettings().setBoolean("UISettings.DARK_MODE", pendingDarkModeState);
                SoapUI.saveSettings();
                hasPendingThemeChange = false;
                pendingDarkModeState = null;
            } catch (Exception e) {
                SoapUI.logError(e, "Error applying pending theme change");
            }
        }
    }

    /**
     * Check if there's a pending theme change
     */
    public static boolean hasPendingThemeChange() {
        return hasPendingThemeChange;
    }

    /**
     * Get the pending theme state
     */
    public static Boolean getPendingDarkModeState() {
        return pendingDarkModeState;
    }

    /**
     * Cancel pending theme change
     */
    public static void cancelPendingThemeChange() {
        hasPendingThemeChange = false;
        pendingDarkModeState = null;
    }

    public void projectAdded(Project project) {
        setEnabled(true);
    }

    public void projectChanged(Project project) {
    }

    public void projectRemoved(Project project) {
        setEnabled(project.getWorkspace().getProjectCount() == 0);
    }

    public void workspaceSwitched(Workspace workspace) {
        setEnabled(workspace.getProjectCount() > 0);
    }

    public void workspaceSwitching(Workspace workspace) {
    }

    @Override
    public void projectClosed(Project project) {
    }

    @Override
    public void projectOpened(Project project) {
    }
}
