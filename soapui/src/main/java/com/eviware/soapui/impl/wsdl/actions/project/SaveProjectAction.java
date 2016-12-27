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
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.IOException;

/**
 * Saves a WsdlProject
 *
 * @author Ole.Matzura
 */

public class SaveProjectAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "SaveProjectAction";

    public SaveProjectAction() {
        super("Save Project", "Saves this project");
    }

    public void perform(WsdlProject project, Object param) {
        try {
            if (StringUtils.hasContent(project.getPath()) || project.getWorkspace() == null) {
                project.save();
            } else {
                project.save(project.getWorkspace().getProjectRoot());
            }
        } catch (IOException e1) {
            UISupport.showErrorMessage("Failed to save project; " + e1);
        }
    }
}
