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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlProject
 *
 * @author Ole.Matzura
 */

public class RenameProjectAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "RenameProjectAction";

    public RenameProjectAction() {
        super("Rename", "Renames this project");
    }

    public void perform(WsdlProject project, Object param) {
        String name = UISupport.prompt("Specify name of project", "Rename Project", project.getName());
        if (name == null || name.equals(project.getName())) {
            return;
        }
        while (project.getWorkspace().getProjectByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of Project", "Rename Project", project.getName());
            if (name == null || name.equals(project.getName())) {
                return;
            }
        }

        project.setName(name);
    }
}
