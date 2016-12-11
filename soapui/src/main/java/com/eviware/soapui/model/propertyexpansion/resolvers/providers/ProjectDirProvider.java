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

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.DynamicPropertyResolver.ValueProvider;
import com.eviware.soapui.model.support.ModelSupport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ProjectDirProvider implements ValueProvider {
    public String getValue(PropertyExpansionContext context) {
        Project project = ModelSupport.getModelItemProject(context.getModelItem());
        if (project != null) {
            return getProjectFolder(project);
        }

        return null;
    }

    public static String getProjectFolder(Project project) {
        if (project.getPath() != null) {
            File file = new File(project.getPath());
            if (file.exists()) {
                return new File(file.getAbsolutePath()).getParent();
            } else {
                try {
                    URL url = new URL(project.getPath());
                    String str = url.getProtocol() + "://" + url.getHost()
                            + ((url.getPort() != -1 ? ":" + url.getPort() : "")) + url.getPath();
                    int ix = str.lastIndexOf('/');
                    if (ix != -1) {
                        return str.substring(0, ix);
                    }
                } catch (MalformedURLException e) {
                }
            }
        }

        return null;
    }
}
