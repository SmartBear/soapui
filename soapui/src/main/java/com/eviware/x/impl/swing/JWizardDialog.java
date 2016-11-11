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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.UpdateableAction;
import com.eviware.soapui.support.DescriptionPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.JButtonBar;
import com.eviware.soapui.support.swing.ModalFrameUtil;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.WizardPage;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JWizardDialog extends SwingXFormDialog {
    private String name;
    private ArrayList<String> pageNames = new ArrayList<String>();

    private JFrame dialog;
    private DescriptionPanel descriptionPanel;
    private List<SwingXFormImpl> forms = new ArrayList<SwingXFormImpl>();
    private JPanel pages;
    private CardLayout cardLayout;

    private HashMap<String, WizardPage> controllers = new HashMap<String, WizardPage>();
    private int currentPage = 0;

    private DefaultActionList actions;
    private JButtonBar buttons;

    public JWizardDialog(String name, XForm[] forms, Action helpAction, String description, ImageIcon icon) {
        this.name = name;

        // Use JFrame instead of JDialog to get maximize button.
        dialog = new JFrame(name);

        initActions(helpAction);

        cardLayout = new CardLayout();
        pages = new JPanel(cardLayout);
        for (XForm form : forms) {
            SwingXFormImpl swingFormImpl = (SwingXFormImpl) form;
            this.forms.add(swingFormImpl);

            JPanel panel = swingFormImpl.getPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

            addPage(form.getName(), panel);
        }

        buttons = UISupport.initFrameActions(actions, dialog);

        if (description != null || icon != null) {
            descriptionPanel = UISupport.buildDescription(name, description, icon);
            dialog.getContentPane().add(descriptionPanel, BorderLayout.NORTH);
        }

        dialog.getContentPane().add(pages, BorderLayout.CENTER);

        buttons.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.pack();
    }

    public XForm[] getForms() {
        List<XForm> result = new ArrayList<XForm>();
        for (XForm form : forms) {
            result.add(form);
        }
        return result.toArray(new XForm[result.size()]);
    }

    public void dispose() {
        dialog.dispose();
    }

    private void initActions(Action helpAction) {
        actions = new DefaultActionList();
        actions.addAction(new BackAction());
        actions.addAction(new NextAction());
        actions.addAction(new CancelAction());
        actions.addAction(new FinishAction());
        if (helpAction != null) {
            actions.addAction(helpAction);
        }
    }

    public void addAction(Action action) {
        DefaultActionList actions = new DefaultActionList();
        actions.addAction(action);
        buttons.addActions(actions);
    }

    private void addPage(String name, JComponent component) {
        pages.add(component, name);
        if (!pageNames.contains(name)) {
            pageNames.add(name);
        }
        actions.update();
    }

    public void addPageController(WizardPage controller) {
        controllers.put(controller.getName(), controller);
    }

    public void addPageAndController(JComponent component, WizardPage controller) {
        addPage(controller.getName(), component);
        addPageController(controller);
    }

    public void setValues(StringToStringMap values) {
        for (XForm form : forms) {
            form.setValues(values);
        }
    }

    public void setOptions(String field, Object[] options) {
        for (XForm form : forms) {
            form.setOptions(field, options);
        }
    }

    public XFormField getFormField(String name) {
        for (XForm form : forms) {
            XFormField formField = form.getFormField(name);
            if (formField != null) {
                return formField;
            }
        }

        return null;
    }

    public StringToStringMap getValues() {
        StringToStringMap result = new StringToStringMap();

        for (XForm form : forms) {
            result.putAll(form.getValues());
        }

        return result;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (showPage(0)) {
                Frame mainFrame = UISupport.getMainFrame();
                UISupport.centerDialog(dialog, mainFrame);
                // dialog.setVisible( visible );
                ModalFrameUtil.showAsModal(dialog, mainFrame);
            }
        } else {
            dialog.setVisible(visible);
        }
    }

    public boolean validate() {
        return true;
    }

    public void setFormFieldProperty(String name, Object value) {
        for (XForm form : forms) {
            form.setFormFieldProperty(name, value);
        }
    }

    public String getValue(String field) {
        for (XForm form : forms) {
            if (form.getComponent(field) != null) {
                return form.getComponent(field).getValue();
            }
        }

        return null;
    }

    public void setValue(String field, String value) {
        for (XForm form : forms) {
            if (form.getComponent(field) != null) {
                form.getComponent(field).setValue(value);
            }
        }
    }

    public int getValueIndex(String name) {
        for (SwingXFormImpl form : forms) {
            if (form.getComponent(name) != null) {
                Object[] options = form.getOptions(name);
                if (options == null) {
                    return -1;
                }

                return Arrays.asList(options).indexOf(form.getComponentValue(name));
            }
        }

        return -1;
    }

    public boolean show() {
        setReturnValue(XFormDialog.CANCEL_OPTION);
        show(new StringToStringMap());
        return getReturnValue() == XFormDialog.OK_OPTION;
    }

    public void setWidth(int i) {
        dialog.setPreferredSize(new Dimension(i, (int) dialog.getPreferredSize().getHeight()));
    }

    public void setSize(int w, int h) {
        dialog.setSize(w, h);
    }

    private boolean showPage(int pageNo) {
        currentPage = pageNo;
        String pageName = pageNames.get(currentPage);
        WizardPage page = controllers.get(pageName);

        descriptionPanel.setTitle(page.getName());
        descriptionPanel.setDescription(page.getDescription());
        cardLayout.show(pages, pageName);

        if (initPage(pageName, page)) {
            actions.update();
            return true;
        } else {
            setVisible(false);
            return false;
        }
    }

    private boolean initPage(String pageName, WizardPage page) {
        try {
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            return page.init();
        } catch (Exception e) {
            dialog.setCursor(Cursor.getDefaultCursor());
            SoapUI.logError(e);
            UISupport.showInfoMessage(pageName + " could not be initialized", this.name);
            return false;
        } finally {
            dialog.setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean runCurrentPage() {
        String pageName = pageNames.get(currentPage);
        WizardPage controller = controllers.get(pageName);
        try {
            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            return controller.run();
        } catch (Exception e) {
            dialog.setCursor(Cursor.getDefaultCursor());
            SoapUI.logError(e);
            UISupport.showInfoMessage(pageName + " failed", this.name);
            return false;
        } finally {
            dialog.setCursor(Cursor.getDefaultCursor());
        }
    }

    private class BackAction extends AbstractAction implements UpdateableAction {
        public BackAction() {
            super("< Back");
        }

        public void update() {
            boolean enable = false;
            if (currentPage > 0) {
                String pageName = pageNames.get(currentPage - 1);
                WizardPage prevPage = controllers.get(pageName);
                enable = prevPage.canGoBack();
            }
            setEnabled(enable);
        }

        public void actionPerformed(ActionEvent e) {
            showPage(currentPage - 1);
        }
    }

    private class NextAction extends AbstractAction implements UpdateableAction {
        public NextAction() {
            super("Next >");
        }

        public void update() {
            setEnabled(currentPage + 1 < pageNames.size());
        }

        public void actionPerformed(ActionEvent evt) {
            if (runCurrentPage()) {
                showPage(currentPage + 1);
            } else {
                setVisible(false);
            }
        }
    }

    private final class CancelAction extends AbstractAction implements UpdateableAction {
        public CancelAction() {
            super("Cancel");
        }

        public void update() {
        }

        public void actionPerformed(ActionEvent e) {
            setReturnValue(XFormDialog.CANCEL_OPTION);
            setVisible(false);
        }
    }

    private final class FinishAction extends AbstractAction implements UpdateableAction {
        public FinishAction() {
            super("Finish");
        }

        public void update() {
            setEnabled(currentPage == pageNames.size() - 1);
        }

        public void actionPerformed(ActionEvent e) {
            runCurrentPage();
            setReturnValue(XFormDialog.OK_OPTION);
            setVisible(false);
        }
    }

    public void release() {
        dialog.dispose();
    }
}
