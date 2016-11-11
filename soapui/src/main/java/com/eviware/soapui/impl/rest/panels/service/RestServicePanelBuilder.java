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

package com.eviware.soapui.impl.rest.panels.service;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.components.JPropertiesTable;

import java.awt.Component;

/**
 * PanelBuilder for WsdlInterface
 *
 * @author Ole.Matzura
 */

public class RestServicePanelBuilder extends EmptyPanelBuilder<RestService> {
    public RestServicePanelBuilder() {
    }

    public RestServiceDesktopPanel buildDesktopPanel(RestService service) {
        return new RestServiceDesktopPanel(service);
    }

    public boolean hasDesktopPanel() {
        return true;
    }

    public Component buildOverviewPanel(RestService service) {
        JPropertiesTable<RestService> table = new JPropertiesTable<RestService>("Service Properties");
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Base Path", "basePath", true);
        table.addProperty("WADL", "wadlUrl", !service.isGenerated());
        table.addProperty("Generated", "generated", false);

        table.setPropertyObject(service);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
