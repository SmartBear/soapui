package com.eviware.soapui.actions;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.SimpleForm;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SumbitUserInfoAction {
    private static final String NAME = "Name";
    private static final String WORK_EMAIL = "Work email";

    public SumbitUserInfoAction() {

    }

    public void show() {
        CollectUserInfoDialog cui = new CollectUserInfoDialog();
        cui.setPreferredSize(new Dimension(440,230));
        cui.setVisible(true);
    }

    private class CollectUserInfoDialog extends SimpleDialog {

        private static final String VALID_EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        private SimpleForm form;
        private Pattern validEmailRegex;

        CollectUserInfoDialog() {
            super("Stay Tuned!", "Please provide your name and e-mail to receive updates and additional materials for SoapUI", "www.smartbear.com");
            this.setModal(true);
            validEmailRegex = Pattern.compile(VALID_EMAIL_PATTERN);
        }

        protected final class SkipAction extends AbstractAction {
            public SkipAction() {
                super("Skip");
            }

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }

        protected final class SubmitAction extends AbstractAction {
            public SubmitAction() {
                super("Submit");
            }

            public void actionPerformed(ActionEvent e) {
                if (handleOk()) {
                    setVisible(false);
                }
            }
        }

        public ActionList buildActions(String url, boolean okAndCancel) {
            DefaultActionList actions = new DefaultActionList("Actions");
            if (url != null) {
                actions.addAction(new HelpAction(url));
            }

            SkipAction okAction = new SkipAction();
            actions.addAction(okAction);
            SubmitAction submitAction = new SubmitAction();
            actions.addAction(submitAction);
            actions.setDefaultAction(submitAction);
            return actions;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Component buildContent() {
            form = new SimpleForm();
            form.appendTextField(NAME, "Name");
            form.appendTextField(WORK_EMAIL, "Your email at work. We will send you an email and ask you to verify it.");

            JPanel wrapperPanel = new JPanel(new BorderLayout());
            wrapperPanel.setBorder(new EmptyBorder(3, 0, 3, 0));
            wrapperPanel.add(form.getPanel(), BorderLayout.CENTER);
            return wrapperPanel;
        }

        @Override
        protected boolean handleOk() {
            if (!validateFormValues()) {
                return false;
            }
            Analytics.trackOSUser(form.getComponentValue(NAME), form.getComponentValue(WORK_EMAIL));
            return true;
        }


        private boolean validateFormValues() {
            List<String> fieldErrors = new ArrayList<String>();
            if (StringUtils.isNullOrEmpty(form.getComponentValue(NAME))) {
                fieldErrors.add("your name");
            }
            if (!isValidEmailAddress(form.getComponentValue(WORK_EMAIL))) {
                fieldErrors.add("a valid email address");
            }
            if (fieldErrors.isEmpty()) {
                return true;
            } else {
                StringBuilder buf = new StringBuilder("You must enter ");
                int numberOfErrors = fieldErrors.size();
                for (int i = 0; i < numberOfErrors; i++) {
                    if (i > 0) {
                        buf.append(i < numberOfErrors - 1 ? ", " : " and ");
                    }
                    buf.append(fieldErrors.get(i));
                }
                buf.append(".");
                UISupport.showErrorMessage(buf.toString());
                return false;
            }
        }

        private boolean isValidEmailAddress(String email) {
            return StringUtils.hasContent(email) && validEmailRegex.matcher(email).matches();
        }

    }
}
