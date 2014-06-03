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
package com.eviware.soapui.impl.actions;

import com.eviware.soapui.model.workspace.Workspace;

import java.util.List;

/**
 * Represents a way of creating a SoapUI project from discovered resources, e.g. by recording traffic or
 * scanning through a log file.
 */
public interface DiscoveryMethod extends Labeled {

    boolean isSynchronous();

    void discoverResources(Workspace workspace);

    // TODO: Change Object to DiscoveredResource when we've moved this to Pro
    List<Object> discoverResourcesSynchronously(Workspace workspace);
}
