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

package com.eviware.x.impl.swing;

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class JWizardDialogBuilder extends SwingXFormDialogBuilder {
    private SwingXFormDialog dialog;

    public JWizardDialogBuilder(String name) {
        super(name);
    }

    protected final class NextAction extends AbstractAction {
        public NextAction() {
            super("Next");
        }

        public void actionPerformed(ActionEvent e) {
            if (dialog != null) {
                // dialog.setReturnValue( XFormDialog.NEXT_OPTION );
                dialog.setVisible(false);
            }
        }
    }

    public ActionList buildprevNextCancelActions() {
        DefaultActionList actions = new DefaultActionList("Actions");
        actions.addAction(new NextAction());
        actions.addAction(new CancelAction());
        return actions;
    }
}
