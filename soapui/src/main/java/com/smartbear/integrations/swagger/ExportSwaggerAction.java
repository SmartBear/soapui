/**
 * Copyright 2013-2017 SmartBear Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbear.integrations.swagger;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormRadioGroup;
import com.eviware.x.impl.swing.FileFormField;
import com.eviware.x.impl.swing.JTextFieldFormField;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;


import static com.eviware.soapui.analytics.ReadyApiActions.EXPORT_DEFINITION;
import static com.eviware.soapui.support.action.ActionGroups.OPEN_PROJECT_ACTIONS;

@ActionConfiguration(targetType = WsdlProject.class, actionGroup = OPEN_PROJECT_ACTIONS, afterAction = "AddSwaggerAction")
public class ExportSwaggerAction extends AbstractSoapUIAction<WsdlProject> {
    private static final MessageSupport messages = MessageSupport.getMessages(ExportSwaggerAction.class);

    public static final String SWAGGER_EXTENSION = ".swagger";
    public static final String JSON_EXTENSION = ".json";
    public static final String YAML_EXTENSION = ".yaml";

    private static final String BASE_PATH = Form.class.getName() + Form.BASEPATH;
    private static final String TARGET_PATH = Form.class.getName() + Form.FOLDER;
    private static final String FORMAT = Form.class.getName() + Form.FORMAT;
    private static final String VERSION = Form.class.getName() + Form.VERSION;
    private static final String SWAGGER_VERSION = Form.class.getName() + Form.SWAGGER_VERSION;

    private static final String SWAGGER_1_2 = "Swagger 1.2";
    private static final String SWAGGER_2_0 = "Swagger 2.0";
    private static final String OPEN_API_3_0 = "OpenAPI 3.0";

    private XFormDialog dialog;

    public ExportSwaggerAction() {
        super("Export Swagger/OpenAPI Definition", "Creates a Swagger/OpenAPI definition for selected REST APIs");
    }

    @Override
    public void perform(WsdlProject project, Object param) {
        if (project.getInterfaces(RestServiceFactory.REST_TYPE).isEmpty() &&
                project.getInterfaces(RestServiceExFactory.REST_EX_TYPE).isEmpty()) {
            UISupport.showErrorMessage("Project is missing REST APIs");
            return;
        }

        // initialize form
        XmlBeansSettingsImpl settings = project.getSettings();
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);

            dialog.setValue(Form.FORMAT, settings.getString(FORMAT, "json"));
            dialog.setValue(Form.VERSION, settings.getString(VERSION, "1.0"));
            dialog.setValue(Form.BASEPATH, settings.getString(BASE_PATH, ""));
            String version = settings.getString(SWAGGER_VERSION, OPEN_API_3_0);
            dialog.setValue(Form.SWAGGER_VERSION, version);

            FileFormField fileFormField = (FileFormField) dialog.getFormField(Form.FILE);
            fileFormField.setDialogState(JFileChooser.SAVE_DIALOG);
            if (version.equals(SWAGGER_1_2)) {
                dialog.setValue(Form.FOLDER, settings.getString(TARGET_PATH, ""));
            } else {
                fileFormField.setCurrentFile(project.getName());
            }
        }

        XFormRadioGroup versionRadioGroup = (XFormRadioGroup) dialog.getFormField(Form.SWAGGER_VERSION);
        final JRadioButton openApiOption = versionRadioGroup.getComponentFromGroup(OPEN_API_3_0);
        hideOrShowFields(openApiOption.isSelected(), dialog);
        openApiOption.addItemListener(e -> hideOrShowFields(openApiOption.isSelected(), dialog));
        final JRadioButton api12Option = versionRadioGroup.getComponentFromGroup(SWAGGER_1_2);
        showFileOrFolderComponent(api12Option.isSelected(), dialog);
        api12Option.addItemListener(e -> showFileOrFolderComponent(api12Option.isSelected(), dialog));

        XFormRadioGroup formatRadioGroup = (XFormRadioGroup) dialog.getFormField(Form.FORMAT);
        setFileFilter(dialog.getValue(Form.FORMAT));
        formatRadioGroup.addFormFieldListener((sourceField, newValue, oldValue) -> setFileFilter(newValue));

        XFormOptionsField apis = (XFormOptionsField) dialog.getFormField(Form.APIS);
        List<AbstractInterface<?, ? extends Operation>> restServices = project.getInterfaces(RestServiceFactory.REST_TYPE);
        restServices.addAll(project.getInterfaces(RestServiceExFactory.REST_EX_TYPE));
        apis.setOptions(ModelSupport.getNames(restServices));

        while (dialog.show()) {
            try {
                Object[] options = ((XFormOptionsField) dialog.getFormField(Form.APIS)).getSelectedOptions();
                if (options.length == 0) {
                    throw new Exception("You must select at least one REST API ");
                }

                AbstractRestService[] services = new AbstractRestService[options.length];
                for (int c = 0; c < options.length; c++) {
                    services[c] = (AbstractRestService) project.getInterfaceByName(String.valueOf(options[c]));
                    if (services[c].getEndpoints().length == 0) {
                        throw new Exception("Selected APIs must contain at least one endpoint");
                    }
                }

                // double-check
                if (services.length == 0) {
                    throw new Exception("You must select at least one REST API to export");
                }

                String swaggerVersion = dialog.getValue(Form.SWAGGER_VERSION);
                String format = dialog.getValue(Form.FORMAT);

                if (format.equals("xml") && (swaggerVersion.equals(SWAGGER_2_0) || swaggerVersion.equals(OPEN_API_3_0))) {
                    throw new Exception("XML format is only supported for Swagger Version 1.2");
                }

                if (format.equals("yaml") && swaggerVersion.equals(SWAGGER_1_2)) {
                    throw new Exception("YAML format is only supported for Swagger Version 2.0 and OpenAPI 3.0");
                }

                String version = dialog.getValue(Form.VERSION);
                if (StringUtils.isNullOrEmpty(version)) {
                    version = "1.0";
                }

                SwaggerExporter exporter;
                String target;

                if (swaggerVersion.equals(SWAGGER_1_2)) {
                    exporter = new Swagger1XExporter(project);
                    target = dialog.getValue(Form.FOLDER);
                } else if (swaggerVersion.equals(SWAGGER_2_0)) {
                    exporter = new Swagger2Exporter(project);
                    target = dialog.getValue(Form.FILE);
                } else {
                    exporter = new OpenAPI3Exporter(project);
                    target = dialog.getValue(Form.FILE);
                }

                String path = exporter.exportToFileSystem(target, version, format, services, dialog.getValue(Form.BASEPATH));

                if (path == null) {
                    UISupport.showInfoMessage(messages.get("ExportSwaggerAction.InfoMessage.NothingCreated"));
                } else {
                    UISupport.showInfoMessage(String.format(messages.get("ExportSwaggerAction.InfoMessage.ListingHasBeenCreated"), path));
                }

                settings.setString(BASE_PATH, dialog.getValue(Form.BASEPATH));
                settings.setString(TARGET_PATH, dialog.getValue(Form.FOLDER));
                settings.setString(FORMAT, dialog.getValue(Form.FORMAT));
                settings.setString(VERSION, dialog.getValue(Form.VERSION));
                settings.setString(SWAGGER_VERSION, dialog.getValue(Form.SWAGGER_VERSION));

                Analytics.trackAction(EXPORT_DEFINITION,
                        "Type", "Swagger",
                        "ExportedDefinitionType", "Swagger",
                        "Version", dialog.getValue(Form.SWAGGER_VERSION),
                        "Format", dialog.getValue(Form.FORMAT));

                break;
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    public static boolean shouldOverwriteFileIfExists(String fileName, String folderName) {
        File apiFile = new File(fileName);
        if (folderName != null) {
            apiFile = new File(folderName + File.separatorChar + fileName);
        }
        if (apiFile.exists()) {
             return (UISupport.confirm(String.format(messages.get("ExportSwaggerAction.ConfirmDialog.Question"), apiFile.getName()),
                    messages.get("ExportSwaggerAction.ConfirmDialog.Title")));
        }
        return true;
    }

    private void hideOrShowFields(boolean isSelected, XFormDialog dialog) {
        JTextFieldFormField basePath = (JTextFieldFormField) dialog.getFormField(Form.BASEPATH);
        JTextFieldFormField versionField = (JTextFieldFormField) dialog.getFormField(Form.VERSION);
        basePath.setVisible(!isSelected);
        versionField.setVisible(!isSelected);
        dialog.adjustSize();
    }

    private void showFileOrFolderComponent(boolean showFolder, XFormDialog dialog) {
        dialog.getFormField(Form.FOLDER).setVisible(showFolder);
        dialog.getFormField(Form.FILE).setVisible(!showFolder);
        dialog.adjustSize();
    }

    private void setFileFilter(String format) {
        FileFormField fileFormField = (FileFormField) dialog.getFormField(Form.FILE);
        fileFormField.setFileFilter(new FileChooser.ExtensionFilter(format.toUpperCase() + " file (*." + format + ")", format));
    }

    @AForm(name = "Export Swagger/OpenAPI Definition", description = "Creates a Swagger/OpenAPI definition for selected REST APIs in this project")
    public interface Form {
        @AField(name = "APIs", description = "Select which REST APIs to include in the Swagger/OpenAPI definition", type = AFieldType.MULTILIST)
        String APIS = "APIs";

        @AField(name = "Version", description = "Select version", type = AFieldType.RADIOGROUP, values = {OPEN_API_3_0, SWAGGER_2_0, SWAGGER_1_2})
        String SWAGGER_VERSION = "Version";

        @AField(name = "Format", description = "Select format", type = AFieldType.RADIOGROUP, values = {"json", "yaml", "xml"})
        String FORMAT = "Format";

        @AField(name = "Target Path", description = "Folder to save the Swagger definition", type = AFieldType.FOLDER)
        String FOLDER = "Target Path";

        @AField(name = "Target File", description = "File to save the Swagger/OpenAPI definition", type = AFieldType.FILE)
        String FILE = "Target File";

        @AField(name = "API Version", description = "API Version", type = AFieldType.STRING)
        String VERSION = "API Version";

        @AField(name = "Base Path", description = "Base Path that the Swagger definition will be hosted on", type = AFieldType.STRING)
        String BASEPATH = "Base Path";
    }

}
