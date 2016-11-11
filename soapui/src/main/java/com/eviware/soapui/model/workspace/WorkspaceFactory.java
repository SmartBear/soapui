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

package com.eviware.soapui.model.workspace;

import com.eviware.soapui.impl.WorkspaceFactoryImpl;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Factory class for creating Workspaces
 *
 * @author Ole.Matzura
 */

public abstract class WorkspaceFactory {
    private static WorkspaceFactory instance;

    public static WorkspaceFactory getInstance() {
        if (instance == null) {
            instance = new WorkspaceFactoryImpl();
        }

        return instance;
    }

    public abstract Workspace openWorkspace(String workspaceName, StringToStringMap projectOptions)
            throws SoapUIException;
}
