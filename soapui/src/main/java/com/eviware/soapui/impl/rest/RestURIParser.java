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

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.URIParser;

/**
 * RestURIParser should parse the URI based on ths standard syntax components referred
 * in {@link http://www.ietf.org/rfc/rfc3986.txt} as [scheme:][//authority][path][?query][#fragment] for HTTP/HTTPS scheme
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */
public interface RestURIParser extends URIParser {

    /**
     * This method returns the decoded endpoint of the URI.
     * Endpoint is composed of [HTTP/HTTPS] followed by hostname and port.
     *
     * @return decoded endpoint of the URI or empty space if there is no endpoint in the URI
     */
    public String getEndpoint();

    /**
     * This method returns the resource name.
     * Resource name is taken from the path, usually the last part of the path.
     *
     * @return decoded resourceName of the URI or empty space if there is no path in the URI
     */
    public String getResourceName();

}
