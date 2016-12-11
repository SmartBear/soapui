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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.IOException;

/**
 * Removes a WsdlProject from the workspace
 *
 * @author Ole.Matzura
 */

public class RemoveProjectAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "RemoveProjectAction";

    public RemoveProjectAction() {
        super("Remove", "Removes this project from the workspace");
    }

    public void perform(WsdlProject project, Object param) {
        if (hasRunningTests(project)) {
            UISupport.showErrorMessage("Cannot remove Interface due to running tests");
            return;
        }

        Boolean saveProject = Boolean.FALSE;

        if (project.isOpen()) {
            saveProject = UISupport.confirmOrCancel("Save project [" + project.getName() + "] before removing?",
                    "Remove Project");
            if (saveProject == null) {
                return;
            }
        } else {
            if (!UISupport.confirm("Remove project [" + project.getName() + "] from workspace", "Remove Project")) {
                return;
            }
        }

        if (saveProject) {
            try {
                SaveStatus status = project.save();
                if (status == SaveStatus.CANCELLED || status == SaveStatus.FAILED) {
                    return;
                }
            } catch (IOException e1) {
                UISupport.showErrorMessage(e1);
            }
        }
        project.getWorkspace().removeProject(project);

    }

    private boolean hasRunningTests(WsdlProject project) {
        for (int c = 0; c < project.getTestSuiteCount(); c++) {
            TestSuite testSuite = project.getTestSuiteAt(c);
            for (int i = 0; i < testSuite.getTestCaseCount(); i++) {
                if (SoapUI.getTestMonitor().hasRunningTest(testSuite.getTestCaseAt(i))) {
                    return true;
                }
            }
        }

        for (MockService mockService : project.getMockServiceList()) {
            if (SoapUI.getTestMonitor().hasRunningMock(mockService)) {
                return true;
            }
        }

        return false;
    }
}
