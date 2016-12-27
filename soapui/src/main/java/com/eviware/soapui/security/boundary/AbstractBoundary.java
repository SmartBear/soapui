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

package com.eviware.soapui.security.boundary;

/**
 * @author nebojsa.tasic
 */
public abstract class AbstractBoundary implements Boundary {

    public static final String XSD_LENGTH = "xsd:length";
    public static final String XSD_MAX_LENGTH = "xsd:maxLength";
    public static final String XSD_MIN_LENGTH = "xsd:minLength";
    public static final String XSD_MIN_INCLUSIVE = "xsd:minInclusive";
    public static final String XSD_MAX_INCLUSIVE = "xsd:maxInclusive";
    public static final String XSD_MIN_EXCLUSIVE = "xsd:minExclusive";
    public static final String XSD_MAX_EXCLUSIVE = "xsd:maxExclusive";
    public static final String XSD_TOTAL_DIGITS = "xsd:totalDigits";
    public static final String XSD_FRACTION_DIGITS = "xsd:fractionDigits";

    // TODO: cover more simple types
    public static Boundary factory(String type) {
        if (type == null) {
            return null;
        }

        if (type.endsWith(":string")) {
            return new StringBoundary();
        }
        if (type.endsWith(":normalizedString")) {
            return new NormalisedStringBoundary();
        }
        if (type.endsWith(":integer")) {
            return new IntegerBoundary();
        }
        if (type.endsWith(":decimal")) {
            return new DecimalBoundary();
        }
        if (type.endsWith(":dateTime")) {
            return new DateTimeBoundary();
        }
        if (type.endsWith(":date")) {
            return new DateBoundary();
        }
        if (type.endsWith(":time")) {
            return new DateBoundary();
        }

        return null;
    }

    public static String outOfBoundaryValue(String baseType, String nodeName, String nodeValue) {
        Boundary boundary = AbstractBoundary.factory(baseType);

        if (XSD_MIN_LENGTH.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MIN_LENGTH, nodeValue);
        } else if (XSD_MAX_LENGTH.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MAX_LENGTH, nodeValue);
        } else if (XSD_LENGTH.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.LENGTH, nodeValue);
        } else if (XSD_MAX_EXCLUSIVE.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MAX_EXCLISIVE, nodeValue);
        } else if (XSD_MAX_INCLUSIVE.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MAX_INCLISIVE, nodeValue);
        } else if (XSD_MIN_EXCLUSIVE.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MIN_EXCLISIVE, nodeValue);
        } else if (XSD_MIN_INCLUSIVE.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.MIN_INCLISIVE, nodeValue);
        } else if (XSD_TOTAL_DIGITS.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.TOTAL_DIGITS, nodeValue);
        } else if (XSD_FRACTION_DIGITS.equals(nodeName)) {
            return boundary.outOfBoundary(Boundary.FRACTION_DIGITS, nodeValue);
        }
        return null;
    }
}
