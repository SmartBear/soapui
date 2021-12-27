package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.ModelSupport;
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
import com.eviware.x.impl.swing.FileFormField;
import com.smartbear.analytics.Analytics;

import java.io.File;
import java.util.List;

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

    private static final String SWAGGER_2_0 = "Swagger 2.0";
    private static final String OPEN_API_3_0 = "OpenAPI 3.0";

    private XFormDialog dialog;

    public ExportSwaggerAction() {
        super("Export Swagger/OpenAPI Definition", "Creates a Swagger/OpenAPI definition for selected REST APIs");
    }

    @Override
    public void perform(WsdlProject project, Object param) {
        if (project.getInterfaces(RestServiceFactory.REST_TYPE).isEmpty() &&
                project.getInterfaces(RestServiceFactory.REST_TYPE).isEmpty()) {
            UISupport.showErrorMessage("Project is missing REST APIs");
            return;
        }

        // initialize form
        XmlBeansSettingsImpl settings = project.getSettings();
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);

            dialog.setValue(Form.FORMAT, settings.getString(FORMAT, "json"));
            dialog.setValue(Form.VERSION, settings.getString(VERSION, SWAGGER_2_0));
            dialog.setValue(Form.BASEPATH, settings.getString(BASE_PATH, ""));
            String version = settings.getString(SWAGGER_VERSION, OPEN_API_3_0);
            dialog.setValue(Form.SWAGGER_VERSION, version);

            FileFormField fileFormField = (FileFormField) dialog.getFormField(Form.FOLDER);
        }

        XFormOptionsField apis = (XFormOptionsField) dialog.getFormField(Form.APIS);
        List<AbstractInterface<?>> restServices = project.getInterfaces(RestServiceFactory.REST_TYPE);
        restServices.addAll(project.getInterfaces(RestServiceFactory.REST_TYPE));
        apis.setOptions(ModelSupport.getNames(restServices));

        while (dialog.show()) {
            try {
                Object[] options = ((XFormOptionsField) dialog.getFormField(Form.APIS)).getSelectedOptions();
                if (options.length == 0) {
                    throw new Exception("You must select at least one REST API ");
                }

                RestService[] services = new RestService[options.length];
                for (int c = 0; c < options.length; c++) {
                    services[c] = (RestService) project.getInterfaceByName(String.valueOf(options[c]));
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

                String version = dialog.getValue(Form.VERSION);
                if (StringUtils.isNullOrEmpty(version)) {
                    version = "1.0";
                }

                SwaggerExporter exporter = null;
                String target = null;

                if (swaggerVersion.equals(SWAGGER_2_0)) {
                    exporter = new Swagger2Exporter(project);
                    target = dialog.getValue(Form.FOLDER);
                }

                //temp condition
                if(exporter == null) {
                    return;
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

                Analytics.trackAction("ExportSwagger",
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

    @AForm(name = "Export Swagger Definition", description = "Creates a Swagger definition for selected REST APIs in this project")
    public interface Form {
        @AField(name = "APIs", description = "Select which REST APIs to include in the Swagger definition", type = AFieldType.MULTILIST)
        public final static String APIS = "APIs";

        @AField(name = "Target Folder", description = "Where to save the Swagger definition", type = AFieldType.FOLDER)
        public final static String FOLDER = "Target Folder";

        @AField(name = "API Version", description = "API Version", type = AFieldType.STRING)
        public final static String VERSION = "API Version";

        @AField(name = "Base Path", description = "Base Path that the Swagger definition will be hosted on", type = AFieldType.STRING)
        public final static String BASEPATH = "Base Path";

        @AField(name = "Swagger Version", description = "Select Swagger version", type = AFieldType.RADIOGROUP, values = {SWAGGER_2_0})
        public final static String SWAGGER_VERSION = "Swagger Version";

        @AField(name = "Format", description = "Select Swagger format", type = AFieldType.RADIOGROUP, values = {"json", "yaml", "xml"})
        public final static String FORMAT = "Format";
    }
}
