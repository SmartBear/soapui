/*
 * Copyright 2004-2014 SmartBear Software
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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.WeakPropertyChangeListener;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.StandaloneActionMapping;

/**
 * Delegates a SwingAction to a SoapUIActionMapping
 *
 * @author ole.matzura
 */

public class SwingActionDelegate<T extends ModelItem> extends AbstractAction implements PropertyChangeListener,
        SoapUIActionMarker {
    private final T target;
    private final SoapUIActionMapping<T> mapping;
    private Object param;

    @Deprecated
    public static boolean switchClassloader;

    public SwingActionDelegate(SoapUIActionMapping<T> mapping, T target) {
        super(mapping.getName());
        this.mapping = mapping;
        this.target = target;

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

        mapping.getAction().addPropertyChangeListener(new WeakPropertyChangeListener(this, mapping.getAction()));

        String name = mapping.getName();
        int ix = name.indexOf('&');
        if (ix >= 0) {
            putValue(Action.NAME, name.substring(0, ix) + name.substring(ix + 1));
            // This doesn't seem to work in Java 5:
            // putValue( Action.DISPLAYED_MNEMONIC_INDEX_KEY, new Integer( ix ));
            putValue(Action.MNEMONIC_KEY, new Integer(name.charAt(ix + 1)));
        }
    }

    public SoapUIActionMapping<T> getMapping() {
        return mapping;
    }

    public void actionPerformed(ActionEvent event) {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            mapping.getAction().perform(target, param == null ? mapping.getParam() : param);
        } catch (Exception exception) {
            SoapUI.logError(exception);
        } finally {
            state.restore();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SoapUIAction.ENABLED_PROPERTY)) {
            setEnabled(((Boolean) evt.getNewValue()).booleanValue());
        }
    }

    public SoapUIAction<T> getAction() {
        return mapping.getAction();
    }

    public T getTarget() {
        return target;
    }

    protected Object getParam() {
        return param;
    }

    protected void setParam(Object param) {
        this.param = param;
    }

    public static <T extends ModelItem> SwingActionDelegate<T> createDelegate(SoapUIAction<T> action, T target,
                                                                              String keyStroke, String iconPath) {
        return new SwingActionDelegate<T>(new StandaloneActionMapping<T>(action, keyStroke, iconPath), target);
    }

    public static <T extends ModelItem> SwingActionDelegate<T> createDelegate(SoapUIAction<T> action, T target,
                                                                              String keyStroke) {
        return new SwingActionDelegate<T>(new StandaloneActionMapping<T>(action, keyStroke), target);
    }

    public static <T extends ModelItem> SwingActionDelegate<T> createDelegate(SoapUIAction<T> action, T target) {
        return new SwingActionDelegate<T>(new StandaloneActionMapping<T>(action), target);
    }

    public static <T extends ModelItem> SwingActionDelegate<T> createDelegate(SoapUIAction<T> action) {
        return new SwingActionDelegate<T>(new StandaloneActionMapping<T>(action), null);
    }

    public static SwingActionDelegate<?> createDelegate(String soapUIActionId) {
        return createDelegate(SoapUI.getActionRegistry().getAction(soapUIActionId));
    }

    public static <T extends ModelItem> SwingActionDelegate<?> createDelegate(String soapUIActionId, T target) {
        return createDelegate(SoapUI.getActionRegistry().getAction(soapUIActionId), target);
    }

    public static <T extends ModelItem> SwingActionDelegate<?> createDelegate(String soapUIActionId, T target,
                                                                              String keyStroke) {
        return createDelegate(SoapUI.getActionRegistry().getAction(soapUIActionId), target, keyStroke);
    }

    public static <T extends ModelItem> SwingActionDelegate<?> createDelegate(String soapUIActionId, T target,
                                                                              String keyStroke, String iconPath) {
        return createDelegate(SoapUI.getActionRegistry().getAction(soapUIActionId), target, keyStroke, iconPath);
    }

    public SoapUIAction<?> getSoapUIAction() {
        return getAction();
    }

    public static void invoke(Runnable action) {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();

        try {
            action.run();
        } catch (Throwable t) {
            SoapUI.logError(t);
        } finally {
            state.restore();
        }
    }
}
