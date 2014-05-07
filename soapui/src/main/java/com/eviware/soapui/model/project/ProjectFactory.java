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

package com.eviware.soapui.model.project;

import java.io.IOException;
import java.io.InputStream;

import com.eviware.soapui.impl.WorkspaceImpl;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.SoapUIException;

public interface ProjectFactory<T extends Project> {
    public T createNew() throws XmlException, IOException, SoapUIException;

    public T createNew(String path) throws XmlException, IOException, SoapUIException;

    public T createNew(String projectFile, String projectPassword);

    public T createNew(Workspace workspace);

    public T createNew(String path, Workspace workspace);

    public T createNew(String path, Workspace workspace, boolean open, String tempName,
                       String projectPassword);

    public T createNew(InputStream inputStream, WorkspaceImpl workspace);
}
