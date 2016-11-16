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

public class AssertionListEntry implements Comparable<AssertionListEntry> {
    private String typeId;
    private String name;
    private String description;

    public AssertionListEntry(String typeId, String name, String description) {
        this.typeId = typeId;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " " + description;
    }

    @Override
    public int hashCode() {
        return name.length() + description.length();
    }

    @Override
    public int compareTo(AssertionListEntry o) {
        return name.compareToIgnoreCase(o.getName());
    }

    public String getTypeId() {
        return typeId;
    }

}
