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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.support.ShowDesktopPanelAction;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlTestSteps
 *
 * @author ole.matzura
 */

public class WsdlTestStepSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlTestStep> {
    private boolean initialized;

    public WsdlTestStepSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    public SoapUIActionMappingList<WsdlTestStep> getActionMappings(WsdlTestStep modelItem) {
        SoapUIActionMappingList<WsdlTestStep> actions = super.getActionMappings(modelItem);
        SoapUIActionMapping<WsdlTestStep> toggleDisabledActionMapping = null;

        if (!initialized) {
            int insertIndex = 0;

            // add open-editor action
            if (modelItem.hasEditor()) {
                DefaultActionMapping<WsdlTestStep> actionMapping = new DefaultActionMapping<WsdlTestStep>(
                        ShowDesktopPanelAction.SOAPUI_ACTION_ID, "ENTER", null, true, null);

                actionMapping.setName("Open Editor");
                actionMapping.setDescription("Opens the editor for this TestStep");

                actions.add(0, actionMapping);
                insertIndex++;
            }

            toggleDisabledActionMapping = new DefaultActionMapping<WsdlTestStep>(
                    ToggleDisableTestStepAction.SOAPUI_ACTION_ID, null, null, false, null);

            actions.add(insertIndex, toggleDisabledActionMapping);
            insertIndex++;

            // add default teststep actions
            SoapUIActionGroup<WsdlTestStep> actionGroup = SoapUI.getActionRegistry()
                    .getActionGroup("WsdlTestStepActions");
            if (actionGroup != null) {
                actions.addAll(insertIndex, actionGroup.getActionMappings(modelItem));
            }

            initialized = true;
        } else {
            for (int c = 0; c < actions.size(); c++) {
                if (actions.get(c).getActionId().equals(ToggleDisableTestStepAction.SOAPUI_ACTION_ID)) {
                    toggleDisabledActionMapping = actions.get(c);
                    break;
                }
            }
        }

        if (toggleDisabledActionMapping != null && modelItem != null) {
            if (modelItem.isDisabled()) {
                toggleDisabledActionMapping.setName("Enable TestStep");
                toggleDisabledActionMapping.setDescription("Enable this TestStep during TestCase execution");
            } else {
                toggleDisabledActionMapping.setName("Disable TestStep");
                toggleDisabledActionMapping.setDescription("Disables this TestStep during TestCase execution");
            }
        }

        return actions;
    }
}
