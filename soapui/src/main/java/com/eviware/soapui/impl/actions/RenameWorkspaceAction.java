/*
 * Copyright 2004-2014 SmartBear Software
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
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlProject
 *
 * @author Ole.Matzura
 */

public class RenameWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public static final String SOAPUI_ACTION_ID = "RenameWorkspaceAction";
    public static final MessageSupport messages = MessageSupport.getMessages(RenameWorkspaceAction.class);

    public RenameWorkspaceAction() {
        super(messages.get("Title"), messages.get("Description"));
    }

    public void perform(WorkspaceImpl workspace, Object param) {
        String name = UISupport.prompt(messages.get("Prompt"), messages.get("Prompt.Title"), workspace.getName());
        if (name == null || name.equals(workspace.getName())) {
            return;
        }

        workspace.setName(name);
    }
}
