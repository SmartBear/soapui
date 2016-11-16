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

package com.eviware.soapui.analytics;

/**
 *
 */
public enum SoapUIFeatures {
    SERVICE_VIRTUALIZATION("ServiceVirtualization"),
    FUNCTIONAL_TESTING("FunctionalTesting"),
    PERFORMANCE_TESTING("PerformanceTesting"),
    SECURITY_TESTING("SecurityTesting"),
    SOAP("SOAP"),
    REST("REST"),
    PLUGINS("Plugins"),
    DISCOVERY("Discovery"),
    INSTALL("Install"),
    MONITORING("Monitoring"),
    AUTOMATE_SOAP_UI("AutomateSoapUI"),
    USE_SOAP_UI("UseSoapUI"),
    REPORTS("Reports"),
    TOOL("Tool"),
    LICENSE("License");

    private String featureName;

    SoapUIFeatures(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
