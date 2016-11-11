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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.util.HermesUtils;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.components.DirectoryFormComponent;
import com.eviware.soapui.support.components.SimpleForm;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class StartHermesJMS extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "StarHermesJMS";

    public StartHermesJMS() {
        super("Start HermesJMS", "Start HermesJMS application");
    }

    public void perform(WsdlProject project, Object param) {
        String hermesConfigPath = chooseFolderDialog(project);

        if (hermesConfigPath == null) {
            return;
        }

        project.setHermesConfig(hermesConfigPath);

        String hermesHome = SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS, HermesUtils.defaultHermesJMSPath());
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
        startHermesJMS(hermesConfigPath, hermesHome);
    }

    private boolean isHermesHomeValid(String hermesHome) {
        File file = new File(hermesHome + File.separator + "bin" + File.separator + "hermes.bat");
        if (file.exists()) {
            return true;
        }
        return false;
    }

    private void startHermesJMS(String hermesConfigPath, String hermesHome) {
        String extension = UISupport.isWindows() ? ".bat" : ".sh";
        String hermesBatPath = hermesHome + File.separator + "bin" + File.separator + "hermes" + extension;
        try {
            File file = new File(hermesConfigPath + File.separator + HermesUtils.HERMES_CONFIG_XML);
            if (!file.exists()) {
                UISupport.showErrorMessage("No hermes-config.xml in this path!");
                return;
            }
            ProcessBuilder pb = new ProcessBuilder(hermesBatPath);
            Map<String, String> env = pb.environment();
            env.put("HERMES_CONFIG", hermesConfigPath);
            env.put("JAVA_HOME", System.getProperty("java.home"));
            pb.start();
        } catch (IOException e) {
            SoapUI.logError(e);
        }
    }

    private String chooseFolderDialog(WsdlProject project) {
        HermesConfigDialog chooseHermesConfigPath = new HermesConfigDialog(PropertyExpander.expandProperties(project,
                project.getHermesConfig()));
        chooseHermesConfigPath.setVisible(true);
        String hermesConfigPath = chooseHermesConfigPath.getPath();
        return hermesConfigPath;
    }

    private class HermesConfigDialog extends SimpleDialog {

        String path;
        DirectoryFormComponent folderComponent;

        public HermesConfigDialog(String initialPath) {
            super("Start  HermesJMS", "Hermes configuration", HelpUrls.START_HERMES_HELP_URL, true);
            setVisible(false);
            folderComponent.setValue(initialPath);
            folderComponent.setInitialFolder(initialPath);

        }

        protected Component buildContent() {

            SimpleForm form = new SimpleForm();
            folderComponent = new DirectoryFormComponent(
                    "Location of desired HermesJMS configuration (hermes-config.xml)");
            form.addSpace(5);
            form.append("Path", folderComponent);
            form.addSpace(5);

            return form.getPanel();
        }

        protected boolean handleOk() {
            setPath(folderComponent.getValue());
            return true;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
