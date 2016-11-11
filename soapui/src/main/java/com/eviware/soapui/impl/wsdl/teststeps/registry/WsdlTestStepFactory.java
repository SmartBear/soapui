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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;

/**
 * Abstract factory behaviour for WsdlTestStep factories
 *
 * @author Ole.Matzura
 */

public abstract class WsdlTestStepFactory {
    private final String typeName;
    private final String name;
    private final String description;
    private final String pathToIcon;

    public WsdlTestStepFactory(String typeName, String name, String description, String pathToIcon) {
        this.typeName = typeName;
        this.name = name;
        this.description = description;
        this.pathToIcon = pathToIcon;
    }

    public abstract WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest);

    public String getType() {
        return typeName;
    }

    public abstract TestStepConfig createNewTestStep(WsdlTestCase testCase, String name);

    public abstract boolean canCreate();

    public boolean promptForName() {
        return true;
    }

    public String getTestStepName() {
        return name;
    }

    public String getTestStepDescription() {
        return description;
    }

    public String getTestStepIconPath() {
        return pathToIcon;
    }

    public boolean canAddTestStepToTestCase(WsdlTestCase testCase) {
        return true;
    }
}
