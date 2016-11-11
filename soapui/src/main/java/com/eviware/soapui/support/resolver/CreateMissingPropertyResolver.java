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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class CreateMissingPropertyResolver implements Resolver {
    private boolean resolved = false;
    private PropertyTransfersTestStep parentPropertyTestStep = null;
    private PropertyTransfer badTransfer = null;

    public CreateMissingPropertyResolver(PropertyTransfer transfer, PropertyTransfersTestStep parent) {
        parentPropertyTestStep = parent;
        badTransfer = transfer;
    }

    public String getDescription() {
        return "Create new property";
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getResolvedPath() {
        return null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        WsdlProject project = parentPropertyTestStep.getTestCase().getTestSuite().getProject();

        String name = UISupport.prompt("Specify unique property name", "Add Property", "");
        if (StringUtils.hasContent(name)) {
            if (project.hasProperty(name)) {
                UISupport.showErrorMessage("Property name [" + name
                        + "] already exists. Property transfer will be disabled.");
                badTransfer.setDisabled(true);

            } else {
                TestProperty newProperty = project.addProperty(name);
                name = UISupport.prompt("What is default value for property " + name, "Add Property Value", "");
                if (StringUtils.hasContent(name)) {
                    newProperty.setValue(name);
                } else {
                    newProperty.setValue(newProperty.getName());
                }
                badTransfer.setSourcePropertyName(newProperty.getName());
                resolved = true;
            }
        } else {
            UISupport.showInfoMessage("Canceled. Property transfer will be disabled.");
            badTransfer.setDisabled(true);
        }
        return resolved;
    }

}
