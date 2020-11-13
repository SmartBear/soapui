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

import org.apache.logging.log4j.core.LogEvent;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import java.awt.Component;

/**
 * JTabbedPane that displays Log4J output in different tabs
 *
 * @author Ole.Matzura
 */

public class TabbedLog4JMonitor extends JTabbedPane implements Log4JMonitor {
    private JLogList defaultLogArea;

    public TabbedLog4JMonitor() {
        super(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public JLogList addLogArea(String title, String loggerName, boolean isDefault) {
        JLogList logArea = new JLogList(title);
        logArea.addLogger(loggerName, !isDefault);
        addTab(title, logArea);

        if (isDefault) {
            defaultLogArea = logArea;
        }

        return logArea;
    }

    public void logEvent(Object msg) {
        if (msg instanceof LogEvent) {
            LogEvent event = (LogEvent) msg;
            String loggerName = event.getLoggerName();

            for (int c = 0; c < getTabCount(); c++) {
                Component tabComponent = getComponentAt(c);
                if (tabComponent instanceof JLogList) {
                    JLogList logArea = (JLogList) tabComponent;
                    if (logArea.monitors(loggerName)) {
                        logArea.addLine(msg);
                    }
                }
            }
        } else if (defaultLogArea != null) {
            defaultLogArea.addLine(msg);
        }
    }

    public JLogList getLogArea(String title) {
        int ix = indexOfTab(title);
        return (JLogList) (ix == -1 ? null : getComponentAt(ix));
    }

    public boolean hasLogArea(String loggerName) {
        for (int c = 0; c < getTabCount(); c++) {
            Component tabComponent = getComponentAt(c);
            if (tabComponent instanceof JLogList) {
                JLogList logArea = (JLogList) tabComponent;
                if (logArea.monitors(loggerName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public JComponent getComponent() {
        return this;
    }

    public JLogList getCurrentLog() {
        int ix = getSelectedIndex();
        return ix == -1 ? null : getLogArea(getTitleAt(ix));
    }

    public void setCurrentLog(JLogList lastLog) {
        for (int c = 0; c < getTabCount(); c++) {
            Component tabComponent = getComponentAt(c);
            if (tabComponent == lastLog) {
                setSelectedComponent(tabComponent);
            }
        }
    }

    public boolean removeLogArea(String loggerName) {
        for (int c = 0; c < getTabCount(); c++) {
            JLogList tabComponent = (JLogList) getComponentAt(c);
            if (tabComponent.getLogger(loggerName) != null) {
                removeTabAt(c);
                return true;
            }
        }

        return false;
    }
}
