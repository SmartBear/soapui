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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clones an Interface to another project
 *
 * @author Ole.Matzura
 */

public class CloneInterfaceAction extends AbstractSoapUIAction<WsdlInterface> {
    public CloneInterfaceAction() {
        super("Clone Interface", "Clones this Interface to another project");
    }

    public void perform(WsdlInterface iface, Object param) {
        WorkspaceImpl workspace = iface.getProject().getWorkspace();
        String[] names = ModelSupport.getNames(workspace.getOpenProjectList(), new String[]{"<Create New>"});

        List<String> asList = new ArrayList<String>(Arrays.asList(names));
        asList.remove(iface.getProject().getName());

        String targetProjectName = UISupport.prompt("Select target Project for cloned Interface", "Clone Interface",
                asList);
        if (targetProjectName == null) {
            return;
        }

        WsdlProject targetProject = (WsdlProject) workspace.getProjectByName(targetProjectName);
        if (targetProject == null) {
            targetProjectName = UISupport.prompt("Enter name for new Project", "Clone TestSuite", "");
            if (targetProjectName == null) {
                return;
            }

            try {
                targetProject = workspace.createProject(targetProjectName, null);
            } catch (SoapUIException e) {
                UISupport.showErrorMessage(e);
            }

            if (targetProject == null) {
                return;
            }
        }

        WsdlInterface targetIface = (WsdlInterface) targetProject.getInterfaceByTechnicalId(iface.getTechnicalId());
        if (targetIface != null) {
            UISupport.showErrorMessage("Target Project already contains Interface for binding");
        } else {
            boolean importEndpoints = UISupport.confirm("Import endpoint defaults also?", getName());
            UISupport.select(targetProject.importInterface(iface, importEndpoints, true));
        }
    }
}
