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

package com.eviware.soapui.impl.wsdl.panels.assertions;

import javax.swing.table.DefaultTableModel;
import java.util.Set;

public class AssertionCategoriesTableModel extends DefaultTableModel {
    Set<String> listEntriesSet;

    public AssertionCategoriesTableModel() {
    }

    public void setLisetEntriesSet(Set<String> keySet) {
        listEntriesSet = keySet;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        if (listEntriesSet != null) {
            return listEntriesSet.size();
        } else {
            return 1;
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (listEntriesSet != null) {
            return listEntriesSet.toArray()[row];
        } else {
            return null;
        }
    }

}
