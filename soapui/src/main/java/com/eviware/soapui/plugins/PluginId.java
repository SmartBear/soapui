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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class PluginId {

    private final String groupId;
    private final String name;

    public PluginId(String groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    /**
     * Returns a String identifying the group to which this plugin belongs. Ideally the group ID should be
     * in the same format as that of a Maven group ID.
     *
     * @return a dot-separated String identifier for the group
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the name of the plugin.
     *
     * @return a non-null name, which should be unique when combined with the group ID.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return groupId + ":" + name;
    }
}
