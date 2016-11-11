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

package com.eviware.soapui.support.types;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap&lt;String,String&gt;
 *
 * @author Ole.Matzura
 */

public class StringToObjectMap extends HashMap<String, Object> {
    public StringToObjectMap() {
        super();
    }

    public StringToObjectMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringToObjectMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringToObjectMap(Map<? extends String, ? extends Object> m) {
        super(m);
    }

}
