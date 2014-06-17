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

package com.eviware.soapui.impl.wsdl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.ProjectFactory;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.SoapUIException;

public class WsdlProjectFactory implements ProjectFactory<WsdlProject> {

    public static final String WSDL_TYPE = "wsdl";

    public WsdlProject createNew() throws XmlException, IOException, SoapUIException {
        return new WsdlProject();
    }

    public WsdlProject createNew(String path) throws XmlException, IOException, SoapUIException {
        return new WsdlProject(path);
    }

    public WsdlProject createNew(String projectFile, String projectPassword) {
        return new WsdlProject(projectFile, (WorkspaceImpl) null, true, null, projectPassword);
    }

    public WsdlProject createNew(Workspace workspace) {
        return new WsdlProject((String)null, (WorkspaceImpl) workspace);
    }

    public WsdlProject createNew(String path, Workspace workspace) {
        return new WsdlProject(path, (WorkspaceImpl) workspace);
    }

    public WsdlProject createNew(String path, Workspace workspace, boolean create) {
        return new WsdlProject(path, (WorkspaceImpl) workspace, true, null, null);
    }

    public WsdlProject createNew(String path, Workspace workspace, boolean open, String tempName,
                                 String projectPassword) {
        return new WsdlProject(path, (WorkspaceImpl) workspace, open, tempName, projectPassword);
    }

    @Override
    public WsdlProject createNew(InputStream inputStream, WorkspaceImpl workspace) {
        return new WsdlProject(inputStream, workspace);
    }

}
