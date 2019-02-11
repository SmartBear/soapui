package com.eviware.soapui.impl.rest.actions.explorer.callback;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.actions.explorer.RequestInspectionData;
import com.eviware.soapui.impl.rest.actions.method.SaveRequestAction;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpPatch;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpCopyMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpDeleteWithBody;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpLockMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpPropFindMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpPurgeMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.HttpUnlockMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_ADD_HEADER;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_CHANGE_HTTP_METHOD;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_CLICK_AUTH_HEADERS_TAB;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_CLICK_BODY_TAB;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_CLICK_SAVE_REQUEST;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_CLICK_SEND;
import static com.eviware.soapui.analytics.SoapUIActions.EXPLORE_API_DONT_SHOW_ON_LAUNCH;
import static com.eviware.soapui.settings.UISettings.SHOW_ENDPOINT_EXPLORER_ON_START;

public class EndpointExplorerCallback {

    public static final String CALLBACK = "inspectorCallback";

    private static final String METHOD_PROPERTY = "method";
    private static final String URL_PROPERTY = "url";
    private static final String PAYLOAD_PROPERTY = "payload";
    private static final String HEADERS_PROPERTY = "headers";

    private final WebViewBasedBrowserComponent browserComponent;

    private boolean requestCreated = false;

    public EndpointExplorerCallback(WebViewBasedBrowserComponent browserComponent){
        this.browserComponent = browserComponent;
    }

    private static final String UNKNOWN_HOST_EXCEPTION_RESPONSE_TEXT = "<missing raw response data>";

    public RestURIParser getUrlParser(String url) {
        if (StringUtils.hasContent(url)) {
            try {
                return new RestURIParserImpl(url);
            } catch (MalformedURLException e) {
                SoapUI.logError(e);
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
            SoapUI.logError(e);
            return;
        }
        Analytics.trackAction(EXPLORE_API_CLICK_SAVE_REQUEST,
                "HTTPMethod", extractMethod(request),
                "Endpoint", extractUrl(request));

        String url = extractUrl(request);
        if (StringUtils.isNullOrEmpty(url)) {
            return;
        }
        HttpMethod method = HttpMethod.valueOf(extractMethod(request));
        RequestInspectionData inspectionData = new RequestInspectionData(extractHeaders(request), extractPayload(request));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> context = new HashMap<>();
                context.put("URLs", Arrays.asList(url));
                context.put("Methods", Arrays.asList(method));
                context.put("InspectionData", Arrays.asList(inspectionData));
                SaveRequestAction saveRequestAction = new SaveRequestAction(context);
                requestCreated = saveRequestAction.showNewRestRequestDialog();
                browserComponent.executeJavaScript(String.format("window.closeHandler(%s)", requestCreated));
            }
        });
    }

    private static String sendRequest(HttpUriRequest httpUriRequest) throws IOException {
        HttpResponse response = HttpClientSupport.getHttpClient().execute(httpUriRequest);
        return getResponseAsString(response);
    }

    public String sendRequest(String json) {
        Analytics.trackAction(EXPLORE_API_CLICK_SEND);

        String url = "";
        String method = "";
        Map<String, String> headersMap = null;
        String payload = "";

        try {
            JSONObject request = new JSONObject(json);
            url = extractUrl(request);
            method = extractMethod(request);
            headersMap = extractHeaders(request);
            payload = extractPayload(request);
        } catch (JSONException e) {
            SoapUI.logError(e);
        }

        try {
            switch (method) {
                case "GET":
                    HttpGet httpGet = new HttpGet(url);
                    setHeaders(httpGet, headersMap);
                    return sendRequest(httpGet);
                case "POST":
                    HttpPost httpPost = new HttpPost(url);
                    setHeadersAndPayload(httpPost, headersMap, payload);
                    return sendRequest(httpPost);
                case "PUT":
                    HttpPut httpPut = new HttpPut(url);
                    setHeadersAndPayload(httpPut, headersMap, payload);
                    return sendRequest(httpPut);
                case "DELETE":
                    HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
                    setHeadersAndPayload(httpDelete, headersMap, payload);
                    return sendRequest(httpDelete);
                case "HEAD":
                    HttpHead httpHead = new HttpHead(url);
                    setHeaders(httpHead, headersMap);
                    return sendRequest(httpHead);
                case "OPTIONS":
                    HttpOptions httpOptions = new HttpOptions(url);
                    setHeaders(httpOptions, headersMap);
                    return sendRequest(httpOptions);
                case "TRACE":
                    HttpTrace httpTrace = new HttpTrace(url);
                    setHeaders(httpTrace, headersMap);
                    return sendRequest(httpTrace);
                case "PATCH":
                    HttpPatch httpPatch = new HttpPatch(url);
                    setHeadersAndPayload(httpPatch, headersMap, payload);
                    return sendRequest(httpPatch);
                case "PROPFIND":
                    HttpPropFindMethod httpPropFind = new HttpPropFindMethod(url);
                    setHeadersAndPayload(httpPropFind, headersMap, payload);
                    return sendRequest(httpPropFind);
                case "LOCK":
                    HttpLockMethod httpLock = new HttpLockMethod(url);
                    setHeadersAndPayload(httpLock, headersMap, payload);
                    return sendRequest(httpLock);
                case "UNLOCK":
                    HttpUnlockMethod httpUnlock = new HttpUnlockMethod(url);
                    setHeaders(httpUnlock, headersMap);
                    return sendRequest(httpUnlock);
                case "COPY":
                    HttpCopyMethod httpCopy = new HttpCopyMethod(url);
                    setHeaders(httpCopy, headersMap);
                    return sendRequest(httpCopy);
                case "PURGE":
                    HttpPurgeMethod httpPurge = new HttpPurgeMethod(url);
                    setHeaders(httpPurge, headersMap);
                    return sendRequest(httpPurge);
                default:
                    return "Unsupported method";
            }
        } catch (Exception e) {
            SoapUI.logError(e);
            if (e instanceof UnknownHostException) {
                return UNKNOWN_HOST_EXCEPTION_RESPONSE_TEXT;
            }
            if (StringUtils.hasContent(e.getMessage())) {
                return e.getMessage();
            } else {
                return e.getCause().getMessage();
            }
        }
    }

    public void exploreAPIAddHeader() {
        Analytics.trackAction(EXPLORE_API_ADD_HEADER);
    }

    public void exploreAPIchangeHTTPMethod(String changeMethodTo) {
        Analytics.trackAction(EXPLORE_API_CHANGE_HTTP_METHOD, "ChangeMethodTo", changeMethodTo);
    }

    public void exploreAPIClickAuthHeadersTab() {
        Analytics.trackAction(EXPLORE_API_CLICK_AUTH_HEADERS_TAB);
    }

    public void exploreAPIClickBodyTab() {
        Analytics.trackAction(EXPLORE_API_CLICK_BODY_TAB);
    }

    public void exploreAPIDontShowAgain(boolean newValue) {
        if (newValue) {
            Analytics.trackAction(EXPLORE_API_DONT_SHOW_ON_LAUNCH);
        }
        SoapUI.getSettings().setBoolean(SHOW_ENDPOINT_EXPLORER_ON_START, !newValue);
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

    private static String getResponseAsString(HttpResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append(response.getStatusLine().toString());
        try {
            builder.append(StringUtils.fixLineSeparator("\r\n"));
        } catch (UnsupportedEncodingException e) {
            SoapUI.logError(e);
        }
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

    private void setHeaders(AbstractHttpMessage message, Map<String, String> headersMap) {
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            if (StringUtils.hasContent(entry.getKey())) {
                message.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setHeadersAndPayload(HttpEntityEnclosingRequestBase request, Map<String, String> headersMap, String payload) {
        setHeaders(request, headersMap);
        request.setEntity(new ByteArrayEntity(payload.getBytes()));
    }
}
