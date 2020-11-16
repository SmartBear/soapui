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

package com.eviware.soapui.actions;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.tools.MockAsWar;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.validators.RequiredValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MockAsWarAction extends AbstractSoapUIAction<WsdlProject> {
    private XFormDialog dialog;
    private Logger log = LogManager.getLogger(MockAsWarAction.class);

    public MockAsWarAction() {
        super("Deploy As War", "Deploys Project MockServices as a WAR file");
    }

    public void perform(WsdlProject project, Object param) {
        // check for mockservices
        if ((project.getMockServiceCount() + project.getRestMockServiceCount()) == 0) {
            UISupport.showErrorMessage("Project does not have any MockServices to deploy");
            return;
        }

        if (dialog == null) {
            buildDialog();
        }

        XFormField settingFile = getPreFilledSettings();

        XFormField warFile = dialog.getFormField(MockAsWarDialog.WAR_FILE);

        String passwordForEncryption = project.getSettings().getString(ProjectSettings.SHADOW_PASSWORD, null);
        project.getSettings().setString(ProjectSettings.SHADOW_PASSWORD, null);

        if (dialog.show()) {
            project.beforeSave();
            try {
                project.save();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                project.getSettings().setString(ProjectSettings.SHADOW_PASSWORD, passwordForEncryption);
            }

            MockAsWar mockAsWar = new MockAsWar(project.getPath(),
                    dialog.getBooleanValue(MockAsWarDialog.GLOBAL_SETTINGS) ? settingFile.getValue() : "",
                    dialog.getFormField(MockAsWarDialog.WAR_DIRECTORY).getValue(),
                    dialog.getFormField(MockAsWarDialog.WAR_FILE).getValue(),
                    dialog.getBooleanValue(MockAsWarDialog.EXT_LIBS),
                    dialog.getBooleanValue(MockAsWarDialog.ACTIONS), dialog.getBooleanValue(MockAsWarDialog.LISTENERS),
                    dialog.getValue(MockAsWarDialog.MOCKSERVICE_ENDPOINT),
                    dialog.getBooleanValue(MockAsWarDialog.ENABLE_WEBUI),
                    project);
            mockAsWar.createMockAsWarArchive();

            if (project.getRestMockServiceCount() > 0) {
                Analytics.trackAction(SoapUIActions.DEPLOY_REST_MOCK_AS_WAR);
            }

            if (project.getMockServiceCount() > 0) {
                Analytics.trackAction(SoapUIActions.DEPLOY_SOAP_MOCK_AS_WAR);
            }
        }
    }

    // Only to be called from test class
    protected void setDialog(XFormDialog dialog) {
        this.dialog = dialog;
    }

    private XFormField getPreFilledSettings() {
        XFormField settingFile = dialog.getFormField(MockAsWarDialog.SETTINGS_FILE);
        DefaultSoapUICore soapUICore = (DefaultSoapUICore) SoapUI.getSoapUICore();
        settingFile.setValue(soapUICore.getSettingsFile());
        settingFile.setEnabled(dialog.getBooleanValue(MockAsWarDialog.GLOBAL_SETTINGS));
        return settingFile;
    }

    private void buildDialog() {
        dialog = ADialogBuilder.buildDialog(MockAsWarDialog.class);
        dialog.getFormField(MockAsWarDialog.GLOBAL_SETTINGS).addFormFieldListener(new XFormFieldListener() {
            public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                dialog.getFormField(MockAsWarDialog.SETTINGS_FILE).setEnabled(Boolean.valueOf(newValue));
            }
        });

        dialog.getFormField(MockAsWarDialog.WAR_DIRECTORY).addFormFieldValidator(
                new RequiredValidator("WAR Directory is required"));
    }

    @AForm(description = "Configure what to include in generated WAR", name = "Deploy Project as WAR", helpUrl = HelpUrls.MOCKASWAR_HELP_URL)
    protected interface MockAsWarDialog {
        @AField(description = "Specify if global settings should be included", name = "Include Global Settings", type = AFieldType.BOOLEAN)
        public final static String GLOBAL_SETTINGS = "Include Global Settings";

        @AField(description = "Specify Settings File", name = "Settings", type = AFieldType.FILE)
        public final static String SETTINGS_FILE = "Settings";

        @AField(description = "Specify if action extensions should be included", name = "Include Actions", type = AFieldType.BOOLEAN)
        public final static String ACTIONS = "Include Actions";

        @AField(description = "Specify if listener extensions should be included", name = "Include Listeners", type = AFieldType.BOOLEAN)
        public final static String LISTENERS = "Include Listeners";

        @AField(description = "Include jar files from ext folder", name = "Include External Jar Files", type = AFieldType.BOOLEAN)
        public final static String EXT_LIBS = "Include External Jar Files";

        @AField(description = "Check to enable WebUI", name = "WebUI", type = AFieldType.BOOLEAN)
        public final static String ENABLE_WEBUI = "WebUI";

        @AField(description = "Local endpoint that will be used for WSDL endpoints/includes/imports", name = "MockService Endpoint", type = AFieldType.STRING)
        public final static String MOCKSERVICE_ENDPOINT = "MockService Endpoint";

        @AField(description = "Specify name of target War File", name = "War File", type = AFieldType.FILE)
        public final static String WAR_FILE = "War File";

        @AField(description = "Specify a directory where War file structure will be created", name = "War Directory", type = AFieldType.FOLDER)
        public final static String WAR_DIRECTORY = "War Directory";
    }
}
