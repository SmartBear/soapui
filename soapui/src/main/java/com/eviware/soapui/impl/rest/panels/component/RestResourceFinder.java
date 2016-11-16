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

package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.support.StringUtils;

/**
 * @author Anders Jaensson
 */
class RestResourceFinder {
    private RestResource resource;

    RestResourceFinder(RestResource resource) {
        this.resource = resource;
    }

    /**
     * Translates a caret position in the full path text field into a resource.
     *
     * @param caretPosition the position at which the user has clicked in the text field
     * @return the RestResource object corresponding to the path element clicked, or <code>null</code> if the base path of the service was clicked
     */
    RestResource findResourceAt(int caretPosition) {
        String basePath = resource.getInterface().getBasePath();
        if (!StringUtils.isNullOrEmpty(basePath) && caretPosition <= basePath.length()) {
            return null;
        }
        for (RestResource r : RestUtils.extractAncestorsParentFirst(resource)) {
            if (caretPosition <= r.getFullPath().length()) {
                return r;
            }
        }
        return resource;
    }
}
