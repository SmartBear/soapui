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

import javax.swing.JList;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Abstract MouseListener for JLists that displays a row-sensitive popup-menu
 *
 * @author ole.matzura
 */

public abstract class AbstractListMouseListener extends MouseAdapter {
    private boolean enablePopup;
    private JPopupMenu menu;

    protected abstract ActionList getActionsForRow(JList list, int row);

    public AbstractListMouseListener() {
        this(true);
    }

    public AbstractListMouseListener(boolean enablePopup) {
        this.enablePopup = enablePopup;
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() < 2) {
            return;
        }

        JList list = (JList) e.getSource();

        int selectedIndex = list.getSelectedIndex();

        ActionList actions = selectedIndex == -1 ? getDefaultActions() : getActionsForRow(list, selectedIndex);

        if (actions != null) {
            actions.performDefaultAction(new ActionEvent(this, 0, null));
        }
    }

    protected ActionList getDefaultActions() {
        return null;
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

        ActionList actions = null;
        JList list = (JList) e.getSource();
        int row = list.locationToIndex(e.getPoint());
        if (row == -1 || !list.getCellBounds(row, row).contains(e.getPoint())) {
            if (list.getSelectedIndex() != -1) {
                list.clearSelection();
            }

            actions = getDefaultActions();
        } else {
            if (list.getSelectedIndex() != row) {
                list.setSelectedIndex(row);
            }

            actions = getActionsForRow(list, row);
        }

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
