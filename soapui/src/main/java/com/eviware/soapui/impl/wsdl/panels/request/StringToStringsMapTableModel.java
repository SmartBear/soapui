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

package com.eviware.soapui.impl.wsdl.panels.request;

import com.eviware.soapui.support.types.StringToStringsMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel for StringToString Maps
 *
 * @author ole.matzura
 */

public class StringToStringsMapTableModel extends AbstractTableModel implements TableModel {
    private final String keyCaption;
    private final String valueCaption;
    private List<NameValuePair> keyList = new ArrayList<NameValuePair>();
    private final boolean editable;
    private StringToStringsMap data;

    public StringToStringsMapTableModel(StringToStringsMap data, String keyCaption, String valueCaption,
                                        boolean editable) {
        this.data = data;
        this.keyCaption = keyCaption;
        this.valueCaption = valueCaption;
        this.editable = editable;

        setData(data);
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int arg0) {
        return arg0 == 0 ? keyCaption : valueCaption;
    }

    public boolean isCellEditable(int arg0, int arg1) {
        return editable;
    }

    public Class<?> getColumnClass(int arg0) {
        return String.class;
    }

    public void setValueAt(Object arg0, int arg1, int arg2) {
        NameValuePair nvpair = keyList.get(arg1);

        // change name?
        if (arg2 == 0) {
            data.get(nvpair.getKey()).remove(nvpair.getIndex());
            nvpair.setKey(String.valueOf(arg0));
            data.put(nvpair.getKey(), nvpair.getIndex());

        } else if (arg2 == 1) {
            data.replace(nvpair.getKey(), nvpair.getIndex(), String.valueOf(arg0));
            nvpair.setValue(String.valueOf(arg0));
        }

        fireTableCellUpdated(arg1, arg2);
    }

    public int getRowCount() {
        return keyList.size();
    }

    public Object getValueAt(int arg0, int arg1) {
        return arg1 == 0 ? keyList.get(arg0).getKey() : keyList.get(arg0).getIndex();
    }

    public void add(String key, String value) {
        data.add(key, value);
        keyList.add(new NameValuePair(key, value));
        fireTableRowsInserted(keyList.size() - 1, keyList.size() - 1);
    }

    public void remove(int row) {
        NameValuePair key = keyList.get(row);
        keyList.remove(row);
        data.remove(key.getKey(), key.getIndex());

        fireTableRowsDeleted(row, row);
    }

    public StringToStringsMap getData() {
        return new StringToStringsMap(data);
    }

    public synchronized void setData(StringToStringsMap newData) {
        data = newData == null ? new StringToStringsMap() : new StringToStringsMap(newData);

        keyList.clear();
        for (String key : data.keySet()) {
            for (String value : data.get(key)) {
                keyList.add(new NameValuePair(key, value));
            }
        }

        fireTableDataChanged();
    }

    private class NameValuePair {
        private String key;
        private String value;

        public NameValuePair(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getIndex() {
            return value;
        }
    }
}
