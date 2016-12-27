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

package com.eviware.soapui.impl.wsdl.actions.support;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for showing the desktop panel for the specified ModelItem
 *
 * @author Ole.Matzura
 */

public class ShowDesktopPanelAction extends AbstractSoapUIAction<ModelItem> {
    public static final String SOAPUI_ACTION_ID = "ShowDesktopPanelAction";

    public ShowDesktopPanelAction() {
        super("Show Desktop Panel", "Show Desktop Panel for this item");
    }

    public void perform(ModelItem target, Object param) {
        UISupport.setHourglassCursor();
        try {
            if (target instanceof WsdlInterface) {
                try {
                    ((WsdlInterface) target).getWsdlContext().loadIfNecessary();
                } catch (Exception e) {
                    UISupport.showErrorMessage(e);
                    return;
                }
            }

            UISupport.selectAndShow(target);
        } finally {
            UISupport.resetCursor();
        }
    }
}
