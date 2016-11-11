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

package com.eviware.soapui.impl;


/**
 * URIParser should parse the URI based on ths standard syntax components referred
 * in {@link http://www.ietf.org/rfc/rfc3986.txt} as [scheme:][//authority][path][?query][#fragment]
 *
 * @author Shadid Chowdhury
 * @since 4.5.6
 */

public interface URIParser {

    /**
     * This method returns the scheme of the URI if there is one, otherwise empty space.
     *
     * @return scheme of a the URI
     */
    public String getScheme();

    /**
     * This method returns the decoded authority component of the URI.
     * Usually authority is composed of hostname and port.
     *
     * @return decoded authority of the URI or empty space if there is no authority in the URI
     */
    //public String getAuthority();

    /**
     * This method returns the decoded path of the URI.
     *
     * @return decoded path of the URI or empty space if there is no path in the URI
     */
    public String getResourcePath();

    /**
     * This method returns the decoded query of the URI.
     *
     * @return decoded query of the URI or empty space if there is no query in the URI
     */
    public String getQuery();

}
