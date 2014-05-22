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
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.InputStream;


public class CreateDataDrivenTestProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    private static final String DATA_SOURCE_TEST_STEP_ID = "f4f876f1-39ab-40f3-8a05-7a6bbef35849";

    public CreateDataDrivenTestProjectAction() {
        super("Create data-driven test", "Creates a new data-driven test");
    }

    @Override
    public void perform(WorkspaceImpl workspace, Object param) {
        InputStream projectPath = getClass().getResourceAsStream("/soapui-projects/trial/Data-driven-soapui-project.xml");
        WsdlProject project = (WsdlProject) workspace.importProject(projectPath);
        project.setName(ModelItemNamer.createName(project.getName(), workspace.getProjectList()));
        selectDataSourceTestStep(project);
    }

    private void selectDataSourceTestStep(Project project) {
        TestStep dataSourceTestStep = ModelSupport.findModelItemById(DATA_SOURCE_TEST_STEP_ID, project);

        UISupport.select(dataSourceTestStep);
        UISupport.showDesktopPanel(dataSourceTestStep);

        showPostCreationInfoMessage();
    }

    private void showPostCreationInfoMessage() {
        String message = String.format("A new TestCase has been created containing the following TestSteps:%n" +
                "- DataSource (pre-filled with data)%n" +
                "- REST request (using the DataSource)%n" +
                "- DataSource Loop%n" +
                "You can now run and/or modify the project.");

        UISupport.showInfoMessage(message, "Data driven project created");
    }
}