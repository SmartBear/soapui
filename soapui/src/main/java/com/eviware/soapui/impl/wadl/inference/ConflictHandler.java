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

package com.eviware.soapui.impl.wadl.inference;

import javax.xml.namespace.QName;

/**
 * Handles schedule conflicts while inferring Xml schema from Xml documents. Has
 * a single callback method.
 *
 * @author Dain Nilsson
 */
public interface ConflictHandler {

    /**
     * Callback method for deciding whether given Xml document is valid or not,
     * and if so, to expand the schema. The function should return true if the
     * contents at the cursor is valid in respect to the message provided, false
     * if not.
     *
     * @param event   What type of event this is, creation or modification.
     * @param type    The type of particle that this is in regards to.
     * @param name    The QName for the particle that is being modified.
     * @param path    The path to the element that is being changed (or contains the
     *                attribute/has the type that is beng changed).
     * @param message A short message describing the change.
     * @return True to accept the schema modification and continue validation,
     *         false to trigger validation failure.
     */
    public boolean callback(Event event, Type type, QName name, String path, String message);

    public enum Type {
        ELEMENT, ATTRIBUTE, TYPE
    }

    public enum Event {
        CREATION, MODIFICATION
    }
}
