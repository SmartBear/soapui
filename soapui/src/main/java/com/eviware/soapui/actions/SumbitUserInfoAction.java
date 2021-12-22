/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.actions;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.UniqueUserIdentifier;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JFriendlyTextField;
import com.smartbear.analytics.OSUserDescription;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.eviware.soapui.analytics.SoapUIActions.STAY_TUNED_DIALOG_ACCEPTED;
import static com.eviware.soapui.analytics.SoapUIActions.STAY_TUNED_DIALOG_SKIPPED;

public class SumbitUserInfoAction {
    private static final String FIRST_NAME_HINT = "Enter your first name";
    private static final String LAST_NAME_HINT = "Enter your last name";
    private static final String EMAIL_HINT = "Enter e-mail";
    private static final String DIALOG_CAPTION = "Stay Tuned!";
    private static final String DIALOG_MAIN_TEXT = "Want to stay in the loop?";
    private static final String DIALOG_DESCRIPTION = "We will use this information to help you get started, and provide you with best practices we’ve learned from over 7.5 Million development, quality and operations experts just like you.";
    private static final String OK_BTN_CAPTION = "Yes, I want to know";
    private static final String SKIP_BTN_CAPTION = "Skip";

    public SumbitUserInfoAction() {
    }

    public void show() {
        CollectUserInfoDialog cui = new CollectUserInfoDialog();
        cui.setVisible(true);
    }

    private class CollectUserInfoDialog extends JDialog {
        private JLabel title;
        private JLabel description;
        private JFriendlyTextField textFieldFirstName;
        private JFriendlyTextField textFieldLastName;
        private JFriendlyTextField textFieldEmail;
        private static final String VALID_EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        private Pattern validEmailRegex;

        private void setBackgroundColor(JPanel curPanel) {
            curPanel.setOpaque(true);
            curPanel.setBackground(Color.WHITE);
        }

        private void setBackgroundColor(JLabel curLabel) {
            curLabel.setOpaque(true);
            curLabel.setBackground(Color.WHITE);
        }

        public CollectUserInfoDialog() {
            super(UISupport.getMainFrame(), DIALOG_CAPTION, true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setUndecorated(true);
            setModal(true);
            setSize(430, 270);
            setBackground(Color.WHITE);

            JPanel jBasePanel = new JPanel(new BorderLayout(5, 5));
            jBasePanel.setBorder(new LineBorder(new Color(170, 170, 170), 2));
            setBackgroundColor(jBasePanel);
            this.add(jBasePanel);

            JPanel jBaseUserPanel = new JPanel(new BorderLayout(5, 5));
            setBackgroundColor(jBaseUserPanel);

            JLabel jCaption = new JLabel("  " + DIALOG_CAPTION);
            jCaption.setOpaque(true);
            jCaption.setPreferredSize(new Dimension(1000, 25));
            jCaption.setBackground(new Color(166, 192, 229));

            jBaseUserPanel.add(buildCaptionPanel(DIALOG_MAIN_TEXT, DIALOG_DESCRIPTION), BorderLayout.NORTH);
            jBaseUserPanel.add(buildControlsPanel());

            jBasePanel.add(jCaption, BorderLayout.NORTH);
            jBasePanel.add(jBaseUserPanel);

            validEmailRegex = Pattern.compile(VALID_EMAIL_PATTERN);
        }

        private JPanel buildCaptionPanel(String titleStr, String descriptionStr) {
            JPanel jRoot = new JPanel(new BorderLayout());
            jRoot.setBorder(new EmptyBorder(10, 30, 0, 25));
            setBackgroundColor(jRoot);
            title = new JLabel();
            setBackgroundColor(title);
            title.setText("<html><div style=\"font-size: 11px\"><b>" + titleStr + "</b></div></html>");
            title.setOpaque(true);
            title.setBackground(Color.WHITE);
            description = new JLabel();
            setBackgroundColor(description);
            description.setText("<html><div style=\"font-size: 9px\">" + descriptionStr + "</div></html>");
            description.setBorder(new EmptyBorder(5, 0, 0, 0));

            jRoot.add(title, BorderLayout.NORTH);
            jRoot.add(description);

            return jRoot;
        }

        private JPanel buildControlsPanel() {
            JPanel jbase = new JPanel(new BorderLayout());

            JPanel jLeftPanel = new JPanel();
            jLeftPanel.setLayout(new BorderLayout());
            jLeftPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
            JLabel arrowIcon = new JLabel(UISupport.createImageIcon("/big_arrow.png"));
            jLeftPanel.add(arrowIcon, BorderLayout.SOUTH);
            setBackgroundColor(jLeftPanel);

            JPanel jControlsPanel = new JPanel(new BorderLayout());
            jControlsPanel.setBorder(new EmptyBorder(5, 2, 10, 35));
            setBackgroundColor(jControlsPanel);
            jControlsPanel.add(buildButtonsPanel(), BorderLayout.SOUTH);
            jControlsPanel.add(buildUserInfoPanel());

            jbase.add(jLeftPanel, BorderLayout.WEST);
            jbase.add(jControlsPanel);

            return jbase;
        }

        private JPanel buildUserInfoPanel() {
            textFieldFirstName = new JFriendlyTextField(FIRST_NAME_HINT);
            textFieldFirstName.setPreferredSize(new Dimension(300, 24));
            textFieldLastName = new JFriendlyTextField(LAST_NAME_HINT);
            textFieldLastName.setPreferredSize(new Dimension(300, 24));

            JPanel namePanel = new JPanel(new GridLayout(1, 2, 8, 8));
            setBackgroundColor(namePanel);
            namePanel.add(textFieldFirstName);
            namePanel.add(textFieldLastName);

            textFieldEmail = new JFriendlyTextField(EMAIL_HINT);
            textFieldEmail.setPreferredSize(new Dimension(300, 24));
            JPanel jHelpEmail = new JPanel(new BorderLayout());
            setBackgroundColor(jHelpEmail);
            jHelpEmail.add(textFieldEmail, BorderLayout.NORTH);

            JPanel userInfoContent = new JPanel(new BorderLayout(8, 8));
            setBackgroundColor(userInfoContent);
            userInfoContent.add(namePanel, BorderLayout.NORTH);
            userInfoContent.add(jHelpEmail, BorderLayout.CENTER);

            return userInfoContent;
        }

        private JPanel buildButtonsPanel() {
            JButton jOkBtn = new JButton(OK_BTN_CAPTION);
            jOkBtn.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            jOkBtn.setBackground(new Color(157, 200, 130));
            jOkBtn.setOpaque(true);
            jOkBtn.setForeground(Color.WHITE);
            jOkBtn.setPreferredSize(new Dimension(300, 24));
            jOkBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (handleOk()) {
                        Analytics.trackAction(STAY_TUNED_DIALOG_ACCEPTED);
                        setVisible(false);
                    }
                }
            });

            JPanel jOkPanel = new JPanel(new BorderLayout());
            jOkPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
            setBackgroundColor(jOkPanel);
            jOkPanel.add(jOkBtn);

            JButton jSkip = new JButton(SKIP_BTN_CAPTION);
            jSkip.setBorder(new LineBorder(new Color(170, 170, 170), 1));
            jSkip.setForeground(new Color(170, 170, 170));
            if (UISupport.isMac()) {
                jSkip.setBackground(new Color(236, 236, 236));
            }
            jSkip.setOpaque(true);
            jSkip.setPreferredSize(new Dimension(60, 20));
            jSkip.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Analytics.trackAction(STAY_TUNED_DIALOG_SKIPPED);
                    setVisible(false);
                }
            });

            JPanel buttonsContent = new JPanel(new BorderLayout());
            buttonsContent.setBorder(new EmptyBorder(20, 0, 20, 0));
            setBackgroundColor(buttonsContent);

            buttonsContent.add(jSkip, BorderLayout.EAST);
            buttonsContent.add(jOkPanel);

            return buttonsContent;
        }

        @Override
        public void setVisible(boolean b) {
            UISupport.centerDialog(this);
            super.setVisible(b);
        }

        private String getUserFirstName() {
            String name = textFieldFirstName.getText();
            name = name.replace(FIRST_NAME_HINT, "");
            return name;
        }

        private String getUserLastname() {
            String name = textFieldLastName.getText();
            name = name.replace(LAST_NAME_HINT, "");
            return name;
        }

        private String getUserEMail() {
            String email = textFieldEmail.getText();
            email = email.replace(EMAIL_HINT, "");
            return email;
        }

        protected boolean handleOk() {
            if (!validateFormValues()) {
                return false;
            }
            UniqueUserIdentifier userIdentifier = UniqueUserIdentifier.getInstance();
            OSUserDescription osUserDescription = new OSUserDescription(
                    getUserFirstName(), getUserLastname(), getUserEMail(), userIdentifier.getUserId());
            Analytics.trackUserInfo(osUserDescription);
            return true;
        }

        private boolean validateFormValues() {
            List<String> fieldErrors = new ArrayList<String>();
            if (StringUtils.isNullOrEmpty(getUserFirstName())) {
                fieldErrors.add("your first name");
            }
            if (StringUtils.isNullOrEmpty(getUserLastname())) {
                fieldErrors.add("your last name");
            }
            if (!isValidEmailAddress(getUserEMail())) {
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
