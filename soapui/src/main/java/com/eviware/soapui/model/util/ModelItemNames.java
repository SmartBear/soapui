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

package com.eviware.soapui.model.util;

import com.eviware.soapui.model.ModelItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for handling model item names.
 *
 * @author Lars Höidahl
 */

public class ModelItemNames<T extends ModelItem> {
    private List<T> elements;

    public ModelItemNames(List<T> elements) {
        this.elements = new ArrayList<T>(elements);
    }

    public ModelItemNames(T[] elements) {
        // Create an ArrayList to make sure that elements is modifyable.
        this.elements = new ArrayList<T>(Arrays.asList(elements));
    }

    public String[] getNames() {
        ArrayList<String> list = getElementNameList();
        return list.toArray(new String[list.size()]);
    }

    private ArrayList<String> getElementNameList() {
        ArrayList<String> elementNames = new ArrayList<String>();
        for (T element : elements) {
            elementNames.add(element.getName());
        }
        return elementNames;
    }

    public T getElement(String name) {
        int index = getElementNameList().indexOf(name);
        return elements.get(index);
    }

    public void addElement(T element) {
        elements.add(element);
    }

    public int getSize() {
        return elements.size();
    }

    public String getNameAt(int i) {
        return elements.get(i).getName();
    }
}
