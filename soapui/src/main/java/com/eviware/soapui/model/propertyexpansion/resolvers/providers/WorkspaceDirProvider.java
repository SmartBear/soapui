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

package com.eviware.soapui.model.propertyexpansion.resolvers.providers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver.ValueProvider;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.workspace.Workspace;

public class WorkspaceDirProvider implements ValueProvider {
    public String getValue(PropertyExpansionContext context) {
        Workspace workspace = SoapUI.getWorkspace();

        if (workspace == null) {
            ModelItem modelItem = context.getModelItem();
            if (modelItem instanceof Workspace) {
                workspace = (Workspace) modelItem;
            } else {
                Project project = ModelSupport.getModelItemProject(modelItem);
                if (project != null) {
                    workspace = project.getWorkspace();
                }
            }
        }

        return workspace == null ? null : PathUtils.getAbsoluteFolder(workspace.getPath());
    }
}
