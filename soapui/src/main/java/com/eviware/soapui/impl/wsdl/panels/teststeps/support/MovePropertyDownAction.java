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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import java.awt.event.ActionEvent;

/**
 * Action that moves a property down in the display order of a table.
 */
public class MovePropertyDownAction extends AbstractAction {
    public static final String MOVE_PROPERTY_DOWN_ACTION_NAME = "Move Property Down";
    private final JTable propertyTable;
    private final MutableTestPropertyHolder propertyHolder;

    public MovePropertyDownAction(JTable propertyTable, MutableTestPropertyHolder propertyHolder, String description) {
        super(MOVE_PROPERTY_DOWN_ACTION_NAME);
        this.propertyTable = propertyTable;
        this.propertyHolder = propertyHolder;
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/down_arrow.gif"));
        putValue(Action.SHORT_DESCRIPTION, description);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        int ix = propertyTable.getSelectedRow();
        if (ix != -1) {
            String propName = (String) propertyTable.getValueAt(ix, 0);
            ((PropertyHolderTableModel) propertyTable.getModel()).moveProperty(propName, ix, ix + 1);
            propertyTable.setRowSelectionInterval(ix + 1, ix + 1);
        }
    }
}
