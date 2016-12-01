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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.integration.exporter.ProjectExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;
import java.util.List;

public class ImportPackedProject extends AbstractSoapUIAction<WorkspaceImpl> {
    public ImportPackedProject() {
        super("Import Packed Project", "Import Packed Project");
    }

    @Override
    public void perform(WorkspaceImpl workspace, Object param) {

        try {

            File target = UISupport.getFileDialogs().open(this, "Select file to unpack project", "zip", "zip",
                    System.getProperty("user.home"));
            if (target == null) {
                return;
            }

            String fileName = target.getAbsolutePath();
            if (fileName == null) {
                return;
            }

            File dest = UISupport.getFileDialogs().saveAsDirectory(this, "Select where to unpack it",
                    new File(System.getProperty("user.home")));

            if (dest == null || dest.getAbsoluteFile() == null) {
                return;
            }
            ProjectExporter.unpackageAll(fileName, dest.getAbsolutePath());
            List<String> contents = ProjectExporter.getZipContents(fileName);

            for (String fName : dest.list()) {
                if (contents.contains(fName) && fName.endsWith("-soapui-project.xml")) {
                    new ImportWsdlProjectAction().perform(workspace, new File(dest, fName).getAbsoluteFile());
                    break;
                }
            }

        } catch (Exception e1) {
            UISupport.showErrorMessage("Failed to export project; " + e1);
        }

    }

}
