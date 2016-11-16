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

package com.eviware.soapui.impl.wsdl.panels.operation;

import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.support.components.JPropertiesTable;

import javax.swing.JPanel;

/**
 * PanelBuilder for WsdlOperation. Only builds an overview panel.
 *
 * @author Ole.Matzura
 */

public class WsdlOperationPanelBuilder extends EmptyPanelBuilder<WsdlOperation> {

    public WsdlOperationPanelBuilder() {
    }

    public JPanel buildOverviewPanel(WsdlOperation operation) {
        JPropertiesTable<WsdlOperation> table = new JPropertiesTable<WsdlOperation>("Operation Properties");
        table.addProperty("Description", "description", true);
        table.addProperty("SOAPAction", "action");
        table.addProperty("Operation", "bindingOperationName");
        table.addProperty("Style", "style");
        table.addProperty("Type", "type");
        table.addProperty("Input", "inputName");
        table.addProperty("Output", "outputName");
        table.addProperty("Sends Attachments", "sendsAttachments");
        table.addProperty("Receives Attachments", "receivesAttachments");
        table.addProperty("WS-A anonymous", "anonymous", new Object[]{AnonymousTypeConfig.OPTIONAL.toString(),
                AnonymousTypeConfig.REQUIRED.toString(), AnonymousTypeConfig.PROHIBITED.toString()});
        table.setPropertyObject(operation);

        return table;
    }

    public boolean hasOverviewPanel() {
        return true;
    }
}
