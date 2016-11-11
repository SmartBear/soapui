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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

/**
 * this class represents toolbar button for starting HermesJMS
 *
 * @author nebojsa.tasic
 */
public class StartHermesJMSButtonAction extends AbstractAction {
    public StartHermesJMSButtonAction() {
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/hermes-16x16.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Start HermesJMS application");
        putValue(Action.NAME, "HermesJMS");
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String hermesHome = SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS,
                    HermesUtils.defaultHermesJMSPath());
            if (!isHermesHomeValid(hermesHome)) {
                UISupport.showErrorMessage("Please set Hermes JMS path in Preferences->Tools ! ");
                if (UISupport.getMainFrame() != null) {
                    if (SoapUIPreferencesAction.getInstance().show(SoapUIPreferencesAction.INTEGRATED_TOOLS)) {
                        hermesHome = SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS,
                                HermesUtils.defaultHermesJMSPath());
                    }
                }

            }
            if (!isHermesHomeValid(hermesHome)) {
                return;
            }
            String extension = UISupport.isWindows() ? ".bat" : ".sh";
            String hermesBatPath = hermesHome + File.separator + "bin" + File.separator + "hermes" + extension;
            ProcessBuilder pb = new ProcessBuilder(hermesBatPath);
            Map<String, String> env = pb.environment();
            env.put("JAVA_HOME", System.getProperty("java.home"));
            pb.start();
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    private boolean isHermesHomeValid(String hermesHome) {
        File file = new File(hermesHome + File.separator + "bin" + File.separator + "hermes.bat");
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
