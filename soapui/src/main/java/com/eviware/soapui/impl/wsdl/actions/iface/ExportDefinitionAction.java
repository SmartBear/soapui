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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.impl.support.definition.export.WsdlDefinitionExporter;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Exports the definition (wsdls and xsds) of a WsdlInterface to the file system
 *
 * @author Ole.Matzura
 */

@SuppressWarnings("unchecked")
public class ExportDefinitionAction extends AbstractSoapUIAction<WsdlInterface> {
    public static final String SOAPUI_ACTION_ID = "ExportDefinitionAction";

    public ExportDefinitionAction() {
        super("Export Definition", "Exports the entire WSDL and included/imported files to a local directory");
    }

    public void perform(WsdlInterface iface, Object param) {
        try {
            if (exportDefinition(null, iface) != null) {
                UISupport.showInfoMessage("Definition exported successfully");
            }
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        }
    }

    public String exportDefinition(String location, WsdlInterface iface) throws Exception {
        File folderName = location == null ? UISupport.getFileDialogs().openDirectory(this, "Select output directory",
                null) : new File(location);

        if (folderName == null) {
            return null;
        }

        WsdlDefinitionExporter exporter = new WsdlDefinitionExporter(iface);
        return exporter.export(folderName.getAbsolutePath());
    }
}
