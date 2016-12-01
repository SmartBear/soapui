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

package com.eviware.soapui.model;

import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.util.List;
import java.util.Map;

public interface TestPropertyHolder {
    public String[] getPropertyNames();

    public void setPropertyValue(String name, String value);

    public String getPropertyValue(String name);

    public TestProperty getProperty(String name);

    public Map<String, TestProperty> getProperties();

    public void addTestPropertyListener(TestPropertyListener listener);

    public void removeTestPropertyListener(TestPropertyListener listener);

    public boolean hasProperty(String name);

    public ModelItem getModelItem();

    public int getPropertyCount();

    public List<TestProperty> getPropertyList();

    public TestProperty getPropertyAt(int index);

    public String getPropertiesLabel();
}
