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
import com.eviware.soapui.model.project.Project;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JPopupMenuFixture;
import org.fest.swing.fixture.JTreeFixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class used for generic operations on the workspace level
 */
public final class WorkspaceUtils {
    public static final String NAVIGATION_TREE_PATH_SEPARATOR = "##";
    private static final String NAVIGATOR = "navigator";

    private WorkspaceUtils() {
        throw new AssertionError();
    }

    public static JPanelFixture getNavigatorPanel(FrameFixture frame) {
        return frame.panel(NAVIGATOR);
    }

    public static JPopupMenuFixture rightClickOnWorkspace(FrameFixture frame) {
        return getNavigatorPanel(frame).tree().showPopupMenuAt(SoapUI.getWorkspace().getName());
    }

    public static List<String> getProjectNameList() {
        List<String> projectNameList = new ArrayList<String>();
        for (Project project : SoapUI.getWorkspace().getProjectList()) {
            projectNameList.add(project.getName());
        }
        Collections.sort(projectNameList);
        return projectNameList;
    }

    public static JTreeFixture getNavigationTree(FrameFixture rootWindow) {
        JTreeFixture navigationTree = getNavigatorPanel(rootWindow).tree();
        navigationTree.separator("##");
        return navigationTree;
    }

    public static String getProjectNavigationPath(String projectName) {
        return SoapUI.getWorkspace().getName() + NAVIGATION_TREE_PATH_SEPARATOR + projectName
                + NAVIGATION_TREE_PATH_SEPARATOR;
    }
}