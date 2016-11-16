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

package com.eviware.soapui.support;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class ListDataChangeListener implements ListDataListener {
    public void contentsChanged(ListDataEvent e) {
        dataChanged((ListModel) e.getSource());
    }

    public void intervalAdded(ListDataEvent e) {
        dataChanged((ListModel) e.getSource());
    }

    public void intervalRemoved(ListDataEvent e) {
        dataChanged((ListModel) e.getSource());
    }

    public abstract void dataChanged(ListModel model);
}
