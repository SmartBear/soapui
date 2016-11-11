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

package com.eviware.soapui.impl.wadl.inference.schema;

/**
 * Static class containing package global variables.
 *
 * @author Dain Nilsson
 */
public class Settings {

    /**
     * Locality used for iLOCAL algorithm when inferring complex types.
     */
    public static final int locality = 2;

    /**
     * The namespace for XML Schema.
     */
    public static final String xsdns = "http://www.w3.org/2001/XMLSchema";

    /**
     * The namespace for XML Schema-instance.
     */
    public static final String xsins = "http://www.w3.org/2001/XMLSchema-instance";
}
