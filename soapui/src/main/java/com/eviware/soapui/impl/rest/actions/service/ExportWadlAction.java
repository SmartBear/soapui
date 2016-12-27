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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.support.definition.export.WadlDefinitionExporter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Exports the definition (wsdls and xsds) of a WsdlInterface to the file system
 *
 * @author Ole.Matzura
 */

@SuppressWarnings("unchecked")
public class ExportWadlAction extends AbstractSoapUIAction<RestService> {
    public static final String SOAPUI_ACTION_ID = "ExportWadlAction";

    public ExportWadlAction() {
        super("Export WADL", "Exports the entire WADL and included/imported files to a local directory");
    }

    public void perform(RestService iface, Object param) {
        try {
            String path = exportDefinition(null, iface);
            if (path != null) {
                UISupport.showInfoMessage("WADL exported succesfully to [" + path + "]", "Export WADL");
            }
        } catch (Exception e1) {
            UISupport.showErrorMessage(e1);
        }
    }

    public String exportDefinition(String location, RestService iface) throws Exception {
        if (!iface.isGenerated()) {
            boolean exportChanges = UISupport.confirm("Do you want the exported WADL to contain recent changes?", "WADL export option");
            iface.setExportChanges(exportChanges);
        }

        File folderName = location == null ? UISupport.getFileDialogs().openDirectory(this, "Select output directory",
                null) : new File(location);

        if (folderName == null) {
            return null;
        }

        WadlDefinitionExporter exporter = new WadlDefinitionExporter(iface);
        return exporter.export(folderName.getAbsolutePath());
    }
}
