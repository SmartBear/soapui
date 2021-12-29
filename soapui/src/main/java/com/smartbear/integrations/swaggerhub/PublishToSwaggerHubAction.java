package com.smartbear.integrations.swaggerhub;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.google.common.io.Files;
import com.smartbear.analytics.Analytics;
import com.smartbear.swagger.Swagger2Exporter;
import com.smartbear.swagger.SwaggerExporter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.smartbear.analytics.AnalyticsManager.Category.CUSTOM_PLUGIN_ACTION;
import static com.smartbear.integrations.swaggerhub.component.ImportFromSwaggerHubDialog.*;

public class PublishToSwaggerHubAction extends AbstractSoapUIAction<RestService> {
    private static final Logger LOG = LoggerFactory.getLogger(PublishToSwaggerHubAction.class);
    public static final String SWAGGER_HUB_API_KEY = "SwaggerHubApiKey";
    public final static String SWAGGERHUB_URL = "https://swaggerhub.com";
    public final static String SWAGGERHUB_API = "https://api.swaggerhub.com/apis";

    private XFormDialog dialog;

    public PublishToSwaggerHubAction() {
        super("Publish to SwaggerHub", "Publishes this API to SwaggerHub");
    }

    public void perform(final RestService restService, Object o) {

        Settings settings = SoapUI.getWorkspace().getSettings();
        dialog = ADialogBuilder.buildDialog(Form.class);
        dialog.setValue(Form.APIKEY, settings.getString(SWAGGER_HUB_API_KEY, ""));
        dialog.setBooleanValue(Form.REMEMBER, true);

        final boolean[] finished = {false};
        while (!finished[0] && dialog.show()) {
            XProgressDialog progressDialog = UISupport.getDialogs().createProgressDialog("Publish to SwaggerHub", 0, "Importing...", false);
            try {
                progressDialog.run(new Worker.WorkerAdapter() {
                    @Override
                    public Object construct(XProgressMonitor xProgressMonitor) {
                        try {
                            finished[0] = publishApi(restService);
                        } catch (IOException e) {
                            UISupport.showErrorMessage(e);
                        }
                        return null;
                    }
                });
            } catch (Throwable e) {
                UISupport.showErrorMessage(e);
            }
        }
    }

    private boolean publishApi(RestService restService) throws IOException {
        try {
            String apiKey = dialog.getValue(Form.APIKEY);
            String groupId = dialog.getValue(Form.GROUP_ID);
            String apiId = dialog.getValue(Form.API_ID);
            String versionId = dialog.getValue(Form.VERSION);
            boolean remember = dialog.getBooleanValue(Form.REMEMBER);

            String uri = SWAGGERHUB_API + "/" + groupId + "/" + apiId;
            CloseableHttpClient client = HttpClientSupport.getHttpClient();

            HttpGet get = new HttpGet(uri + "/" + versionId);
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                if (!UISupport.confirm("API Version [" + versionId + "] already exists at SwaggerHub - Overwrite?",
                        "Publish to SwaggerHub")) {
                    return false;
                }
            }

            SwaggerExporter exporter = new Swagger2Exporter(restService.getProject());
            String tempDirectoryPath = Files.createTempDir().getAbsolutePath();
            String tempFilePath = tempDirectoryPath + File.separator + "api-docs.json";
            String result = exporter.exportToFileSystem(tempFilePath, versionId,
                    "json", new RestService[]{restService}, restService.getBasePath());
            new File(tempDirectoryPath).deleteOnExit();

            LOG.info("Created temporary Swagger definition at " + result);

            if (remember) {
                Workspace workspace = SoapUI.getWorkspace();
                workspace.getSettings().setString(SWAGGER_HUB_API_KEY, apiKey);
            }

            HttpPost post = new HttpPost(uri + "?version=" + versionId + "&isPrivate=" + dialog.getBooleanValue(Form.PRIVATE));
            post.setEntity(new FileEntity(new File(result), "application/json"));
            post.addHeader("Authorization", apiKey);

            LOG.info("Posting definition to " + uri);
            response = client.execute(post);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201 || statusCode == 200 || statusCode == 205) {
                UISupport.showInfoMessage("API published successfully");
                sendAnalytics("ExportToSwaggerHubAction");
                return true;
            } else {
                String reason = "";
                if (statusCode == 400) {
                    reason = "The definition was invalid.";
                } else if (statusCode == 403) {
                    reason = "Maximum number of APIs reached.";
                } else if (statusCode == 409) {
                    reason = "Cannot overwrite a published API version.";
                } else if (statusCode == 415) {
                    reason = "Invalid content type.";
                }
                UISupport.showErrorMessage("Failed to publish API; " + response.getStatusLine().toString() + "; " + reason);
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    private static void sendAnalytics(String action) {
        Map<String, String> params = new HashMap();
        params.put("SourceModule", "");
        params.put("ProductArea", "MainMenu");
        params.put("Type", "REST");
        params.put("Source", "SwaggerHub");
        Analytics.getAnalyticsManager().trackAction(CUSTOM_PLUGIN_ACTION, action, params);
    }

    @AForm(name = "Publish Definition to SwaggerHub", description = "Publishes the selected REST definition to SwaggerHub (in Swagger 2.0/OpenAPI 3.0 format)")
    public interface Form {

        @AField(name = "API Key", description = "Your SwaggerHub password", type = AField.AFieldType.PASSWORD)
        public final static String APIKEY = "API Key";

        @AField(name = "Owner", description = "An API owner", type = AField.AFieldType.STRING)
        public final static String GROUP_ID = "Owner";

        @AField(name = "Unique API name", description = "The API identifier at SwaggerHub (letters, digits or spaces, 3 chars min)", type = AField.AFieldType.STRING)
        public final static String API_ID = "Unique API name";

        @AField(name = "Version", description = "The version of this API", type = AField.AFieldType.STRING)
        public final static String VERSION = "Version";

        @AField(name = "Remember credentials", description = "Save credentials for future actions", type = AField.AFieldType.BOOLEAN)
        public final static String REMEMBER = "Remember credentials";

        @AField(name = "Private", description = "Make this API private", type = AField.AFieldType.BOOLEAN)
        public final static String PRIVATE = "Private";
    }
}
