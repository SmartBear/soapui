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

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class JCollapsibleTablePanel extends JCollapsiblePanel {

    private JTable table;
    private String title;

    public JCollapsibleTablePanel(JTable table, String title) {
        super(new JPanel(), title);
        setTable(table);
        setMinusIcon(UISupport.createImageIcon("/minus.gif"));
        setPlusIcon(UISupport.createImageIcon("/plus.gif"));
        this.title = title;
    }

    private void setTable(JTable table) {
        this.table = table;
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel content = new JPanel(new BorderLayout());
        content.add(table, BorderLayout.CENTER);
        setContentPanel(content);
    }

    public JTable getTable() {
        return table;
    }

    public String getTitle() {
        return title;
    }
}
