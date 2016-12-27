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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.wsi;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.DefaultHyperlinkListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

/**
 * Panel for displaying a WS-I Report
 *
 * @author ole.matzura
 */

public class WSIReportPanel extends JPanel {
    private File reportFile;
    private JEditorPane editorPane;
    private final String configFile;
    private final File logFile;
    private SaveReportAction saveReportAction;

    // private BrowserComponent browser;

    public WSIReportPanel(File reportFile, String configFile, File logFile, boolean addToolbar) throws Exception {
        super(new BorderLayout());

        this.reportFile = reportFile;
        this.configFile = configFile;
        this.logFile = logFile;

        saveReportAction = new SaveReportAction();

        if (addToolbar) {
            add(buildToolbar(), BorderLayout.NORTH);
        }

        add(buildContent(), BorderLayout.CENTER);
    }

    public SaveReportAction getSaveReportAction() {
        return saveReportAction;
    }

    private JComponent buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        toolbar.addFixed(UISupport.createToolbarButton(saveReportAction));
        toolbar.addGlue();
        toolbar.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        return toolbar;
    }

    private JComponent buildContent() throws Exception {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);

        editorPane = new JEditorPane();
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setEditable(false);
        editorPane.setPage(reportFile.toURI().toURL());
        editorPane.addHyperlinkListener(new DefaultHyperlinkListener(editorPane));

        JTextArea configContent = new JTextArea();
        configContent.setEditable(false);
        configContent.setText(configFile);

        // browser = new BrowserComponent( false );
        // browser.navigate( reportFile.toURI().toURL().toString(), null );

        JScrollPane scrollPane = new JScrollPane(editorPane);
        UISupport.addPreviewCorner(scrollPane, true);
        tabs.addTab("Report", scrollPane);
        tabs.addTab("Config", new JScrollPane(configContent));

        if (logFile != null) {
            // String logFileContent = XmlObject.Factory.parse( logFile
            // ).toString();
            String logFileContent = XmlUtils.createXmlObject(logFile).toString();
            JTextArea logContent = new JTextArea();
            logContent.setEditable(false);
            logContent.setText(logFileContent);

            tabs.addTab("Log", new JScrollPane(logContent));
        }

        return UISupport.createTabPanel(tabs, true);
    }

    public class SaveReportAction extends AbstractAction {
        public SaveReportAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/export.png"));
            putValue(Action.SHORT_DESCRIPTION, "Saves this report to a file");
        }

        public void actionPerformed(ActionEvent e) {
            File file = UISupport.getFileDialogs().saveAs(this, "Save Report", "html", "HTML files", null);
            if (file == null) {
                return;
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(editorPane.getText());
                writer.close();

                UISupport.showInfoMessage("Report saved to [" + file.getAbsolutePath() + "]");
            } catch (Exception e1) {
                SoapUI.logError(e1);
            }
        }
    }
}
