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

package com.eviware.soapui.support.log;

import javax.swing.JComponent;

/**
 * JTabbedPane that displays Log4J output in different tabs
 *
 * @author Ole.Matzura
 */

public interface Log4JMonitor {
    public JLogList addLogArea(String title, String loggerName, boolean isDefault);

    public void logEvent(Object msg);

    public JLogList getLogArea(String title);

    public boolean hasLogArea(String loggerName);

    public JComponent getComponent();

    public JLogList getCurrentLog();

    public void setCurrentLog(JLogList lastLog);

    public boolean removeLogArea(String loggerName);
}
