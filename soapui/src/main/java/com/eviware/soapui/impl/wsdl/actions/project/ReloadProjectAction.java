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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Prompts to reload the specified WsdlProject
 *
 * @author ole.matzura
 */

public class ReloadProjectAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "ReloadProjectAction";

    public ReloadProjectAction() {
        super("Reload Project", "Reloads this project from file");
    }

    public void perform(WsdlProject project, Object param) {
        if (project.isRemote()) {
            String path = UISupport.prompt("Reload remote project URL", getName(), project.getPath());
            if (path != null) {
                try {
                    project.reload(path);
                } catch (SoapUIException ex) {
                    UISupport.showErrorMessage(ex);
                }
            }
        } else {
            File file = UISupport.getFileDialogs().open(this, "Reload Project", ".xml", "SoapUI Project Files (*.xml)",
                    project.getPath());
            if (file != null) {
                try {
                    project.reload(file.getAbsolutePath());
                } catch (SoapUIException ex) {
                    UISupport.showErrorMessage(ex);
                }
            }
        }
    }
}
