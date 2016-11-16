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

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.UISupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DragAndDropSupport {

    public static boolean copyTestStep(WsdlTestStep source, WsdlTestCase target, int defaultPosition) {
        String name = UISupport.prompt("Enter name for copied TestStep", "Copy TestStep",
                target == source.getTestCase() ? "Copy of " + source.getName() : source.getName());
        if (name == null) {
            return false;
        }

        WsdlProject sourceProject = source.getTestCase().getTestSuite().getProject();
        WsdlProject targetProject = target.getTestSuite().getProject();

        if (sourceProject != targetProject) {
            if (!importRequiredInterfaces(targetProject, new HashSet<Interface>(source.getRequiredInterfaces()),
                    "Copy Test Step")) {
                return false;
            }
        }

        target.importTestStep(source, name, defaultPosition, true);

        return true;
    }

    public static boolean importRequiredInterfaces(Project project, Set<Interface> requiredInterfaces, String title) {
        if (requiredInterfaces.size() > 0 && project.getInterfaceCount() > 0) {
            Map<String, Interface> bindings = new HashMap<String, Interface>();
            for (Interface iface : requiredInterfaces) {
                bindings.put(iface.getTechnicalId(), iface);
            }

            for (Interface iface : project.getInterfaceList()) {
                bindings.remove(iface.getTechnicalId());
            }

            requiredInterfaces.retainAll(bindings.values());
        }

        if (requiredInterfaces.size() > 0) {
            String msg = "Target project [" + project.getName() + "] is missing required Interfaces;\r\n\r\n";
            for (Interface iface : requiredInterfaces) {
                msg += iface.getName() + " [" + iface.getTechnicalId() + "]\r\n";
            }
            msg += "\r\nThese will be cloned to the target project as well";

            if (!UISupport.confirm(msg, title)) {
                return false;
            }

            for (Interface iface : requiredInterfaces) {
                ((WsdlProject) project).importInterface((AbstractInterface<?>) iface, true, true);
            }
        }

        return true;
    }

    public static boolean moveTestStep(WsdlTestStep source, WsdlTestCase target, int defaultPosition) {
        if (source.getTestCase() == target) {
            int ix = target.getIndexOfTestStep(source);

            if (defaultPosition == -1) {
                target.moveTestStep(ix, target.getTestStepCount() - ix);
            } else if (ix >= 0 && defaultPosition != ix) {
                int offset = defaultPosition - ix;
                if (offset > 0) {
                    offset--;
                }
                target.moveTestStep(ix, offset);
            }
        } else {
            String name = UISupport.prompt("Enter name for moved TestStep", "Move TestStep", source.getName());
            if (name == null) {
                return false;
            }

            WsdlProject sourceProject = source.getTestCase().getTestSuite().getProject();
            WsdlProject targetProject = target.getTestSuite().getProject();

            if (sourceProject != targetProject) {
                if (!importRequiredInterfaces(targetProject, new HashSet<Interface>(source.getRequiredInterfaces()),
                        "Move Test Step")) {
                    return false;
                }
            }

            final WsdlTestStep result = target.importTestStep(source, name, defaultPosition, false);
            if (result == null) {
                return false;
            }
            source.getTestCase().removeTestStep(source);
        }

        return true;
    }

}
