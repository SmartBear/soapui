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
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.environment.EnvironmentListener;
import com.eviware.soapui.model.support.TestPropertyUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.beans.PropertyChangeListener;

public class DirectAccessPropertyHolderTableModel<T extends TestPropertyHolder>
        extends DefaultPropertyHolderTableModel<T>
        implements PropertyHolderTableModel, EnvironmentListener, PropertyChangeListener {
    protected final InternalTestPropertyListener testPropertyListener;

    public DirectAccessPropertyHolderTableModel(T holder) {
        this.params = holder;

        testPropertyListener = new InternalTestPropertyListener();
        holder.addTestPropertyListener(testPropertyListener);
    }

    public int getRowCount() {
        return params.getPropertyCount();
    }


    public TestProperty getPropertyAtRow(int rowIndex) {
        return params.getPropertyAt(rowIndex);
    }

    @Override
    public void moveProperty(String name, int oldIndex, int newIndex) {
        ((MutableTestPropertyHolder) params).moveProperty(name, newIndex);
        testPropertyListener.propertyMoved(name, oldIndex, newIndex);
    }

    public void sort() {
        TestPropertyUtils.sortProperties(((MutableTestPropertyHolder) params));
        fireTableDataChanged();
    }

    protected final class InternalTestPropertyListener implements TestPropertyListener {
        public void propertyAdded(String name) {
            fireTableDataChanged();
        }

        public void propertyRemoved(String name) {
            isLastChangeParameterLevelChange = false;
            fireTableDataChanged();
        }

        public void propertyRenamed(String oldName, String newName) {
            fireTableDataChanged();
        }

        public void propertyValueChanged(String name, String oldValue, String newValue) {
            fireTableDataChanged();
        }

        public void propertyMoved(String name, int oldIndex, int newIndex) {
            fireTableDataChanged();
        }
    }

}
