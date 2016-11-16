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

package com.eviware.soapui.impl.wadl.inference.support;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;

import javax.xml.namespace.QName;

/**
 * ConflictHandler that allows any changes that need to be made to the schema in
 * order to validate against given XML document.
 *
 * @author Dain Nilsson
 */
public class AllowAll implements ConflictHandler {
    /**
     * Constructs a new AllowAll instance.
     */
    public AllowAll() {

    }

    public boolean callback(Event event, Type type, QName name, String path, String message) {
        StringBuilder s = new StringBuilder(message).append("\n");
        if (event == Event.CREATION) {
            s.append("Create ");
        } else if (event == Event.MODIFICATION) {
            s.append("Modify ");
        }
        if (type == Type.ELEMENT) {
            s.append("element '");
        } else if (type == Type.ATTRIBUTE) {
            s.append("attribute '");
        } else if (type == Type.TYPE) {
            s.append("type '");
        }
        s.append(name.getLocalPart()).append("' in namespace '").append(name.getNamespaceURI()).append("'");
        s.append(" at path ").append(path).append("?");
        System.out.println(s.toString());
        return true;
    }

}
