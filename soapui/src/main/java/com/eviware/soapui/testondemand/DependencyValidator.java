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

package com.eviware.soapui.testondemand;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.InterfaceExternalDependency;
import com.eviware.soapui.impl.wsdl.support.MockServiceExternalDependency;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DependencyValidator {

    private File filename;

    public File getFilename() {
        return filename;
    }

    /*
     * make sure all interfaces are loaded
     */
    protected void loadInterfaces(WsdlProject project) {
        for (Interface inf : project.getInterfaceList()) {
            try {
                if (inf instanceof WsdlInterface) {
                    ((WsdlInterface) inf).getWsdlContext().loadIfNecessary();
                } else {
                    ((RestService) inf).getDefinitionContext().loadIfNecessary();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * save interfaces to project
     */
    protected void saveProject(WsdlProject project) throws IOException {
        boolean shouldCache = project.isCacheDefinitions();

        try {
            File tempFile = File.createTempFile("project-temp-", ".xml", null);
            // cache interfaces
            if (!shouldCache) {
                project.setCacheDefinitions(true);
            }
            project.saveIn(tempFile);
            this.filename = tempFile;
        } finally {
            project.setCacheDefinitions(shouldCache);
        }

    }

    public boolean isValid(WsdlTestCase testCase) {
        if (testCase != null) {
            WsdlProject project = testCase.getTestSuite().getProject();

            // perform validations
            validate(project);

            List<ExternalDependency> deps = project.getExternalDependencies();
            for (ExternalDependency d : deps) {
                if (!(d instanceof InterfaceExternalDependency || d instanceof MockServiceExternalDependency)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void validate(WsdlProject project) {
        loadInterfaces(project);

        try {
            saveProject(project);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
