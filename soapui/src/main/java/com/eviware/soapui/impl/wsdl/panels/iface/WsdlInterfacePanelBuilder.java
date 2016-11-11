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

package com.eviware.soapui.impl.wsdl.panels.iface;

import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.Component;

/**
 * PanelBuilder for WsdlInterface
 *
 * @author Ole.Matzura
 */

public class WsdlInterfacePanelBuilder extends EmptyPanelBuilder<WsdlInterface> {
    public WsdlInterfacePanelBuilder() {
    }

    public Component buildOverviewPanel(WsdlInterface iface) {
        JPropertiesTable<WsdlInterface> table = new JPropertiesTable<WsdlInterface>("Interface Properties");
        table.addProperty("Name", "name", true);
        table.addProperty("Description", "description", true);
        table.addProperty("Definition URL", "definition", true);
        table.addProperty("Binding", "bindingName");
        table.addProperty("SOAP Version", "soapVersion", new Object[]{SoapVersion.Soap11, SoapVersion.Soap12});
        table.addProperty("Cached", "cached", false);
        table.addProperty("Style", "style", false);
        // TODO extract info from wsdl if by default ws addresing is implemented
        table.addProperty("WS-A version", "wsaVersion", new Object[]{WsaVersionTypeConfig.NONE.toString(),
                WsaVersionTypeConfig.X_200408.toString(), WsaVersionTypeConfig.X_200508.toString()});
        table.addProperty("WS-A anonymous", "anonymous", new Object[]{AnonymousTypeConfig.OPTIONAL.toString(),
                AnonymousTypeConfig.REQUIRED.toString(), AnonymousTypeConfig.PROHIBITED.toString()});

        table.setPropertyObject(iface);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }

    public DesktopPanel buildDesktopPanel(WsdlInterface iface) {
        return new WsdlInterfaceDesktopPanel(iface);
    }

    public boolean hasDesktopPanel() {
        return true;
    }
}
