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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

public class ExportMockOperation extends AbstractSoapUIAction<WsdlMockOperation> {
    public ExportMockOperation() {
        super("Export", "Exports the mock operation");
    }

    public void perform(WsdlMockOperation mOperation, Object param) {
        mOperation.beforeSave();
        String defaultFileName = System.getProperty("user.home", ".") + File.separator + mOperation.getName() + ".xml";
        File file = UISupport.getFileDialogs().saveAs(this, "Select test case file", "xml", "XML",
                new File(defaultFileName));

        if (file == null) {
            return;
        }

        String fileName = file.getAbsolutePath();
        if (fileName == null) {
            return;
        }

        mOperation.exportMockOperation(file);
    }
}
