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
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.support.MessageSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Action for adding a new child REST resource.
 *
 * @author Ole.Matzura
 */

public class NewRestChildResourceAction extends NewRestResourceActionBase<RestResource> {
    public static final String SOAPUI_ACTION_ID = "NewRestChildResourceAction";
    public static final MessageSupport messages = MessageSupport.getMessages(NewRestChildResourceAction.class);

    public NewRestChildResourceAction() {
        super(messages.get("Title"), messages.get("Description"));
    }


    @Override
    protected List<RestResource> getResourcesFor(RestResource item) {
        List<RestResource> returnValue = new ArrayList<RestResource>();
        returnValue.add(item);
        returnValue.addAll(Arrays.asList(item.getAllChildResources()));
        return returnValue;
    }

    @Override
    protected RestResource addResourceTo(RestResource parentResource, String name, String path) {
        return parentResource.addNewChildResource(name, path);
    }


}
