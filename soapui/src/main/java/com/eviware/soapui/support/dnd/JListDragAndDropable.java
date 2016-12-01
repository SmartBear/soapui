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

package com.eviware.soapui.support.dnd;

import com.eviware.soapui.model.ModelItem;

import javax.swing.JComponent;
import javax.swing.JList;
import java.awt.Point;
import java.awt.Rectangle;

public abstract class JListDragAndDropable<T extends JList> implements SoapUIDragAndDropable<ModelItem> {
    private T list;
    private ModelItem parent;

    public JListDragAndDropable(T list, ModelItem parent) {
        this.list = list;
        this.parent = parent;
    }

    public T getList() {
        return list;
    }

    public abstract ModelItem getModelItemAtRow(int row);

    public JComponent getComponent() {
        return list;
    }

    public Rectangle getModelItemBounds(ModelItem modelItem) {
        if (modelItem == parent) {
            return list.getBounds();
        }

        int ix = getModelItemRow(modelItem);
        return list.getCellBounds(ix, ix);
    }

    public abstract int getModelItemRow(ModelItem modelItem);

    public void selectModelItem(ModelItem modelItem) {
        list.setSelectedIndex(getModelItemRow(modelItem));
    }

    public void setDragInfo(String dropInfo) {
        list.setToolTipText(dropInfo);
    }

    public ModelItem getModelItemForLocation(int x, int y) {
        int index = list.locationToIndex(new Point(x, y));
        return index == -1 ? parent : getModelItemAtRow(index);
    }

    public void toggleExpansion(ModelItem last) {
    }
}
