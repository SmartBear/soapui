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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestSteps
 *
 * @author ole.matzura
 */

public class WsdlTestSuiteSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestSuite> {
    public WsdlTestSuiteSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<WsdlTestSuite> getActionMappings(WsdlTestSuite modelItem) {
        SoapUIActionMappingList<WsdlTestSuite> actions = super.getActionMappings(modelItem);
        SoapUIActionMapping<WsdlTestSuite> toggleDisabledActionMapping = null;

        for (int c = 0; c < actions.size(); c++) {
            if (actions.get(c).getActionId().equals(ToggleDisableTestSuiteAction.SOAPUI_ACTION_ID)) {
                toggleDisabledActionMapping = actions.get(c);
                break;
            }
        }

        if (toggleDisabledActionMapping != null && modelItem != null) {
            if (modelItem.isDisabled()) {
                toggleDisabledActionMapping.setName("Enable TestSuite");
                toggleDisabledActionMapping.setDescription("Enable this TestSuite");
            } else {
                toggleDisabledActionMapping.setName("Disable TestSuite");
                toggleDisabledActionMapping.setDescription("Disables this TestSuite");
            }
        }

        return actions;
    }
}
