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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.tree.nodes.support.EmptyModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Base class for DesktopPanels..
 */

public abstract class ModelItemDesktopPanel<T extends ModelItem> extends JPanel implements DesktopPanel,
        PropertyChangeListener {
    private final T modelItem;

    public ModelItemDesktopPanel(T modelItem) {
        super(new BorderLayout());
        this.modelItem = modelItem;

        modelItem.addPropertyChangeListener(this);
    }

    protected boolean release() {
        modelItem.removePropertyChangeListener(this);
        return true;
    }

    public JComponent getComponent() {
        return this;
    }

    final public T getModelItem() {
        return modelItem;
    }

    public Icon getIcon() {
        return modelItem.getIcon();
    }

    public boolean dependsOn(ModelItem modelItem) {
        return ModelSupport.dependsOn(getModelItem(), modelItem);
    }

    public String getTitle() {
        return modelItem.getName();
    }

    public final String getDescription() {
        TreePath treePath = SoapUI.getNavigator().getTreePath(modelItem);

        if (treePath == null) {
            return modelItem.getDescription();
        } else {
            String str = modelItem.getName() + " [";

            for (int c = 1; c < treePath.getPathCount(); c++) {
                SoapUITreeNode comp = (SoapUITreeNode) treePath.getPathComponent(c);
                if (comp.getModelItem() instanceof EmptyModelItem) {
                    continue;
                }

                if (c > 1) {
                    str += "/";
                }

                str += comp.toString();
            }

            str += "]";

            return str;
        }
    }

    public static JButton createActionButton(Action action, boolean enabled) {
        JButton button = UISupport.createToolbarButton(action, enabled);
        action.putValue(Action.NAME, null);
        return button;
    }

    public void notifyPropertyChange(String propertyName, Object oldValue, Object newValue) {
        firePropertyChange(propertyName, oldValue, newValue);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ModelItem.NAME_PROPERTY)) {
            notifyPropertyChange(DesktopPanel.TITLE_PROPERTY, null, getTitle());
        }

        if (evt.getPropertyName().equals(ModelItem.ICON_PROPERTY)) {
            notifyPropertyChange(DesktopPanel.ICON_PROPERTY, null, getIcon());
        }
    }

    @Override
    public boolean onClose(boolean canCancel) {
        return release();
    }
}
