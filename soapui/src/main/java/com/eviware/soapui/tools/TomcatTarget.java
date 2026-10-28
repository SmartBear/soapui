/*
 * SoapUI, Copyright (C) 2004-2026 SmartBear Software
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

package com.eviware.soapui.tools;

public enum TomcatTarget {
    TOMCAT_10_PLUS("Tomcat 10+"),
    TOMCAT_9_LEGACY("Tomcat 9 or older (legacy)");

    public static final String LEGACY_DEPRECATION_NOTE =
            "Tomcat 9 support ends on 31 March 2027. The legacy option will be deprecated at that time.";

    private final String label;

    TomcatTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TomcatTarget fromLabel(String label) {
        for (TomcatTarget target : values()) {
            if (target.label.equals(label)) {
                return target;
            }
        }
        return TOMCAT_10_PLUS;
    }

    public static TomcatTarget fromVersion(String version) {
        return version != null && "9".equals(version.trim()) ? TOMCAT_9_LEGACY : TOMCAT_10_PLUS;
    }
}
