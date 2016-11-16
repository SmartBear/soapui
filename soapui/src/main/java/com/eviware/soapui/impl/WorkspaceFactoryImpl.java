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

package com.eviware.soapui.impl;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Default WorkspaceFactory implementation
 *
 * @author Ole.Matzura
 */

public class WorkspaceFactoryImpl extends WorkspaceFactory {
    public Workspace openWorkspace(String workspaceName, StringToStringMap projectOptions) throws SoapUIException {
        try {
            return new WorkspaceImpl(workspaceName, projectOptions);
        } catch (Exception e) {
            throw new SoapUIException(e);
        }
    }
}
