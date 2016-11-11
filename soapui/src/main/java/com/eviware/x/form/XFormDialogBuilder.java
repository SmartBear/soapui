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

package com.eviware.x.form;

import com.eviware.soapui.support.action.swing.ActionList;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.ImageIcon;
import java.util.ArrayList;

public abstract class XFormDialogBuilder {
    private ArrayList<XForm> forms = new ArrayList<XForm>();

    public XFormDialogBuilder() {
    }

    protected void addForm(XForm form) {
        forms.add(form);
    }

    protected XForm[] getForms() {
        return forms.toArray(new XForm[forms.size()]);
    }

    public abstract XForm createForm(String name);

    public abstract XFormDialog buildDialog(ActionList actions, String description, ImageIcon icon);

    public abstract XFormDialog buildWizard(String description, ImageIcon icon, String helpURL);

    public abstract ActionList buildOkCancelActions();

    public abstract ActionList buildOkCancelHelpActions(String url);

    public abstract ActionList buildHelpActions(String url);

    public XForm createForm(String name, FormLayout layout) {
        return createForm(name);
    }
}
