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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.action.SoapUIAction;

public class WsdlImportMethodFactory implements ImportMethodFactory {
    @Override
    public ImportMethod createNewImportMethod() {
        return new WsdlImportMethod();
    }

    public static class WsdlImportMethod implements ImportMethod {

        @Override
        public SoapUIAction<WorkspaceImpl> getImportAction() {
            return new NewWsdlProjectAction();
        }

        @Override
        public String getLabel() {
            return "WSDL definition (SOAP)";
        }
    }
}
