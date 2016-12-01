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

package com.eviware.soapui.support.action.swing;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.SoapUIMultiAction;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Delegates a SwingAction to a SoapUIActionMapping containgin a
 * SoapUIMultiAction
 *
 * @author ole.matzura
 */

public class SwingMultiActionDelegate extends AbstractAction implements PropertyChangeListener, SoapUIActionMarker {
    private final SoapUIActionMapping<?> mapping;
    private ModelItem[] targets;

    public SwingMultiActionDelegate(SoapUIActionMapping<?> mapping, ModelItem[] targets) {
        super(mapping.getName());
        this.mapping = mapping;
        this.targets = targets;

        if (mapping.getDescription() != null) {
            putValue(Action.SHORT_DESCRIPTION, mapping.getDescription());
        }

        if (mapping.getIconPath() != null) {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon(mapping.getIconPath()));
        }

        if (mapping.getKeyStroke() != null) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke(mapping.getKeyStroke()));
        }

        setEnabled(mapping.getAction().isEnabled());

        String name = mapping.getName();
        int ix = name.indexOf('&');
        if (ix >= 0) {
            putValue(Action.NAME, name.substring(0, ix) + name.substring(ix + 1));
            // This doesn't seem to work in Java 5:
            // putValue( Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer( ix ));
            putValue(Action.MNEMONIC_KEY, new Integer(name.charAt(ix + 1)));
        }
    }

    public SoapUIActionMapping<?> getMapping() {
        return mapping;
    }

    public void actionPerformed(ActionEvent e) {
        // required by IDE plugins
        if (SwingActionDelegate.switchClassloader) {
            SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

            try {
                ((SoapUIMultiAction) mapping.getAction()).perform(targets, mapping.getParam());
            } catch (Throwable t) {
                SoapUI.logError(t);
            } finally {
                state.restore();
            }
        } else {
            try {
                ((SoapUIMultiAction) mapping.getAction()).perform(targets, mapping.getParam());
            } catch (Throwable t) {
                SoapUI.logError(t);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SoapUIAction.ENABLED_PROPERTY)) {
            setEnabled(((Boolean) evt.getNewValue()).booleanValue());
        }
    }

    public ModelItem[] getTargets() {
        return targets;
    }

    public SoapUIAction<?> getSoapUIAction() {
        return mapping.getAction();
    }
}
