package com.eviware.soapui.impl.rest.actions.explorer.callback;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.actions.explorer.RequestInspectionData;
import com.eviware.soapui.impl.rest.actions.method.SaveRequestAction;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpPatch;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpDeleteWithBody;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;

public class EndpointExplorerCallback {

    public static final String CALLBACK = "inspectorCallback";

    private static final String METHOD_PROPERTY = "method";
    private static final String URL_PROPERTY = "url";
    private static final String PAYLOAD_PROPERTY = "payload";
    private static final String HEADERS_PROPERTY = "headers";

    //private final QuickCreateFunctionalTestAction createFunctionalTestAction = new QuickCreateFunctionalTestAction();


    public RestURIParser getUrlParser(String url) {
        if (StringUtils.hasContent(url)) {
            try {
                return new RestURIParserImpl(url);
            } catch (MalformedURLException e) {
                //Logging.logError(e);
                return null;
            }
        }
        return null;
    }

    public void createFromInspection(String json) {
        JSONObject request;
        try {
            request = new JSONObject(json);
        } catch (JSONException e) {
            //Logging.logError(e);
            return;
        }

        String url = extractUrl(request);
        if (StringUtils.isNullOrEmpty(url)) {
            return;
        }
        HttpMethod method = HttpMethod.valueOf(extractMethod(request));
        RequestInspectionData inspectionData = new RequestInspectionData(extractHeaders(request), extractPayload(request));

        SwingUtilities.invokeLater(() -> {
            HashMap<String, Object> context = new HashMap<>();
            context.put("URLs", Arrays.asList(url));
            context.put("Methods", Arrays.asList(method));
            context.put("InspectionData", Arrays.asList(inspectionData));
            SaveRequestAction saveRequestAction = new SaveRequestAction(context);
            saveRequestAction.showNewRestRequestDialog();
        });
    }

    public String sendRequest(String json) {
        // Analytics.trackAction(EXPLORE_API_CLICK_SEND);

        String url = "";
        String method = "";
        HashMap<String, String> headersMap = null;
        String payload = "";

        try {
            JSONObject request = new JSONObject(json);
            url = extractUrl(request);
            method = extractMethod(request);
            headersMap = extractHeaders(request);
            payload = extractPayload(request);
        } catch (JSONException e) {
            //Logging.logError(e);
        }

        try {
            switch (method) {
                case "GET": {
                    HttpGet httpGet = new HttpGet(url);
                    setHeaders(httpGet, headersMap);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpGet);

                    return getResponseAsString(response);
                }
                case "POST": {
                    HttpPost httpPost = new HttpPost(url);
                    setHeadersAndPayload(httpPost, headersMap, payload);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpPost);

                    return getResponseAsString(response);
                }
                case "PUT": {
                    HttpPut httpPut = new HttpPut(url);
                    setHeadersAndPayload(httpPut, headersMap, payload);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpPut);

                    return getResponseAsString(response);
                }
                case "DELETE": {
                    HttpDeleteWithBody httpDeleteWithBody = new HttpDeleteWithBody(url);
                    setHeadersAndPayload(httpDeleteWithBody, headersMap, payload);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpDeleteWithBody);

                    return getResponseAsString(response);
                }
                case "HEAD": {
                    HttpHead httpHead = new HttpHead(url);
                    setHeaders(httpHead, headersMap);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpHead);

                    return getResponseAsString(response);
                }
                case "OPTIONS": {
                    HttpOptions httpOptions = new HttpOptions(url);
                    setHeaders(httpOptions, headersMap);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpOptions);

                    return getResponseAsString(response);
                }
                case "TRACE": {
                    HttpTrace httpTrace = new HttpTrace(url);
                    setHeaders(httpTrace, headersMap);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpTrace);

                    return getResponseAsString(response);
                }
                case "PATCH": {
                    HttpPatch httpPatch = new HttpPatch(url);
                    setHeadersAndPayload(httpPatch, headersMap, payload);
                    HttpResponse response = HttpClientSupport.getHttpClient().execute(httpPatch);

                    return getResponseAsString(response);
                }
                default:
                    return "Unsupported method";
            }
        } catch (Exception e) {
            //Logging.logError(e);
            if (StringUtils.hasContent(e.getMessage())) {
                return e.getMessage();
            } else {
                return e.getCause().getMessage();
            }
        }
    }

    public void exploreAPIAddHeader() {
        // Analytics.trackAction(EXPLORE_API_ADD_HEADER);
    }

    public void exploreAPIchangeHTTPMethod(String changeMethodTo) {
        //   Analytics.trackAction(EXPLORE_API_CHANGE_HTTP_METHOD, "ChangeMethodTo", changeMethodTo);
    }

    public void exploreAPIClickAuthHeadersTab() {
        //  Analytics.trackAction(EXPLORE_API_CLICK_AUTH_HEADERS_TAB);
    }

    public void exploreAPIClickBodyTab() {
        //   Analytics.trackAction(EXPLORE_API_CLICK_BODY_TAB);
    }

    private String extractUrl(JSONObject request) {
        try {
            if (request.getString(URL_PROPERTY) != null) {
                return request.getString(URL_PROPERTY);
            }
        } catch (JSONException e) {
            return "";
        }
        return "";
    }

    private String extractMethod(JSONObject request) {
        try {
            if (request.getString(METHOD_PROPERTY) != null) {
                return request.getString(METHOD_PROPERTY);
            }
        } catch (JSONException e) {
            return "";
        }
        return "";
    }

    private String extractPayload(JSONObject request) {
        try {
            if (request.getString(PAYLOAD_PROPERTY) != null) {
                return request.getString(PAYLOAD_PROPERTY);
            }
        } catch (JSONException e) {
            return "";
        }
        return "";
    }

    private String getResponseAsString(HttpResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append(response.getStatusLine().toString());
        builder.append("\r\n");
        for (Header header : response.getAllHeaders()) {
            builder.append(header.getName());
            builder.append("=");
            builder.append(header.getValue());
            builder.append("\r\n");
        }
        builder.append("\r\n");
        if (response.getEntity() != null) {
            try {
                builder.append(EntityUtils.toString(response.getEntity()));
            } catch (IOException ignore) {
            }
        }
        return builder.toString();
    }

    private HashMap extractHeaders(JSONObject request) {
        HashMap<String, String> headersMap = new HashMap();
        try {
            if (request.getJSONArray(HEADERS_PROPERTY) != null) {
                JSONArray headersArray = request.getJSONArray(HEADERS_PROPERTY);
                for (int i = 0; i < headersArray.length(); i++) {
                    JSONArray headerGroup = (JSONArray) headersArray.get(i);
                    String headerName = (String) headerGroup.get(0);
                    if (StringUtils.hasContent(headerName)) {
                        String headerValue = (String) headerGroup.get(1);
                        headersMap.put(headerName, headerValue);
                    }
                }
            }
        } catch (JSONException e) {
            return headersMap;
        }
        return headersMap;
    }

    private void setHeaders(AbstractHttpMessage message, HashMap<String, String> headersMap) {
        headersMap.entrySet().stream().filter(entry -> StringUtils.hasContent(entry.getKey())).forEach(entry -> {
            message.addHeader(entry.getKey(), entry.getValue());
        });
    }

    private void setHeadersAndPayload(HttpEntityEnclosingRequestBase request, HashMap<String, String> headersMap, String payload) {
        setHeaders(request, headersMap);
        request.setEntity(new ByteArrayEntity(payload.getBytes()));
    }

    public void startSaveRequestAction() {

    }
}
