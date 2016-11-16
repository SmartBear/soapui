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

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Actions for importing an existing remote SoapUI project file into the current
 * workspace
 *
 * @author Ole.Matzura
 */

public class ImportRemoteWsdlProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "ImportRemoteWsdlProjectAction";
    public static final MessageSupport messages = MessageSupport.getMessages(ImportRemoteWsdlProjectAction.class);

    public ImportRemoteWsdlProjectAction() {
        super(messages.get("title"), messages.get("description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
        try {
            String url = UISupport.prompt(messages.get("prompt.text"), messages.get("prompt.title"), "");

            if (url != null) {
                WsdlProject project = (WsdlProject) workspace.importRemoteProject(url);
                if (project != null) {
                    UISupport.select(project);
                }
            }
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex);
        } finally {
            state.restore();
        }
    }
}
