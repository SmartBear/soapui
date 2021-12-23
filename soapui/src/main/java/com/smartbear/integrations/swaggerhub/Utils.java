package com.smartbear.integrations.swaggerhub;

import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.google.common.io.ByteStreams;
import com.smartbear.analytics.Analytics;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.smartbear.analytics.AnalyticsManager.Category.CUSTOM_PLUGIN_ACTION;

public class Utils {
    public static final String GET_TOKEN_URL = "https://api.swaggerhub.com/token";

    public static void sendAnalytics(String action) {
        Map<String, String> params = new HashMap();
        params.put("SourceModule", "");
        params.put("ProductArea", "MainMenu");
        params.put("Type", "REST");
        params.put("Source", "SwaggerHub");
        Analytics.getAnalyticsManager().trackAction(CUSTOM_PLUGIN_ACTION, action, params);
    }

    public static String getApiKey(String login, String password) throws Exception {
        String jsonString = "";
        jsonString = new JSONObject()
                .put("password", password)
                .put("username", login).toString();
        HttpPost httpPost = new HttpPost(GET_TOKEN_URL);
        StringEntity params = new StringEntity(jsonString);
        httpPost.setHeader("content-type", "application/json");
        httpPost.setEntity(params);
        HttpResponse response = HttpClientSupport.getHttpClient().execute(httpPost);
        String jsonResponse = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String extractedApiKey = jsonObject.getString("token");

        if (StringUtils.isNotEmpty(extractedApiKey)) {
            return extractedApiKey;
        } else {
            throw new Exception("Cannot retrieve an API Key");
        }
    }
}
