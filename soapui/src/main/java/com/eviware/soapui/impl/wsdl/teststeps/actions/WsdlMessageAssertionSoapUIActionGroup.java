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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for a WsdlMessageAssertion
 *
 * @author ole.matzura
 */

public class WsdlMessageAssertionSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlMessageAssertion> {
    public WsdlMessageAssertionSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    @Override
    public SoapUIActionMappingList<WsdlMessageAssertion> getActionMappings(WsdlMessageAssertion modelItem) {
        SoapUIActionMappingList<WsdlMessageAssertion> actions = super.getActionMappings(modelItem);
        SoapUIActionMappingList<WsdlMessageAssertion> result = new SoapUIActionMappingList<WsdlMessageAssertion>(actions);

        if (modelItem.isConfigurable()) {
            DefaultActionMapping<WsdlMessageAssertion> actionMapping = new DefaultActionMapping<WsdlMessageAssertion>(
                    ConfigureAssertionAction.SOAPUI_ACTION_ID, "ENTER", null, true, null);

            actionMapping.setName("Configure");
            actionMapping.setDescription("Configures this Assertion");

            result.add(0, actionMapping);
        }

        if (modelItem.isClonable()) {
            DefaultActionMapping<WsdlMessageAssertion> actionMapping = new DefaultActionMapping<WsdlMessageAssertion>(
                    CloneAssertionAction.SOAPUI_ACTION_ID, "F9", null, true, null);

            result.add(1, actionMapping);
        }

        // result.add( 1, SeperatorAction.getDefaultMapping() );

        SoapUIActionMapping<WsdlMessageAssertion> toggleDisabledActionMapping = null;
        for (int c = 0; c < result.size(); c++) {
            if (result.get(c).getActionId().equals(ToggleDisableAssertionAction.SOAPUI_ACTION_ID)) {
                toggleDisabledActionMapping = result.get(c);
                break;
            }
        }

        if (toggleDisabledActionMapping != null) {
            if (modelItem.isDisabled()) {
                toggleDisabledActionMapping.setName("Enable");
                toggleDisabledActionMapping.setDescription("Enable this Assertion");
            } else {
                toggleDisabledActionMapping.setName("Disable");
                toggleDisabledActionMapping.setDescription("Disables this Assertion");
            }
        }

        return result;
    }
}
