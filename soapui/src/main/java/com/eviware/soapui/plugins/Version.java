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

package com.eviware.soapui.plugins;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a plugin version
 */
public class Version implements Comparable<Version> {
    private final int majorVersion;
    private final int minorVersion;
    private final String patchVersion;

    public Version(int majorVersion, int minorVersion, String patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
    }

    public static Version fromString(String versionString) {
        if (versionString == null) {
            return new Version(0, 0, null);
        }
        try {
            String[] parts = versionString.split("\\.");
            String patchVersion = parts.length == 3 ? parts[2] : null;
            return new Version(parts.length > 0 ? Integer.parseInt(parts[0]) : 0,
                    parts.length > 1 ? Integer.parseInt(parts[1]) : 0, patchVersion);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(versionString + " is not a valid version string");
        }
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    @Override
    public String toString() {
        return majorVersion + "." + minorVersion + (patchVersion == null ? "" : '.' + patchVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version that = (Version) o;
        return this.majorVersion == that.majorVersion && this.minorVersion == that.minorVersion &&
                StringUtils.equals(this.patchVersion, that.patchVersion);

    }

    @Override
    public int hashCode() {
        int result = majorVersion;
        result = 31 * result + minorVersion;
        result = 31 * result + (patchVersion == null ? 0 : patchVersion.hashCode());
        return result;
    }

    @Override
    public int compareTo(Version other) {
        if (majorVersion != other.majorVersion) {
            return majorVersion - other.majorVersion;
        }
        if (minorVersion != other.minorVersion) {
            return minorVersion - other.minorVersion;
        }
        return normalizePatchVersion(patchVersion).compareTo(normalizePatchVersion(other.patchVersion));
    }

    private String normalizePatchVersion(String patchVersion) {
        return patchVersion == null ? "0" : patchVersion;
    }
}
