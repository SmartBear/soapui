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

package com.eviware.soapui.autoupdate;

import java.util.Comparator;

/**
 * Created by avdeev on 27.08.2014.
 */
public class SoapUIVersionInfo implements Comparator<SoapUIVersionInfo> {
    private int majorVersion;
    private int minorVersion;
    private int middleVersion;
    private String asString;

    public SoapUIVersionInfo(int majorVersion, int middleVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.middleVersion = middleVersion;
        this.minorVersion = minorVersion;
        asString = String.format("%d.%d.%d", majorVersion, minorVersion, middleVersion);
    }

    public SoapUIVersionInfo(String version) {
            /*
            If we can't parse some parts of version then this part will be equal to 0 and so all the other
            checkings will say that there is no version to update.
            * */
        this.asString = version;
        String[] versionParts = version.split("\\.");
        try {
            majorVersion = Integer.parseInt(versionParts[0]);
        } catch (NumberFormatException ex) {
            majorVersion = 0;
        }
        try {
            middleVersion = Integer.parseInt(versionParts[1]);
        } catch (NumberFormatException ex) {
            middleVersion = 0;
        }
        try {
            minorVersion = Integer.parseInt(versionParts[2]);
        } catch (NumberFormatException ex) {
            minorVersion = 0;
        }
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public int getMiddleVersion() {
        return this.middleVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    @Override
    public int compare(SoapUIVersionInfo o1, SoapUIVersionInfo o2) {
        if (o1.getMajorVersion() < o2.getMajorVersion()) {
            return -1;
        } else if (o1.getMajorVersion() > o2.getMajorVersion()) {
            return 1;
        } else {
            if (o1.getMiddleVersion() < o2.getMiddleVersion()) {
                return -1;
            } else if (o1.getMiddleVersion() > o2.getMiddleVersion()) {
                return 1;
            } else {
                if (o1.getMinorVersion() < o2.getMinorVersion()) {
                    return -1;
                } else if (o1.getMinorVersion() > o2.getMinorVersion()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        SoapUIVersionInfo ver = (SoapUIVersionInfo) obj;
        if (ver == null) {
            return false;
        }

        if (getMajorVersion() == ver.getMajorVersion() &&
                getMiddleVersion() == ver.getMiddleVersion() &&
                getMinorVersion() == ver.getMinorVersion()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return asString;
    }
}
