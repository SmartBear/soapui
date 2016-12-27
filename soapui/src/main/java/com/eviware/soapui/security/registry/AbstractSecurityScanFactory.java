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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;

/**
 * Abstract factory behaviour for SecurityScan factories
 *
 * @author soapui team
 */

public abstract class AbstractSecurityScanFactory implements SecurityScanFactory {
    private final String type;
    private final String name;
    private final String description;
    protected final String pathToIcon;

    public AbstractSecurityScanFactory(String typeName, String name, String description, String pathToIcon) {
        this.type = typeName;
        this.name = name;
        this.description = description;
        this.pathToIcon = pathToIcon;
    }

    public abstract SecurityScanConfig createNewSecurityScan(String name);

    public abstract AbstractSecurityScan buildSecurityScan(TestStep testStep, SecurityScanConfig config,
                                                           ModelItem parent);

    public String getSecurityScanType() {
        return type;
    }

    /**
     * True for test step on which this scan could be aplied.
     *
     * @return
     */
    public abstract boolean canCreate(TestStep testStep);

    public String getSecurityScanName() {
        return name;
    }

    public String getSecurityScanDescription() {
        return description;
    }

    public String getSecurityScanIconPath() {
        return pathToIcon;
    }

}
