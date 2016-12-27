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

package com.eviware.soapui.autoupdate;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by avdeev on 27.08.2014.
 */
public class NewSoapUIVersionAvailableDialog extends JDialog {
    enum ReadyApiUpdateDialogResult {Update, Delay_1Day, Delay_3Days, Delay_7Days, DoNotUpdate, SkipThisVersion}

    private ReadyApiUpdateDialogResult dialogResult;
    private final static String NEW_VERSION_AVAILABLE_MESSAGE = "New Version Available";
    private final static String NEW_VERSION_AVAILABLE_MESSAGE_EX = "A new version of SoapUI is available, please check the details below.";
    public final static String SKIPPED_VERSION_SETTING = "SkippedVersion";
    private SoapUIVersionInfo newProductVersion, curVersion;
    private String releaseNotes;

    public NewSoapUIVersionAvailableDialog(SoapUIVersionInfo version, SoapUIVersionInfo curVersion, String releaseNotes) {
        super(UISupport.getMainFrame(), true);
        newProductVersion = version;
        this.releaseNotes = releaseNotes;
        this.curVersion = curVersion;

        Init();
    }

    protected void Init() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(UISupport.buildDescription(NEW_VERSION_AVAILABLE_MESSAGE, NEW_VERSION_AVAILABLE_MESSAGE_EX, null), new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        mainPanel.add(new JLabel("Current version:"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        mainPanel.add(new JLabel(curVersion.toString()), new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        mainPanel.add(new JLabel("New version:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        mainPanel.add(new JLabel(newProductVersion.toString()), new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        JEditorPane releaseNotesPane = createReleaseNotesPane();
        JScrollPane scb = new JScrollPane(releaseNotesPane);
        scb.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Release notes"));
        mainPanel.add(scb, new GridBagConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

        JPanel toolbar = buildToolbar(this);
        mainPanel.add(toolbar, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

        setTitle("New Version Check");
        getContentPane().add(mainPanel);
        setSize(new Dimension(550, 440));
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)/2 - getWidth()/2, (Toolkit.getDefaultToolkit().getScreenSize().height)/2 - getHeight()/2);
    }

    public ReadyApiUpdateDialogResult showDialog() {
        setVisible(true);
        return dialogResult;
    }

    protected JEditorPane createReleaseNotesPane() {
        JEditorPane text = new JEditorPane();
        try {
            //text.setPage("Release notes");
            text.setPage(this.releaseNotes);
            text.setEditable(false);
        } catch (IOException e) {
            text.setText("No release notes");
            SoapUI.logError(e);
        }
        return text;
    }

    protected JPanel buildToolbar(JDialog dialog) {
        final JComboBox<String> choice = new JComboBox<String>();
        choice.addItem("1 day");
        choice.addItem("3 days");
        choice.addItem("7 days");
        choice.setSelectedIndex(0);
        dialogResult = ReadyApiUpdateDialogResult.DoNotUpdate;

        JButton remindMeLaterButton = new JButton("Remind me later");
        remindMeLaterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (choice.getSelectedIndex()) {
                    case 0:
                        dialogResult = ReadyApiUpdateDialogResult.Delay_1Day;
                        break;
                    case 1:
                        dialogResult = ReadyApiUpdateDialogResult.Delay_3Days;
                        break;
                    case 2:
                        dialogResult = ReadyApiUpdateDialogResult.Delay_7Days;
                        break;
                    default:
                        dialogResult = ReadyApiUpdateDialogResult.Delay_1Day;
                }
                setVisible(false);
            }
        });

        JButton downloadButton = new JButton("Download and install");
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogResult = ReadyApiUpdateDialogResult.Update;
                setVisible(false);
            }
        });

        JButton ignoreUpdate = new JButton(new IgnoreUpdateAction(this));

        JPanel toolbarPanel = new JPanel(new GridBagLayout());
        toolbarPanel.add(choice, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        toolbarPanel.add(remindMeLaterButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        toolbarPanel.add(new JPanel(), new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        toolbarPanel.add(ignoreUpdate, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        toolbarPanel.add(downloadButton, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        return toolbarPanel;
    }

    protected class IgnoreUpdateAction extends AbstractAction {
        private JDialog dialog;

        public IgnoreUpdateAction(JDialog dialog) {
            super("Ignore this update");
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            SoapUI.getSettings().setString(SKIPPED_VERSION_SETTING, newProductVersion.toString());
            dialog.setVisible(false);
        }
    }
}
