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
import com.eviware.soapui.security.tools.AttachmentHolder;
import com.eviware.soapui.support.UISupport;

import javax.swing.table.AbstractTableModel;

public abstract class MaliciousAttachmentTableModel extends AbstractTableModel {

    protected AttachmentHolder holder = new AttachmentHolder();

    public int getRowCount() {
        return holder.size();
    }

    public void removeResult(int i) {
        if (UISupport.confirm("Remove selected attachments?", "Remove Attachments")) {
            holder.removeElement(i);
            fireTableDataChanged();
        }
    }

    public void clear() {
        holder.clear();
        fireTableDataChanged();
    }

    public MaliciousAttachmentConfig getRowValue(int rowIndex) {
        return holder.getList().get(rowIndex);
    }

    public abstract int getColumnCount();

    public abstract String getColumnName(int column);

    public abstract Object getValueAt(int rowIndex, int columnIndex);

    public abstract void addResult(MaliciousAttachmentConfig config);
}
