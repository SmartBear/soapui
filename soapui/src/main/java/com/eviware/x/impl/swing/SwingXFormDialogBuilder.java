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

import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

public class SwingXFormDialogBuilder extends XFormDialogBuilder {
    private String name;
    private SwingXFormDialog dialog;

    public SwingXFormDialogBuilder(String name) {
        this.name = name;
    }

    @Override
    public XForm createForm(String name) {
        XForm form = new SwingXFormImpl(name);
        ((SwingXFormImpl) form).addSpace(5);
        addForm(form);
        return form;
    }

    @Override
    public XForm createForm(String name, FormLayout layout) {
        XForm form = new SwingXFormImpl(name, layout);
        ((SwingXFormImpl) form).addSpace(5);
        addForm(form);
        return form;
    }

    @Override
    public XFormDialog buildDialog(ActionList actions, String description, ImageIcon icon) {
        XForm[] forms = getForms();
        dialog = forms.length > 1 ? new JTabbedFormDialog(name, forms, actions, description, icon) : new JFormDialog(
                name, (SwingXFormImpl) forms[0], actions, description, icon);

        return dialog;
    }

    @Override
    public XFormDialog buildWizard(String description, ImageIcon icon, String helpURL) {
        Action helpAction = (helpURL.length() > 0 ? new HelpAction(helpURL) : null);
        XForm[] forms = getForms();
        dialog = new JWizardDialog(name, forms, helpAction, description, icon);

        return dialog;
    }

    @Override
    public ActionList buildOkCancelActions() {
        DefaultActionList actions = new DefaultActionList("Actions");
        actions.addAction(new OKAction());
        actions.addAction(new CancelAction());
        return actions;
    }

    @Override
    public ActionList buildOkCancelHelpActions(String url) {
        DefaultActionList actions = new DefaultActionList("Actions");
        actions.addAction(new HelpAction(url));
        OKAction okAction = new OKAction();
        actions.addAction(okAction);
        actions.addAction(new CancelAction());
        actions.setDefaultAction(okAction);
        return actions;
    }

    @Override
    public ActionList buildHelpActions(String url) {
        DefaultActionList actions = new DefaultActionList("Actions");
        actions.addAction(new HelpAction(url));
        return actions;
    }

    protected final class OKAction extends AbstractAction {
        public OKAction() {
            super("OK");
        }

        public void actionPerformed(ActionEvent e) {
            if (dialog != null && dialog.validate()) {
                dialog.setReturnValue(XFormDialog.OK_OPTION);
                dialog.setVisible(false);
            }
        }
    }

    protected final class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent e) {
            if (dialog != null) {
                dialog.setReturnValue(XFormDialog.CANCEL_OPTION);
                dialog.setVisible(false);
            }
        }
    }

    public final class HelpAction extends AbstractAction implements HelpActionMarker {
        private String url;

        public HelpAction(String url) {
            this("Online Help", url, UISupport.getKeyStroke("F1"));
        }

        public HelpAction(String title, String url) {
            this(title, url, null);
        }

        public HelpAction(String title, String url, KeyStroke accelerator) {
            super(title);
            this.url = url;
            putValue(Action.SHORT_DESCRIPTION, "Show online help");
            if (accelerator != null) {
                putValue(Action.ACCELERATOR_KEY, accelerator);
            }

            putValue(Action.SMALL_ICON, UISupport.HELP_ICON);
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void actionPerformed(ActionEvent e) {
            Integer mods = e.getModifiers();
            String helpUrl = Tools.modifyUrl (url, mods);
            Tools.openURL(helpUrl);
        }
    }
}
