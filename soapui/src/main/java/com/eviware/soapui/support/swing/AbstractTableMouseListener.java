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

package com.eviware.soapui.support.swing;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Abstract MouseListener for JLists that displays a row-sensitive popup-menu
 *
 * @author ole.matzura
 */

public abstract class AbstractTableMouseListener extends MouseAdapter {
    private boolean enablePopup;
    private JPopupMenu menu;

    protected abstract ActionList getActionsForRow(JTable table, int row);

    public AbstractTableMouseListener() {
        this(true);
    }

    public AbstractTableMouseListener(boolean enablePopup) {
        this.enablePopup = enablePopup;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() < 2) {
            return;
        }

        JTable list = (JTable) e.getSource();

        int selectedIndex = list.getSelectedRow();
        if (selectedIndex == -1) {
            return;
        }

        ActionList actions = getActionsForRow(list, selectedIndex);

        if (actions != null) {
            actions.performDefaultAction(new ActionEvent(this, 0, null));
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void showPopup(MouseEvent e) {
        if (!enablePopup) {
            return;
        }

        JTable list = (JTable) e.getSource();
        int row = list.rowAtPoint(e.getPoint());
        if (row == -1) {
            return;
        }

        if (list.getSelectedRow() != row) {
            list.setRowSelectionInterval(row, row);
        }

        ActionList actions = getActionsForRow(list, row);

        if (actions == null || actions.getActionCount() == 0) {
            return;
        }

        JPopupMenu popup = menu == null ? ActionSupport.buildPopup(actions) : menu;
        UISupport.showPopup(popup, list, e.getPoint());
    }

    public void setPopupMenu(JPopupMenu menu) {
        this.menu = menu;
    }
}
