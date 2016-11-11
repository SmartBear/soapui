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

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.util.List;

public class AssertionsListTable extends JXTable {
    private List<Integer> nonSelectableIndexes;
    private boolean selectable;

    public AssertionsListTable(TableModel tableModel) {
        super(tableModel);
        if (UISupport.isMac()) {
            JTableFactory.setGridAttributes(this);
        }
    }

    public void setNonSelectableIndexes(List<Integer> nonSelectableIndexes) {
        this.nonSelectableIndexes = nonSelectableIndexes;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    /*
	 * Used for disabling certain assertions to be selected. When
	 * AddAssertionPanel is opened and no category has yet been selected all
	 * needs to be disabled (case of recently used assertions being available in
	 * the list but shouldn't be selectable ). After category is selected
	 * underlying assertions are enabled/disabled depending on assertions being
	 * applicable for the source/property
	 */
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (!selectable) {
            rowIndex = -1;
        } else if (nonSelectableIndexes != null && nonSelectableIndexes.contains(rowIndex)) {
            int currentIndex = getSelectedRow();
            if (rowIndex != currentIndex) {
                rowIndex = -1;
            }
        }
        // make the selection change
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component defaultRenderer = super.prepareRenderer(renderer, row, column);
        if (UISupport.isMac()) {
            JTableFactory.applyStripesToRenderer(row, defaultRenderer);
        }
        return defaultRenderer;
    }

    @Override
    public boolean getShowVerticalLines() {
        return UISupport.isMac() ? false : super.getShowVerticalLines();
    }

}
