/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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
}
