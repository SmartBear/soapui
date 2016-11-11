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

package com.eviware.soapui.model.propertyexpansion;

import com.eviware.soapui.model.testsuite.TestProperty;
import org.apache.commons.beanutils.PropertyUtils;

public class MutablePropertyExpansionImpl extends PropertyExpansionImpl implements MutablePropertyExpansion {
    private final Object container;
    private final String propertyName;
    private String stringRep;

    public MutablePropertyExpansionImpl(TestProperty tp, String xpath, Object container, String propertyName) {
        super(tp, xpath);
        this.container = container;
        this.propertyName = propertyName;

        stringRep = toString();
    }

    public void setProperty(TestProperty property) {
        super.setProperty(property);
    }

    public void setXPath(String xpath) {
        super.setXPath(xpath);
    }

    public void update() throws Exception {
        String rep = toString();

        // not changed
        if (stringRep.equals(rep)) {
            return;
        }

        Object obj = PropertyUtils.getProperty(container, propertyName);
        if (obj == null) {
            throw new Exception("property value is null");
        }

        String str = obj.toString();
        int ix = str.indexOf(stringRep);
        if (ix == -1) {
            throw new Exception("property expansion [" + stringRep + "] not found for update");
        }

        while (ix != -1) {
            str = str.substring(0, ix) + rep + str.substring(ix + stringRep.length());
            ix = str.indexOf(stringRep, ix + rep.length());
        }

        PropertyUtils.setProperty(container, propertyName, str);

        stringRep = rep;
    }
}
