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

package com.eviware.soapui.model.support;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility methods for soapui model-related interfaces
 *
 * @author Ole.Matzura
 */

public class ModelSupport {
    public static <T extends ModelItem> String[] getNames(List<T> list) {
        String[] names = new String[list.size()];
        for (int c = 0; c < names.length; c++) {
            names[c] = list.get(c).getName();
        }

        return names;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ModelItem> List<T> getChildren(ModelItem modelItem, Class<T> childType) {
        List<T> result = new ArrayList<T>();
        for (ModelItem child : modelItem.getChildren()) {
            if (child.getClass().equals(childType)) {
                result.add((T) child);
            }
        }

        return result;
    }

    public static <T extends ModelItem> String[] getNames(List<T> list, ModelItemFilter<T> filter) {
        String[] names = new String[list.size()];
        for (int c = 0; c < names.length; c++) {
            if (filter == null || filter.accept(list.get(c))) {
                names[c] = list.get(c).getName();
            }
        }

        return names;
    }

    public static String[] getNames(String[] firstItems, List<? extends ModelItem> list) {
        String[] names = new String[list.size() + firstItems.length];
        for (int c = 0; c < firstItems.length; c++) {
            names[c] = firstItems[c];
        }

        for (int c = 0; c < list.size(); c++) {
            names[c + firstItems.length] = list.get(c).getName();
        }

        return names;
    }

    public static String[] getNames(List<? extends ModelItem> list, String[] lastItems) {
        String[] names = new String[list.size() + lastItems.length];
        for (int c = 0; c < lastItems.length; c++) {
            names[c + list.size()] = lastItems[c];
        }

        for (int c = 0; c < list.size(); c++) {
            names[c] = list.get(c).getName();
        }

        return names;
    }

    public static String generateModelItemID() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ModelItem> T findModelItemById(String id, ModelItem root) {
        if (root == null || id == null) {
            return null;
        }

        for (ModelItem child : root.getChildren()) {
            if (child.getId().equals(id)) {
                return (T) child;
            }

            ModelItem result = findModelItemById(id, child);
            if (result != null) {
                return (T) result;
            }
        }

        return null;
    }

    public static String promptForUniqueName(String typeName, ModelItem parent, String def) {
        String name = UISupport.prompt("Specify name for new " + typeName, "New " + typeName, def);
        StringList names = new StringList();
        for (ModelItem item : parent.getChildren()) {
            names.add(item.getName());
        }

        while (name != null && names.contains(name)) {
            name = UISupport.prompt("Specify unique name for new " + typeName, "New " + typeName, def);
        }

        return name;
    }

    public static Project getModelItemProject(ModelItem modelItem) {
        if (modelItem == null) {
            return null;
        }

        while (!(modelItem instanceof Project) && modelItem != null) {
            modelItem = modelItem.getParent();
        }

        return (Project) modelItem;
    }

    public static TestCase getModelItemTestCase(ModelItem modelItem) {
        if (modelItem == null) {
            return null;
        }

        while (!(modelItem instanceof TestCase) && modelItem != null) {
            modelItem = modelItem.getParent();
        }

        return (TestCase) modelItem;
    }

    public static String getResourceRoot(AbstractWsdlModelItem<?> testStep) {
        WsdlProject project = (WsdlProject) getModelItemProject(testStep);
        if (project == null) {
            return null;
        }

        return PropertyExpander.expandProperties(project, project.getResourceRoot());
    }

    public static void createNewIds(AbstractWsdlModelItem<?> modelItem) {
        if (modelItem.getConfig().isSetId()) {
            modelItem.getConfig().setId(generateModelItemID());
        }

        for (ModelItem child : modelItem.getChildren()) {
            if (child instanceof AbstractWsdlModelItem<?>) {
                createNewIds((AbstractWsdlModelItem<?>) child);
            }
        }
    }

    public static void unsetIds(AbstractWsdlModelItem<?>[] modelItems) {
        for (AbstractWsdlModelItem<?> modelItem : modelItems) {
            createNewIds(modelItem);
        }
    }

    public interface ModelItemFilter<T extends ModelItem> {
        public boolean accept(T modelItem);
    }

    public static boolean dependsOn(ModelItem source, ModelItem target) {
        if (source == target) {
            return true;
        }

        ModelItem p = source.getParent();
        while (p != null) {
            if (p == target) {
                return true;
            }

            p = p.getParent();
        }

        return false;
    }

    public static class InterfaceTypeFilter implements ModelItemFilter<Interface> {
        private String type;

        public InterfaceTypeFilter(String type) {
            this.type = type;
        }

        public boolean accept(Interface modelItem) {
            return modelItem.getInterfaceType().equals(type);
        }

    }

    public static boolean isOneOf(ModelItem modelItem, Object... classes) {
        for (Object clazz : classes) {
            if (clazz instanceof Class && ((Class) clazz).isAssignableFrom(modelItem.getClass())) {
                return true;
            } else if (clazz instanceof ModelItem &&
                    clazz.getClass().isAssignableFrom(modelItem.getClass())) {
                return true;
            }
        }

        return false;
    }
}
