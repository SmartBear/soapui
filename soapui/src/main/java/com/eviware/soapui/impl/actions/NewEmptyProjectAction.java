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

package com.eviware.soapui.impl.actions;


import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

public class NewEmptyProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "NewEmptyProjectAction";
    public static final MessageSupport messages = MessageSupport.getMessages(NewEmptyProjectAction.class);

    public NewEmptyProjectAction() {
        super(messages.get("Title"),messages.get("Description"));
    }

    @Override
    public void perform(WorkspaceImpl target, Object param) {
        try {
            WsdlProject project = target.createProject(ModelItemNamer.createName("Project", target.getProjectList()), null);
            UISupport.selectAndShow(project);
        } catch (SoapUIException e) {
            UISupport.showErrorMessage(e);
        }
    }
}
