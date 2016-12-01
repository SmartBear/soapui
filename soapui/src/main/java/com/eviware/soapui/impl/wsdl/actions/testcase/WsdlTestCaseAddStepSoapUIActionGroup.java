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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestCases, dynamically creates "Append Step"
 * submenu contents from the WsdlTestStepRegistry
 *
 * @author ole.matzura
 */

public class WsdlTestCaseAddStepSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestCase> {
    public WsdlTestCaseAddStepSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<WsdlTestCase> getActionMappings(WsdlTestCase modelItem) {
        SoapUIActionMappingList<WsdlTestCase> actions = new SoapUIActionMappingList<WsdlTestCase>();

        WsdlTestStepRegistry registry = WsdlTestStepRegistry.getInstance();
        WsdlTestStepFactory[] factories = (WsdlTestStepFactory[]) registry.getFactories();

        for (int c = 0; c < factories.length; c++) {
            WsdlTestStepFactory factory = factories[c];
            if (factory.canCreate()) {
                DefaultActionMapping<WsdlTestCase> actionMapping = new DefaultActionMapping<WsdlTestCase>(
                        AddWsdlTestStepAction.SOAPUI_ACTION_ID, null, factory.getTestStepIconPath(), false, factory);

                actionMapping.setName(factory.getTestStepName());
                actionMapping.setDescription(factory.getTestStepDescription());

                actions.add(actionMapping);
            }
        }

        return actions;
    }
}
