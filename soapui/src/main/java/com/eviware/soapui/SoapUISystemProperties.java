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

package com.eviware.soapui;

/**
 * @author Erik R. Yverling
 *         <p/>
 *         This is a container for all system properties used in SoapUI core.
 */
public final class SoapUISystemProperties {
    public static final String TEST_ON_DEMAND_HOST = "soapui.testondemand.host";
    public static final String TEST_ON_DEMAND_FIRST_PAGE_URL = "soapui.testondemand.firstpage.url";
    public static final String TEST_ON_DEMAND_GET_LOCATIONS_URL = "soapui.testondemand.getlocations.url";
    public static final String TEST_ON_DEMAND_PROTOCOL = "soapui.testondemand.protocol";

    public static final String SOAPUI_SSL_KEYSTORE_LOCATION = "soapui.ssl.keystore.location";
    public static final String SOAPUI_SSL_KEYSTORE_PASSWORD = "soapui.ssl.keystore.password";

    public static final String VERSION = "soapui.version";

    public static final String SOAPUI_LOG4j_CONFIG_FILE = "soapui.log4j.config";

    private SoapUISystemProperties() {
        throw new AssertionError();
    }
}
