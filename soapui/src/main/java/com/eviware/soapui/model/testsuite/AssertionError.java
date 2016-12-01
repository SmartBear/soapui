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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.support.editor.xml.support.ValidationError;
import org.apache.xmlbeans.XmlError;

/**
 * Holder for an assertion error
 *
 * @author Ole.Matzura
 */

public class AssertionError implements ValidationError {
    private String message;
    private XmlError xmlError;

    public AssertionError(String message) {
        this.message = message;
    }

    public AssertionError(XmlError xmlError) {
        this.xmlError = xmlError;
        this.message = xmlError.getMessage();
    }

    public String getMessage() {
        return message;
    }

    public int getLineNumber() {
        return xmlError == null ? -1 : xmlError.getLine();
    }

    public XmlError getXmlError() {
        return xmlError;
    }

    public String toString() {
        if (xmlError == null) {
            return message;
        }

        return "line " + getLineNumber() + ": " + message;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        String msg = toString();
        result = PRIME * result + ((msg == null) ? 0 : msg.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AssertionError other = (AssertionError) obj;

        return other.toString().equals(toString());
    }
}
