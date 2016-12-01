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

package com.eviware.soapui.security.support;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.support.UISupport;

import java.io.File;

public class MaliciousAttachmentReplaceTableModel extends MaliciousAttachmentTableModel {

    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 3 ? Boolean.class : columnIndex == 2 ? String.class : String.class;
    }

    public boolean isCellEditable(int row, int col) {
        if (col > 1) {
            return true;
        } else {
            return false;
        }
    }

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "With";
            case 1:
                return "Size";
            case 2:
                return "Content type";
            case 3:
                return "Enable";
        }

        return null;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        MaliciousAttachmentConfig element = holder.getList().get(rowIndex);

        if (element != null) {
            switch (columnIndex) {
                case 0:
                    return (element.getCached()) ? new File(element.getFilename()).getName() : new File(
                            element.getFilename()).getAbsolutePath();
                case 1:
                    return element.getSize();
                case 2:
                    return element.getContentType();
                case 3:
                    return element.getEnabled();
            }
        }

        return null;
    }

    public void setValueAt(Object aValue, int row, int column) {
        if (holder.getList().isEmpty()) {
            return;
        }

        MaliciousAttachmentConfig element = holder.getList().get(row);

        switch (column) {
            case 2:
                element.setContentType((String) aValue);
                break;
            case 3:
                element.setEnabled((Boolean) aValue);
                break;
        }
    }

    public void addResult(MaliciousAttachmentConfig config) {
        try {
            holder.addElement(config);
            // addFile( file, cached );
            fireTableDataChanged();
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

}
