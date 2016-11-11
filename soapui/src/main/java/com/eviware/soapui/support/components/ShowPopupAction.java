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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class ShowPopupAction extends AbstractAction {
    private final JComponent popupContainer;
    private final JComponent container;

    public ShowPopupAction(JComponent popupContainer, JComponent container) {
        this.popupContainer = popupContainer;
        this.container = container;

        putValue(SMALL_ICON, UISupport.createImageIcon("/get_data_button.gif"));
    }

    public void actionPerformed(ActionEvent e) {
        popupContainer.getComponentPopupMenu().show(container, container.getWidth() / 2, container.getHeight() / 2);
    }
}
