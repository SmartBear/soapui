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

package com.eviware.soapui.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Helper to find Swing/AWT components in a Container.
 */
public class ContainerWalker {

    private final Container container;
    private java.util.List<Component> containedComponents;

    public ContainerWalker(Container container) {
        this.container = container;
        rebuildIndex();
    }

    public AbstractButton findButtonWithIcon(String iconFile) {
        AbstractButton returnedButton = (AbstractButton) Iterables.find(containedComponents, new ButtonWithIconPredicate(iconFile));
        if (returnedButton == null) {
            throw new NoSuchElementException("No button found with icon file " + iconFile);
        }
        return returnedButton;
    }

    // Currently unused, but probably useful
    public <T> JComboBox findComboBoxWithValue(T value) {
        for (Component component : containedComponents) {
            if (component instanceof JComboBox) {
                JComboBox comboBox = (JComboBox) component;
                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    if (comboBox.getItemAt(i).equals(value)) {
                        return comboBox;
                    }
                }
            }
        }
        throw new NoSuchElementException("No combo box found with item " + value);
    }

    // Currently unused, but probably useful
    public AbstractButton findCheckBoxWithLabel(String labelText) {
        for (Component component : containedComponents) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (String.valueOf(checkBox.getText()).equals(labelText)) {
                    return checkBox;
                }
            }
        }
        throw new NoSuchElementException("No checkbox found with label " + labelText);
    }

    private java.util.List<Component> findAllComponentsIn(Container container) {
        java.util.List<Component> components = new ArrayList<Component>();
        for (Component component : container.getComponents()) {
            components.add(component);
            if (component instanceof Container) {
                components.addAll(findAllComponentsIn((Container) component));
            }
        }
        return components;
    }

    public AbstractButton findButtonWithName(String buttonName) {
        return (AbstractButton) Iterables.find(containedComponents,
                new ComponentClassAndNamePredicate(AbstractButton.class, buttonName));
    }

    public JLabel findLabelWithName(String labelName) {
        return (JLabel) Iterables.find(containedComponents,
                new ComponentClassAndNamePredicate(JLabel.class, labelName));
    }

    public JTextComponent findTextComponent(String componentName) {
        JTextComponent component = (JTextComponent) Iterables.find(containedComponents,
                new ComponentClassAndNamePredicate(JTextComponent.class, componentName));
        if (component == null) {
            throw new NoSuchElementException("No text component with name '" + componentName + "' found");
        }
        return component;
    }

    public void rebuildIndex() {
        containedComponents = findAllComponentsIn(container);
    }

    public <T extends Component> T findComponent(String name, Class<? extends T> componentClass) {
        return (T) Iterables.find(containedComponents, new ComponentClassAndNamePredicate(componentClass, name));
    }

    private class ComponentClassAndNamePredicate implements Predicate<Component> {

        private Class<? extends Component> componentClass;
        private String name;

        private ComponentClassAndNamePredicate(Class<? extends Component> componentClass, String name) {
            this.componentClass = componentClass;
            this.name = name;
        }

        @Override
        public boolean apply(@Nullable Component component) {
            return component != null && componentClass.isAssignableFrom(component.getClass()) &&
                    StringUtils.equals(name, component.getName());
        }

    }

    private class ButtonWithIconPredicate implements Predicate<Component> {

        private String iconFile;

        private ButtonWithIconPredicate(String iconFile) {
            this.iconFile = iconFile;
        }

        @Override
        public boolean apply(@Nullable Component component) {
            if (!(component instanceof AbstractButton)) {
                return false;
            }
            AbstractButton button = (AbstractButton) component;
            return String.valueOf(button.getIcon()).endsWith("/" + iconFile);
        }
    }

}
