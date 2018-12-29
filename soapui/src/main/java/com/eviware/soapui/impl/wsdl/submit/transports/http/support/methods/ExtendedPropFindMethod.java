package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedEntityEnclosingHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpMethodSupport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ExtendedPropFindMethod extends HttpPropFindMethod implements ExtendedEntityEnclosingHttpMethod {
    private final HttpMethodSupport httpMethodSupport;

    public ExtendedPropFindMethod() {
        super();
        httpMethodSupport = new HttpMethodSupport();
    }

    public ExtendedPropFindMethod(String url) {
        super(url);
        httpMethodSupport = new HttpMethodSupport();
    }

    public String getDumpFile() {
        return httpMethodSupport.getDumpFile();
    }

    public void setDumpFile(String dumpFile) {
        httpMethodSupport.setDumpFile(dumpFile);
    }

    public boolean hasResponse() {
        return httpMethodSupport.hasResponse();
    }

    public void afterReadResponse(SSLSession session) {
        httpMethodSupport.afterReadResponse(session);
    }

    public String getResponseCharSet() {
        return httpMethodSupport.getResponseCharset();
    }

    public HttpEntity getRequestEntity() {
        return super.getEntity();
    }

    public long getMaxSize() {
        return httpMethodSupport.getMaxSize();
    }

    public void setMaxSize(long maxSize) {
        httpMethodSupport.setMaxSize(maxSize);
    }

    public long getResponseReadTime() {
        return httpMethodSupport.getResponseReadTime();
    }

    public long getResponseReadTimeNanos() {
        return httpMethodSupport.getResponseReadTimeNanos();
    }

    public void afterWriteRequest() {
        httpMethodSupport.afterWriteRequest();
    }

    public void initStartTime() {
        httpMethodSupport.initStartTime();
    }

    public long getTimeTaken() {
        return httpMethodSupport.getTimeTaken();
    }

    public long getStartTime() {
        return httpMethodSupport.getStartTime();
    }

    public byte[] getResponseBody() throws IOException {
        return httpMethodSupport.getResponseBody();
    }

    public SSLInfo getSSLInfo() {
        return httpMethodSupport.getSSLInfo();
    }

    public String getResponseContentType() {
        return httpMethodSupport.getResponseContentType();
    }

    public Throwable getFailureCause() {
        return httpMethodSupport.getFailureCause();
    }

    public boolean isFailed() {
        return httpMethodSupport.isFailed();
    }

    public void setFailed(Throwable t) {
        httpMethodSupport.setFailed(t);
    }

    public byte[] getDecompressedResponseBody() throws IOException {
        return httpMethodSupport.getDecompressedResponseBody();
    }

    public void setDecompress(boolean decompress) {
        httpMethodSupport.setDecompress(decompress);
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        httpMethodSupport.setHttpResponse(httpResponse);
    }

    public HttpResponse getHttpResponse() {
        return httpMethodSupport.getHttpResponse();
    }

    public boolean hasHttpResponse() {
        return httpMethodSupport.hasHttpResponse();
    }

    public String getResponseBodyAsString() throws IOException {
        byte[] rawdata = getResponseBody();
        if (rawdata != null) {
            return EncodingUtil.getString(rawdata, getResponseCharSet());
        } else {
            return null;
        }
    }

    public SoapUIMetrics getMetrics() {
        return httpMethodSupport.getMetrics();
    }

    @Override
    public Header[] getAllResponseHeaders() {
        return httpMethodSupport.getAllResponseHeaders();
    }

    @Override
    public URL getURL() throws MalformedURLException {
        return getURI().toURL();
    }
}
