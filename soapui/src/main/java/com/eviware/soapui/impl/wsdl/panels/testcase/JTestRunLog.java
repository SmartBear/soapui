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

package com.eviware.soapui.impl.wsdl.panels.testcase;

import com.eviware.soapui.impl.wsdl.testcase.TestCaseLogItem;
import com.eviware.soapui.impl.wsdl.testcase.TestCaseLogModel;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.components.JHyperlinkLabel;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Panel for displaying TestStepResults
 *
 * @author Ole.Matzura
 */

public class JTestRunLog extends JPanel implements TestRunLog {
    private TestCaseLogModel logListModel;
    private JList testLogList;
    private boolean errorsOnly = false;
    private final Settings settings;
    private Set<String> boldTexts = new HashSet<String>();
    private boolean follow = true;
    protected int selectedIndex;
    private XFormDialog optionsDialog;

    public JTestRunLog(Settings settings) {
        super(new BorderLayout());
        this.settings = settings;

        errorsOnly = settings.getBoolean(OptionsForm.class.getName() + "@errors_only");

        buildUI();
    }

    private void buildUI() {
        logListModel = new TestCaseLogModel();
        logListModel.setMaxSize((int) settings.getLong(OptionsForm.class.getName() + "@max_rows", 1000));

        testLogList = new JList(logListModel);
        testLogList.setCellRenderer(new TestLogCellRenderer());
        testLogList.setPrototypeCellValue("Testing 123");
        testLogList.setFixedCellWidth(-1);
        testLogList.addMouseListener(new LogListMouseListener());

        JScrollPane scrollPane = new JScrollPane(testLogList);
        add(scrollPane, BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createSmallToolbar();

        addToolbarButtons(toolbar);

        return toolbar;
    }

    protected JList getTestLogList() {
        return testLogList;
    }

    public boolean isErrorsOnly() {
        return errorsOnly;
    }

    public boolean isFollow() {
        return follow;
    }

    protected void addToolbarButtons(JXToolBar toolbar) {
        toolbar.addFixed(UISupport.createToolbarButton(new ClearLogAction()));
        toolbar.addFixed(UISupport.createToolbarButton(new SetLogOptionsAction()));
        toolbar.addFixed(UISupport.createToolbarButton(new ExportLogAction()));
    }

    private final class TestLogCellRenderer extends JLabel implements ListCellRenderer {
        private Font boldFont;
        private Font normalFont;
        private JHyperlinkLabel hyperlinkLabel = new JHyperlinkLabel("");

        public TestLogCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            setIcon(null);
            boldFont = getFont().deriveFont(Font.BOLD);
            normalFont = getFont();

            hyperlinkLabel.setOpaque(true);
            hyperlinkLabel.setForeground(Color.BLUE.darker().darker().darker());
            hyperlinkLabel.setUnderlineColor(Color.GRAY);
            hyperlinkLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 3, 3));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof String) {
                setText(value.toString());
            } else if (value instanceof TestCaseLogItem) {
                TestCaseLogItem logItem = (TestCaseLogItem) value;
                String msg = logItem.getMsg();
                setText(msg == null ? "" : msg);
            }

            TestStepResult result = logListModel.getResultAt(index);
            if (result != null && !getText().startsWith(" ->")) {
                hyperlinkLabel.setText(getText());
                hyperlinkLabel.setBackground(getBackground());
                hyperlinkLabel.setEnabled(list.isEnabled());

                if (result.getStatus() == TestStepStatus.OK) {
                    hyperlinkLabel.setIcon(UISupport.createImageIcon("/valid_assertion.gif"));
                } else if (result.getStatus() == TestStepStatus.FAILED) {
                    hyperlinkLabel.setIcon(UISupport.createImageIcon("/failed_assertion.gif"));
                } else {
                    hyperlinkLabel.setIcon(UISupport.createImageIcon("/unknown_assertion.png"));
                }

                return hyperlinkLabel;
            }

            setEnabled(list.isEnabled());

            if (boldTexts.contains(getText())) {
                setFont(boldFont);
            } else {
                setFont(normalFont);
            }

            return this;
        }
    }

    /**
     * Mouse Listener for triggering default action and showing popup for log
     * list items
     *
     * @author Ole.Matzura
     */

    private final class LogListMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            int index = testLogList.getSelectedIndex();
            if (index != -1 && (index == selectedIndex || e.getClickCount() > 1)) {
                TestStepResult result = logListModel.getResultAt(index);
                if (result != null && result.getActions() != null) {
                    result.getActions().performDefaultAction(new ActionEvent(this, 0, null));
                }
            }

            selectedIndex = index;
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        public void showPopup(MouseEvent e) {
            int row = testLogList.locationToIndex(e.getPoint());
            if (row == -1) {
                return;
            }

            if (testLogList.getSelectedIndex() != row) {
                testLogList.setSelectedIndex(row);
            }

            TestStepResult result = logListModel.getResultAt(row);
            if (result == null) {
                return;
            }

            ActionList actions = result.getActions();

            if (actions == null || actions.getActionCount() == 0) {
                return;
            }

            JPopupMenu popup = ActionSupport.buildPopup(actions);
            UISupport.showPopup(popup, testLogList, e.getPoint());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog#clear()
     */
    public synchronized void clear() {
        logListModel.clear();
        boldTexts.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog#addText(java.lang
     * .String)
     */
    public synchronized void addText(String string) {
        logListModel.addText(string);
        if (follow) {
            testLogList.ensureIndexIsVisible(logListModel.getSize() - 1);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.impl.wsdl.panels.testcase.TestRunLog#addTestStepResult
     * (com.eviware.soapui.model.testsuite.TestStepResult)
     */
    public synchronized void addTestStepResult(TestStepResult stepResult) {
        if (errorsOnly && stepResult.getStatus() != TestStepResult.TestStepStatus.FAILED) {
            return;
        }

        logListModel.addTestStepResult(stepResult);
        if (follow) {
            try {
                testLogList.ensureIndexIsVisible(logListModel.getSize() - 1);
            } catch (RuntimeException e) {
            }
        }
    }

    public TestCaseLogModel getLogListModel() {
        return logListModel;
    }

    public void setLogListModel(TestCaseLogModel logListModel) {
        this.logListModel = logListModel;
        testLogList.setModel(logListModel);
    }

    private class SetLogOptionsAction extends AbstractAction {
        public SetLogOptionsAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/preferences.png"));
            putValue(Action.SHORT_DESCRIPTION, "Sets TestCase Log Options");
        }

        public void actionPerformed(ActionEvent e) {
            if (optionsDialog == null) {
                optionsDialog = ADialogBuilder.buildDialog(OptionsForm.class);
            }

            optionsDialog.setIntValue(OptionsForm.MAXROWS,
                    (int) settings.getLong(OptionsForm.class.getName() + "@max_rows", 1000));
            optionsDialog.setBooleanValue(OptionsForm.ERRORSONLY,
                    settings.getBoolean(OptionsForm.class.getName() + "@errors_only"));
            optionsDialog.setBooleanValue(OptionsForm.FOLLOW, follow);

            if (optionsDialog.show()) {
                int maxRows = optionsDialog.getIntValue(OptionsForm.MAXROWS, 1000);
                logListModel.setMaxSize(maxRows);
                settings.setLong(OptionsForm.class.getName() + "@max_rows", maxRows);
                errorsOnly = optionsDialog.getBooleanValue(OptionsForm.ERRORSONLY);
                settings.setBoolean(OptionsForm.class.getName() + "@errors_only", errorsOnly);

                follow = optionsDialog.getBooleanValue(OptionsForm.FOLLOW);
            }
        }
    }

    @AForm(name = "Log Options", description = "Set options for the run log below")
    private static interface OptionsForm {
        @AField(name = "Max Rows", description = "Sets the maximum number of rows to keep in the log", type = AFieldType.INT)
        public static final String MAXROWS = "Max Rows";

        @AField(name = "Errors Only", description = "Logs only TestStep errors in the log", type = AFieldType.BOOLEAN)
        public static final String ERRORSONLY = "Errors Only";

        @AField(name = "Follow", description = "Follow log content", type = AFieldType.BOOLEAN)
        public static final String FOLLOW = "Follow";
    }

    private class ClearLogAction extends AbstractAction {
        public ClearLogAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/clear.png"));
            putValue(Action.SHORT_DESCRIPTION, "Clears the log");
        }

        public void actionPerformed(ActionEvent e) {
            logListModel.clear();
        }
    }

    private class ExportLogAction extends AbstractAction {
        public ExportLogAction() {
            putValue(Action.SMALL_ICON, UISupport.createImageIcon("/export.png"));
            putValue(Action.SHORT_DESCRIPTION, "Exports this log to a file");
        }

        public void actionPerformed(ActionEvent e) {
            File file = UISupport.getFileDialogs().saveAs(this, "Save Log");
            if (file != null) {
                try {
                    PrintWriter out = new PrintWriter(file);
                    printLog(out);

                    out.close();
                } catch (FileNotFoundException e1) {
                    UISupport.showErrorMessage(e1);
                }
            }
        }
    }

    public void setStepIndex(int i) {
        logListModel.setStepIndex(i);
    }

    public synchronized void addBoldText(String string) {
        boldTexts.add(string);
        addText(string);
    }

    public void release() {
        if (optionsDialog != null) {
            optionsDialog.release();
            optionsDialog = null;
        }
    }

    public void printLog(PrintWriter out) {
        for (int c = 0; c < logListModel.getSize(); c++) {
            Object value = logListModel.getElementAt(c);
            if (value instanceof String) {
                out.println(value.toString());
            } else if (value instanceof TestCaseLogItem) {
                TestCaseLogItem logItem = (TestCaseLogItem) value;
                String msg = logItem.getMsg();
                if (StringUtils.hasContent(msg)) {
                    out.println(msg);
                }
            }
        }
    }
}
