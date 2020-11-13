/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.support.log;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.smartbear.soapui.core.Logging;
import org.apache.commons.collections.list.TreeList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Component for displaying log entries
 *
 * @author Ole.Matzura
 */

public class JLogList extends JPanel {
    private static final String INTERNAL_LOG_NAME = "InternalLog";
    private long maxRows = 1000;
    private JList logList;
    private final LogListModel model;
    private List<Logger> loggers = new ArrayList<Logger>();
    private InternalLogAppender internalLogAppender = new InternalLogAppender();
    private boolean tailing = true;
    private BlockingQueue<Object> linesToAdd = new LinkedBlockingQueue<Object>();
    private JCheckBoxMenuItem enableMenuItem;
    private final String title;

    public JLogList(String title) {
        super(new BorderLayout());
        this.title = title;

        model = new LogListModel();
        logList = new JList(model);
        logList.setToolTipText(title);
        logList.setCellRenderer(new LogAreaCellRenderer());
        logList.setPrototypeCellValue("Testing 123");
        logList.setFixedCellWidth(-1);

        JPopupMenu listPopup = new JPopupMenu();
        listPopup.add(new ClearAction());
        EnableAction enableAction = new EnableAction();
        enableMenuItem = new JCheckBoxMenuItem(enableAction);
        enableMenuItem.setSelected(true);
        listPopup.add(enableMenuItem);
        listPopup.addSeparator();
        listPopup.add(new CopyAction());
        listPopup.add(new SetMaxRowsAction());
        listPopup.addSeparator();
        listPopup.add(new ExportToFileAction());

        logList.setComponentPopupMenu(listPopup);

        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JScrollPane scrollPane = new JScrollPane(logList);
        UISupport.addPreviewCorner(scrollPane, true);
        add(scrollPane, BorderLayout.CENTER);

        SimpleAttributeSet requestAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(requestAttributes, Color.BLUE);

        SimpleAttributeSet responseAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(responseAttributes, Color.GREEN);

        try {
            maxRows = Long.parseLong(SoapUI.getSettings().getString("JLogList#" + title, "1000"));
        } catch (NumberFormatException ignore) {
        }
    }

    public void clear() {
        model.clear();
    }

    public JList getLogList() {
        return logList;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(long maxRows) {
        this.maxRows = maxRows;
    }

    public void addLine(Object line) {
        if (!isEnabled()) {
            return;
        }

        if (line instanceof LogEvent) {
            LogEvent ev = (LogEvent) line;
            linesToAdd.add(new LoggingEventWrapper(ev));

            if (ev.getThrown() != null) {
                Throwable t = ev.getThrown();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                StringTokenizer st = new StringTokenizer(sw.toString(), "\r\n");
                while (st.hasMoreElements()) {
                    linesToAdd.add("   " + st.nextElement());
                }
            }
        } else {
            linesToAdd.add(line);
        }
        model.ensureUpdateIsStarted();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        logList.setEnabled(enabled);
        enableMenuItem.setSelected(enabled);
    }

    private static class LogAreaCellRenderer extends DefaultListCellRenderer {
        private Map<Level, Color> levelColors = new HashMap<Level, Color>();

        private LogAreaCellRenderer() {
            levelColors.put(Level.ERROR, new Color(192, 0, 0));
            levelColors.put(Level.INFO, new Color(0, 92, 0));
            levelColors.put(Level.WARN, Color.ORANGE.darker().darker());
            levelColors.put(Level.DEBUG, new Color(0, 0, 128));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof LoggingEventWrapper) {
                LoggingEventWrapper eventWrapper = (LoggingEventWrapper) value;

                if (!isSelected && levelColors.containsKey(eventWrapper.getLevel())) {
                    component.setForeground(levelColors.get(eventWrapper.getLevel()));
                }
            }

            // Limit the length of the tool tip, to prevent long delays.
            String toolTip = component.getText();
            if (toolTip != null && toolTip.length() > 1000) {
                toolTip = toolTip.substring(0, 1000);
            }
            component.setToolTipText(toolTip);

            return component;
        }
    }

    private final static class LoggingEventWrapper {
        private final LogEvent loggingEvent;
        private String str;

        public LoggingEventWrapper(LogEvent loggingEvent) {
            this.loggingEvent = loggingEvent;
        }

        public Level getLevel() {
            return loggingEvent.getLevel();
        }

        public String toString() {
            if (str == null) {
                StringBuilder builder = new StringBuilder();
                builder.append(new Date(loggingEvent.getTimeMillis()));
                builder.append(':').append(loggingEvent.getLevel()).append(':').append(loggingEvent.getMessage());
                str = builder.toString();
            }

            return str;
        }
    }

    public void addLogger(String loggerName, boolean addAppender) {
        Logger logger = LogManager.getLogger(loggerName);
        if (addAppender) {
            Logging.addAppender(loggerName, internalLogAppender);
        }

        loggers.add(logger);
    }

    public Logger[] getLoggers() {
        return loggers.toArray(new Logger[loggers.size()]);
    }

    public void setLevel(Level level) {
        for (Logger logger : loggers) {
            Configurator.setLevel(logger.getName(), level);
        }
    }

    public Logger getLogger(String loggerName) {
        for (Logger logger : loggers) {
            if (logger.getName().equals(loggerName)) {
                return logger;
            }
        }

        return null;
    }

    private class InternalLogAppender extends AbstractAppender {

        InternalLogAppender() {
            super(INTERNAL_LOG_NAME, null, PatternLayout.createDefaultLayout());
        }

        public void append(LogEvent event) {
            addLine(event);
        }

        public void close() {
        }

        public boolean requiresLayout() {
            return false;
        }
    }

    public boolean monitors(String loggerName) {
        for (Logger logger : loggers) {
            if (loggerName.startsWith(logger.getName())) {
                return true;
            }
        }

        return false;
    }

    public void removeLogger(String loggerName) {
        for (Logger logger : loggers) {
            if (loggerName.equals(logger.getName())) {
                Logging.removeAppender(loggerName, internalLogAppender);
            }
        }
    }

    public void saveToFile(File file) {
        try {
            PrintWriter writer = new PrintWriter(file);
            for (int c = 0; c < model.getSize(); c++) {
                writer.println(model.getElementAt(c));
            }

            writer.close();
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    public boolean isTailing() {
        return tailing;
    }

    public void setTailing(boolean tail) {
        this.tailing = tail;
    }

	/*
    Helper classes.
	*/

    private class ClearAction extends AbstractAction {
        public ClearAction() {
            super("Clear");
        }

        public void actionPerformed(ActionEvent e) {
            model.clear();
        }
    }

    private class SetMaxRowsAction extends AbstractAction {
        public SetMaxRowsAction() {
            super("Set Max Rows");
        }

        public void actionPerformed(ActionEvent e) {
            String val = UISupport.prompt("Set maximum number of log rows to keep", "Set Max Rows",
                    String.valueOf(maxRows));
            if (val != null) {
                try {
                    maxRows = Long.parseLong(val);
                    SoapUI.getSettings().setString("JLogList#" + title, val);
                } catch (NumberFormatException e1) {
                    UISupport.beep();
                }
            }
        }
    }

    private class ExportToFileAction extends AbstractAction {
        public ExportToFileAction() {
            super("Export to File");
        }

        public void actionPerformed(ActionEvent e) {
            if (model.getSize() == 0) {
                UISupport.showErrorMessage("Log is empty; nothing to export");
                return;
            }

            File file = UISupport.getFileDialogs().saveAs(JLogList.this, "Save Log [] to File", "*.log", "*.log", null);
            if (file != null) {
                saveToFile(file);
            }
        }
    }

    private class CopyAction extends AbstractAction {
        public CopyAction() {
            super("Copy to clipboard");
        }

        public void actionPerformed(ActionEvent e) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            StringBuilder buf = new StringBuilder();
            int[] selectedIndices = logList.getSelectedIndices();
            if (selectedIndices.length == 0) {
                for (int c = 0; c < logList.getModel().getSize(); c++) {
                    buf.append(logList.getModel().getElementAt(c).toString());
                    buf.append("\r\n");
                }
            } else {
                for (int selectedIndex : selectedIndices) {
                    buf.append(logList.getModel().getElementAt(selectedIndex).toString());
                    buf.append("\r\n");
                }
            }

            StringSelection selection = new StringSelection(buf.toString());
            clipboard.setContents(selection, selection);
        }
    }

    private class EnableAction extends AbstractAction {
        public EnableAction() {
            super("Enable");
        }

        public void actionPerformed(ActionEvent e) {
            JLogList.this.setEnabled(enableMenuItem.isSelected());
        }
    }

    /**
     * Internal list model that for optimized storage and notifications
     *
     * @author Ole.Matzura
     */

    @SuppressWarnings("unchecked")
    private final class LogListModel extends AbstractListModel {
        private final List<Object> lines = Collections.synchronizedList(new TreeList());
        private ListUpdater updater = new ListUpdater();

        public int getSize() {
            return lines.size();
        }

        public Object getElementAt(int index) {
            return lines.get(index);
        }

        public void clear() {
            final int size = lines.size();
            if (size == 0) {
                return;
            }

            lines.clear();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireIntervalRemoved(LogListModel.this, 0, size - 1);
                }
            });
        }

        public void ensureUpdateIsStarted() {
            updater.ensureUpdateIsStarted();
        }

        private class ListUpdater implements Runnable {
            private volatile boolean updating;

            public void run() {
                String originalThreadName = Thread.currentThread().getName();
                Thread.currentThread().setName("LogList Updater for " + title);
                setUpdating(true);
                try {
                    Object line;
                    while ((line = getNextLine()) != null) {
                        try {
                            List<Object> linesToAddNow = new ArrayList<Object>();
                            linesToAddNow.add(line);
                            while ((line = linesToAdd.poll()) != null) {
                                linesToAddNow.add(line);
                            }
                            int oldSize = lines.size();
                            lines.addAll(linesToAddNow);
                            updateJList(oldSize);
                        } catch (Exception e) {
                            SoapUI.logError(e);
                        }
                    }
                } finally {
                    synchronized (this) {
                        updating = false;
                        if (!linesToAdd.isEmpty()) {
                            ensureUpdateIsStarted();
                        }
                    }
                    Thread.currentThread().setName(originalThreadName);
                }
            }

            public synchronized void ensureUpdateIsStarted() {
                if (!updating) {
                    setUpdating(true);
                    SoapUI.getThreadPool().submit(this);
                }
            }

            private Object getNextLine() {
                try {
                    return linesToAdd.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    //shouldn't really happen
                    return null;
                }
            }


            private void updateJList(final int oldSize) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            fireIntervalAdded(LogListModel.this, oldSize, lines.size() - 1);
                            int linesToRemove = lines.size() - ((int) maxRows);
                            if (linesToRemove > 0) {
                                for (int i = 0; i < linesToRemove; i++) {
                                    lines.remove(0);
                                }
                                fireIntervalRemoved(LogListModel.this, 0, linesToRemove);
                            }
                            if (tailing) {
                                logList.ensureIndexIsVisible(lines.size() - 1);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            private synchronized void setUpdating(boolean updating) {
                this.updating = updating;
            }
        }
    }
}
