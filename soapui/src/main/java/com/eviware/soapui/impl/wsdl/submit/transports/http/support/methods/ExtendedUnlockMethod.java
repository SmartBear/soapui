package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
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

public class ExtendedUnlockMethod extends HttpUnlockMethod implements ExtendedHttpMethod {
    private final HttpMethodSupport httpMethodSupport;

    public ExtendedUnlockMethod() {
        httpMethodSupport = new HttpMethodSupport();
    }

    public ExtendedUnlockMethod(String url) {
        super(url);
        httpMethodSupport = new HttpMethodSupport();
    }

    @Override
    public long getMaxSize() {
        return httpMethodSupport.getMaxSize();
    }

    @Override
    public void setMaxSize(long maxSize) {
        httpMethodSupport.setMaxSize(maxSize);
    }

    @Override
    public long getResponseReadTime() {
        return httpMethodSupport.getResponseReadTime();
    }

    @Override
    public long getResponseReadTimeNanos() {
        return httpMethodSupport.getResponseReadTimeNanos();
    }

    @Override
    public void initStartTime() {
        httpMethodSupport.initStartTime();
    }

    @Override
    public long getTimeTaken() {
        return httpMethodSupport.getTimeTaken();
    }

    @Override
    public long getStartTime() {
        return httpMethodSupport.getStartTime();
    }

    @Override
    public SSLInfo getSSLInfo() {
        return httpMethodSupport.getSSLInfo();
    }

    @Override
    public String getResponseCharSet() {
        return httpMethodSupport.getResponseCharset();
    }

    @Override
    public String getResponseContentType() {
        return httpMethodSupport.getResponseContentType();
    }

    @Override
    public void setDumpFile(String dumpFile) {
        httpMethodSupport.setDumpFile(dumpFile);
    }

    @Override
    public void setFailed(Throwable t) {
        httpMethodSupport.setFailed(t);
    }

    @Override
    public boolean isFailed() {
        return httpMethodSupport.isFailed();
    }

    @Override
    public Throwable getFailureCause() {
        return httpMethodSupport.getFailureCause();
    }

    @Override
    public boolean hasResponse() {
        return httpMethodSupport.hasResponse();
    }

    @Override
    public byte[] getDecompressedResponseBody() throws IOException {
        return httpMethodSupport.getDecompressedResponseBody();
    }

    @Override
    public void setDecompress(boolean decompress) {
        httpMethodSupport.setDecompress(decompress);
    }

    @Override
    public void setHttpResponse(HttpResponse httpResponse) {
        httpMethodSupport.setHttpResponse(httpResponse);
    }

    @Override
    public HttpResponse getHttpResponse() {
        return httpMethodSupport.getHttpResponse();
    }

    @Override
    public boolean hasHttpResponse() {
        return httpMethodSupport.hasHttpResponse();
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return httpMethodSupport.getResponseBody();
    }

    @Override
    public String getResponseBodyAsString() throws IOException {
        byte[] rawdata = getResponseBody();
        if (rawdata != null) {
            return EncodingUtil.getString(rawdata, getResponseCharSet());
        } else {
            return null;
        }
    }

    @Override
    public HttpEntity getRequestEntity() {
        return null;
    }

    @Override
    public void afterReadResponse(SSLSession session) {
        httpMethodSupport.afterReadResponse(session);
    }

    @Override
    public void afterWriteRequest() {
        httpMethodSupport.afterWriteRequest();
    }

    @Override
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

    public String getDumpFile() {
        return httpMethodSupport.getDumpFile();
    }
}

