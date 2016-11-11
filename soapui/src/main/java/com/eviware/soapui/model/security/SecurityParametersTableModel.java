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

package com.eviware.soapui.model.security;

import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class SecurityParametersTableModel extends DefaultTableModel {

    private String[] columnNames = new String[]{"Label", "Name", "XPath", "Enabled"};
    private SecurityCheckedParameterHolder holder;

    public SecurityParametersTableModel(SecurityCheckedParameterHolder holder) {
        this.holder = holder;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 1;

    }

    @Override
    public Object getValueAt(int row, int column) {
        SecurityCheckedParameter param = holder.getParameterList().get(row);
        switch (column) {
            case 0:
                return param.getLabel();
            case 1:
                return param.getName();
            case 2:
                return param.getXpath();
            case 3:
                return param.isChecked();
        }
        return super.getValueAt(row, column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 3 ? Boolean.class : columnIndex == 2 ? String.class : Object.class;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (holder.getParameterList().isEmpty()) {
            return;
        }
        SecurityCheckedParameterImpl param = (SecurityCheckedParameterImpl) holder.getParameterList().get(row);
        switch (column) {
            case 0:
                param.setLabel((String) aValue);
                break;
            case 1:
                param.setName((String) aValue);
                break;
            case 2:
                param.setXpath((String) aValue);
                break;
            case 3:
                param.setChecked((Boolean) aValue);
        }
    }

    public boolean addParameter(String label, String name, String xpath) {
        if (holder.addParameter(label, name, xpath, true)) {
            fireTableDataChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getRowCount() {
        return holder == null ? 0 : holder.getParameterList().size();
    }

    public void removeRows(int[] selectedRows) {
        holder.removeParameters(selectedRows);
    }

}
