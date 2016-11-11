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

package com.eviware.soapui.security.boundary.enumeration;

import com.eviware.soapui.security.boundary.BoundaryUtils;
import com.eviware.soapui.security.boundary.StringBoundary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnumerationValues {
    private String type;
    private List<String> valuesList = new ArrayList<String>();

    public EnumerationValues(String type) {
        this.type = type;
    }

    public static int maxLengthStringSize(Collection<String> values) {
        int max = 0;
        for (String str : values) {
            if (max < str.length()) {
                max = str.length();
            }
        }
        return max;
    }

    public static String createOutOfBoundaryValue(EnumerationValues enumValues, int size) {
        if ("XmlString".equals(enumValues.getType())) {
            String value = null;
            do {
                value = BoundaryUtils.createCharacterArray(StringBoundary.AVAILABLE_VALUES, size);
            }
            while (enumValues.getValuesList().contains(value));
            return value;
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void addValue(String value) {
        valuesList.add(value);
    }

    public List<String> getValuesList() {
        return valuesList;
    }

}
