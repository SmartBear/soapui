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