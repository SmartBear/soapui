/*
* Copyright 2004-2014 SmartBear Software
*
* Licensed under the EUPL, Version 1.1 or – as soon as they will be approved by the European Commission – subsequent
* versions of the EUPL (the “Licence”);
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software distributed under the Licence is
* distributed on an “AS IS” basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the Licence for the specific language governing permissions and limitations
* under the Licence.
*/
package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.InputStream;


public class CreateDataDrivenTestAction extends AbstractSoapUIAction<WorkspaceImpl> {
    private static final String DATA_SOURCE_TEST_STEP_ID = "f4f876f1-39ab-40f3-8a05-7a6bbef35849";


    public static final String SOAPUI_ACTION_ID = "CreateDataDrivenTestAction";

    public CreateDataDrivenTestAction() {
        super("Create data-driven test", "Creates a new data-driven test");
    }

    @Override
    public void perform(WorkspaceImpl workspace, Object param) {
        System.out.println(workspace.getName() + " lol");

        InputStream projectPath = getClass().getResourceAsStream("/soapui-projects/trial/Data-driven-soapui-project.xml");
        Project project = workspace.importProject(projectPath);
        selectDataSourceTestStep(project);
    }

    private void selectDataSourceTestStep(Project project) {
        TestStep dataSourceTestStep = ModelSupport.findModelItemById(DATA_SOURCE_TEST_STEP_ID, project);

        UISupport.select(dataSourceTestStep);
        UISupport.showDesktopPanel(dataSourceTestStep);
    }

    /*
    private void unhookProjectFromFile() {
        try {
            Field projectPath = WsdlProject.class.getDeclaredField("path");
            projectPath.set
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    */
}