/*
 * Copyright (C) 1995-2020 Die Software Peter Fitzon GmbH
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
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.File;

/**
 * Actions for importing an existing SoapUI project file into the current
 * workspace
 *
 * @author Jovan Vasic
 */

public class DswImportWsdlProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "DswImportWsdlProjectAction";
    public static final MessageSupport messages = MessageSupport.getMessages(DswImportWsdlProjectAction.class);

    public DswImportWsdlProjectAction() {
        super(messages.get("title"), messages.get("description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        File file = null;

        for (Project project : workspace.getProjectList()) {
        	if (project.getPath().equalsIgnoreCase((String) param)) {
        		UISupport.showInfoMessage("Project already open");
            	return;
            }
		}       
        
        if (param == null || param instanceof SoapUIActions) {
            file = UISupport.getFileDialogs().openXML(this, messages.get("prompt.title"));
        } else {
            file = new File(param.toString());
        }

        if (file == null) {
            return;
        }

        String fileName = file.getAbsolutePath();
        if (fileName == null) {
            return;
        }

        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
        try {
            WsdlProject project = (WsdlProject) workspace.importProject(fileName);
            if (project != null) {
                UISupport.select(project);
            }
            if (param != null && param instanceof SoapUIActions) {
                Analytics.trackAction((SoapUIActions) param);
            } else {
                Analytics.trackAction(SoapUIActions.IMPORT_PROJECT);
            }
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex);
        } finally {
            state.restore();
        }
    }
}
