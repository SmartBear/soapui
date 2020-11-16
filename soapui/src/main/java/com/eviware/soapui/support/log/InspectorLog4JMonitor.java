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

import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import org.apache.logging.log4j.core.LogEvent;

import javax.swing.JComponent;
import java.awt.Component;
import java.util.List;

/**
 * JTabbedPane that displays Log4J output in different tabs
 *
 * @author Ole.Matzura
 */

public class InspectorLog4JMonitor implements JInspectorPanel, Log4JMonitor {
    private JLogList defaultLogArea;
    private JInspectorPanel inspectorPanel;

    public InspectorLog4JMonitor(JComponent content) {
        inspectorPanel = JInspectorPanelFactory.build(content);

        setResizeWeight(0.9F);
    }

    public JLogList addLogArea(String title, String loggerName, boolean isDefault) {
        JLogList logArea = new JLogList(title);
        logArea.addLogger(loggerName, !isDefault);
        JComponentInspector<JLogList> inspector = new JComponentInspector<JLogList>(logArea, title, null, true);
        addInspector(inspector);

        if (isDefault) {
            defaultLogArea = logArea;
            activate(inspector);
            setDividerLocation(500);
        }

        return logArea;
    }

    public void logEvent(Object msg) {
        if (msg instanceof LogEvent) {
            LogEvent event = (LogEvent) msg;
            String loggerName = event.getLoggerName();

            for (Inspector inspector : inspectorPanel.getInspectors()) {
                Component tabComponent = inspector.getComponent();
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
        Inspector inspector = inspectorPanel.getInspectorByTitle(title);
        return (JLogList) (title == null ? null : inspector.getComponent());
    }

    public boolean hasLogArea(String loggerName) {
        for (Inspector inspector : getInspectors()) {
            Component tabComponent = inspector.getComponent();
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
        return inspectorPanel.getComponent();
    }

    public Inspector getCurrentInspector() {
        return inspectorPanel.getCurrentInspector();
    }

    public Inspector getInspectorByTitle(String title) {
        return inspectorPanel.getInspectorByTitle(title);
    }

    public List<Inspector> getInspectors() {
        return inspectorPanel.getInspectors();
    }

    public void setCurrentInspector(String s) {
        inspectorPanel.setCurrentInspector(s);
    }

    public void setDefaultDividerLocation(float v) {
        inspectorPanel.setDefaultDividerLocation(v);
    }

    public void setDividerLocation(int i) {
        inspectorPanel.setDividerLocation(i);
    }

    public void setResizeWeight(double v) {
        inspectorPanel.setResizeWeight(v);
    }

    public void setCurrentLog(JLogList lastLog) {
        for (Inspector inspector : getInspectors()) {
            Component tabComponent = inspector.getComponent();
            if (tabComponent == lastLog) {
                activate(inspector);
                return;
            }
        }

        inspectorPanel.deactivate();
    }

    public void activate(Inspector inspector) {
        inspectorPanel.activate(inspector);
    }

    public <T extends Inspector> T addInspector(T inspector) {
        return inspectorPanel.addInspector(inspector);
    }

    public void deactivate() {
        inspectorPanel.deactivate();
    }

    public void removeInspector(Inspector inspector) {
        inspectorPanel.removeInspector(inspector);
    }

    public JLogList getCurrentLog() {
        return (JLogList) (inspectorPanel.getCurrentInspector() == null ? null : inspectorPanel.getCurrentInspector()
                .getComponent());
    }

    public boolean removeLogArea(String loggerName) {
        for (Inspector inspector : getInspectors()) {
            JLogList logList = ((JLogList) ((JComponentInspector<?>) inspector).getComponent());
            if (logList.getLogger(loggerName) != null) {
                logList.removeLogger(loggerName);
                inspectorPanel.removeInspector(inspector);

                return true;
            }
        }

        return false;
    }

    public int getDividerLocation() {
        return inspectorPanel.getDividerLocation();
    }

    public void setContentComponent(JComponent component) {
        inspectorPanel.setContentComponent(component);
    }

    public void release() {
        inspectorPanel.release();
    }

    public void setResetDividerLocation() {
        inspectorPanel.setResetDividerLocation();
    }

    public void setInspectorVisible(Inspector inspector, boolean b) {
        inspectorPanel.setInspectorVisible(inspector, b);
    }

    public Inspector getInspector(String inspectorId) {
        return inspectorPanel.getInspector(inspectorId);
    }
}
