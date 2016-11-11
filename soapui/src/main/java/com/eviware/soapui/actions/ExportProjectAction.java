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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.integration.exporter.ProjectExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

public class ExportProjectAction extends AbstractSoapUIAction<WsdlProject> {

    public ExportProjectAction() {
        super("Export Project", "Export Project");
    }

    @Override
    public void perform(WsdlProject project, Object param) {
        ProjectExporter exporter = new ProjectExporter(project);

        try {
            String path = project.getPath();
            if (path == null) {
                project.save();
            } else {
                File file = UISupport.getFileDialogs().saveAs(this, "Select file to export project", "zip", "zip",
                        new File(System.getProperty("user.home")));
                if (file == null) {
                    return;
                }

                String fileName = file.getAbsolutePath();
                if (fileName == null) {
                    return;
                }

                exporter.exportProject(fileName);
            }
        } catch (Exception e1) {
            SoapUI.logError(e1, "Failed to export project");
            UISupport.showErrorMessage("Failed to export project; " + e1);
        }

    }

}
