package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.RequestContent;

public class RequestContentWrapper implements HttpRequestInterceptor {
    private RequestContent requestContent;

    RequestContentWrapper(boolean overwrite) {
        requestContent = new RequestContent(overwrite);
    }

    public void process(org.apache.http.HttpRequest request, org.apache.http.protocol.HttpContext context) throws org.apache.http.HttpException, java.io.IOException {
        requestContent.process(request, context);
        boolean removeEmptyContentLength = !Boolean.valueOf(System.getProperty("soapui.send.zero.content.length", "true"));
        if (removeEmptyContentLength) {
            for (Header header : request.getAllHeaders()) {
                if (header.getName().equals("Content-Length") && header.getValue().equals("0")) {
                    request.removeHeader(header);
                    break;
                }
            }
        }
    }
}
