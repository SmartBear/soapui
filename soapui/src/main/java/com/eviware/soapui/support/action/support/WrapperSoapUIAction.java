/*
 * SoapUI, Copyright (C) 2004-2017 SmartBear Software
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

package com.eviware.soapui.support.action.support;

import com.eviware.soapui.model.ModelItem;

import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Created by ole on 20/08/14.
 */
public class WrapperSoapUIAction extends AbstractSoapUIAction {
    private final Action swingAction;

    public WrapperSoapUIAction(Action swingAction) {
        super(swingAction.getClass().getSimpleName(),
                String.valueOf(swingAction.getValue(Action.NAME)),
                String.valueOf(swingAction.getValue(Action.SHORT_DESCRIPTION)));
        this.swingAction = swingAction;
    }

    @Override
    public void perform(ModelItem target, Object param) {
        swingAction.actionPerformed(new ActionEvent(target, 1, String.valueOf(param)));
    }
}
