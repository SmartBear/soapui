package com.smartbear.swagger;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import java.io.File;

public class AddSwaggerAction extends AbstractSoapUIAction<ModelItem> {
    public static final String DEFAULT_MEDIA_TYPE = "application/json";

    private XFormDialog dialog;

    public AddSwaggerAction() {
        super("Import Swagger/OpenAPI Definition", "Imports a Swagger/OpenAPI definition into SoapUI");
    }

    @Override
    public void perform(ModelItem modelItem, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class, null, null);
            dialog.setValue(Form.DEFAULT_MEDIA_TYPE, DEFAULT_MEDIA_TYPE);
        } else {
            dialog.setValue(Form.SWAGGER_URL, "");
        }

        while (dialog.show()) {
            try {
                // get the specified URL
                String url = dialog.getValue(Form.SWAGGER_URL).trim();
                if (StringUtils.hasContent(url)) {
                    WsdlProject wsdlProject = (WsdlProject) ModelSupport.getModelItemProject(modelItem);

                    // expand any property-expansions
                    String expUrl = PathUtils.expandPath(url, wsdlProject);

                    // if this is a file - convert it to a file URL
                    if (new File(expUrl).exists()) {
                        expUrl = new File(expUrl).toURI().toURL().toString();
                    }

                    importSwaggerDefinition(wsdlProject, expUrl, dialog.getValue(Form.DEFAULT_MEDIA_TYPE));
                    break;
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    public SwaggerImporter importSwaggerDefinition(final WsdlProject project,
                                                   final String definitionUrl,
                                                   final String defaultMediaType) throws Exception {
        SwaggerImporter importer = SwaggerUtils.importSwaggerFromUrl(
                project, definitionUrl, defaultMediaType);
        return importer;
    }

    @AForm(name = "Add Swagger/OpenAPI Definition", description = "Creates a REST API from the specified Swagger/OpenAPI definition")
    public interface Form {
        @AField(name = "Swagger/OpenAPI Definition", description = "Location or URL of Swagger/OpenAPI definition", type = AFieldType.FILE)
        String SWAGGER_URL = "Swagger/OpenAPI Definition";

        @AField(name = "Default Media Type", description = "Default Media Type of the responses", type = AFieldType.STRING)
        String DEFAULT_MEDIA_TYPE = "Default Media Type";
    }
}
