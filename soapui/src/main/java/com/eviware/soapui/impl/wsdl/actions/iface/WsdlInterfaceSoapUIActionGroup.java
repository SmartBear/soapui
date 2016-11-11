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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * ActionGroup handler for WsdlInterface
 *
 * @author ole.matzura
 */

public class WsdlInterfaceSoapUIActionGroup extends DefaultSoapUIActionGroup<WsdlInterface> {
    public WsdlInterfaceSoapUIActionGroup(String id, String name) {
        super(id, name);
    }

    @Override
    public SoapUIActionMappingList<WsdlInterface> getActionMappings(WsdlInterface modelItem) {
        SoapUIActionMappingList<WsdlInterface> actionMappings = super.getActionMappings(modelItem);

        actionMappings.getMapping(AddJMSEndpointAction.SOAPUI_ACTION_ID)
                .setEnabled(HermesUtils.isHermesJMSSupported());
        // SoapUIActionMapping<WsdlInterface> mapping = actionMappings.getMapping(
        // WSToolsRegenerateJava2WsdlAction.SOAPUI_ACTION_ID );
        // WSToolsRegenerateJava2WsdlAction action = (
        // WSToolsRegenerateJava2WsdlAction ) mapping.getAction();
        // mapping.setEnabled( modelItem.getSettings().isSet(
        // action.getValuesSettingID() ) );

        return actionMappings;
    }
}
