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

package com.eviware.soapui.support.xml;

import org.apache.xmlbeans.XmlObject;

/**
 * Marker interface for objects that can be saved/restored to/from an XmlObject
 *
 * @author Ole.Matzura
 */

public interface XmlObjectPersistable {
    /**
     * Persisits this object to an XmlObject
     *
     * @return the persisted XmlObject
     */

    public XmlObject save();

    /**
     * Restores this object from the specified XmlObject
     *
     * @param xmlObject the xmlObject to restore from
     */

    public void restore(XmlObject xmlObject);
}
