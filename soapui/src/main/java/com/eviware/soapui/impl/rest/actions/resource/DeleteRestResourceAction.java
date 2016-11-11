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

package com.eviware.soapui.impl.rest.actions.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a RestResource from its containing Service or Resource
 *
 * @author Ole.Matzura
 */

public class DeleteRestResourceAction extends AbstractSoapUIAction<RestResource> {
    public DeleteRestResourceAction() {
        super("Delete", "Deletes this Resource");
    }

    public void perform(RestResource resource, Object param) {
        if (UISupport.confirm("Delete Resource [" + resource.getName() + "] from ["
                + resource.getResourceContainer().getName() + "]. This will also delete all the child resources and test steps!", "Delete Resource")) {
            resource.getResourceContainer().deleteResource(resource);
        }
    }
}
