package com.eviware.soapui.impl.rest.actions.explorer;

import java.util.Map;

public class RequestInspectionData {
    private final Map<String, String> headers;
    private final String requestBody;

    /**
     * Main constructor
     * @param headers request headers
     * @param requestBody request payload
     */
    public RequestInspectionData(Map<String, String> headers, String requestBody) {
        this.headers = headers;
        this.requestBody = requestBody;
    }

    /**
     * Getter for request headers
     * @return headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Getter for request payload
     * @return request body
     */
    public String getRequestBody() {
        return requestBody;
    }
}
