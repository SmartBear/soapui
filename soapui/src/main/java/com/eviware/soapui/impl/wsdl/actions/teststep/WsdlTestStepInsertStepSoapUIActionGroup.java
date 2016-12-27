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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIAction group for dynamically creating the "Insert TestStep" popup menu
 *
 * @author ole.matzura
 */

public class WsdlTestStepInsertStepSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestStep> {
    public WsdlTestStepInsertStepSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<WsdlTestStep> getActionMappings(WsdlTestStep modelItem) {
        SoapUIActionMappingList<WsdlTestStep> actions = new SoapUIActionMappingList<WsdlTestStep>();

        WsdlTestStepRegistry registry = WsdlTestStepRegistry.getInstance();
        WsdlTestStepFactory[] factories = (WsdlTestStepFactory[]) registry.getFactories();

        for (int c = 0; c < factories.length; c++) {
            WsdlTestStepFactory factory = factories[c];
            if (factory.canCreate()) {
                DefaultActionMapping<WsdlTestStep> actionMapping = new DefaultActionMapping<WsdlTestStep>(
                        InsertWsdlTestStepAction.SOAPUI_ACTION_ID, null, factory.getTestStepIconPath(), false, factory);

                actionMapping.setName(factory.getTestStepName());
                actionMapping.setDescription(factory.getTestStepDescription());

                actions.add(actionMapping);
            }
        }

        return actions;
    }
}
