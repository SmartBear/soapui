/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpMethodSupport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Extended PostMethod that supports limiting of response size and detailed
 * timestamps
 *
 * @author Ole.Matzura
 */

public final class ExtendedHeadMethod extends HttpHead implements ExtendedHttpMethod {
    private HttpMethodSupport httpMethodSupport;

    public ExtendedHeadMethod() {
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

    @Override
    public String getResponseCharSet() {
        return httpMethodSupport.getResponseCharset();
    }

    public HttpEntity getRequestEntity() {
        return null;
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

    public SSLInfo getSSLInfo() {
        return httpMethodSupport.getSSLInfo();
    }

    public String getResponseContentType() {
        return httpMethodSupport.getResponseContentType();
    }

    public String getMethod() {
        return RestRequestInterface.HttpMethod.HEAD.toString();
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

    public byte[] getResponseBody() throws IOException {
        return httpMethodSupport.getResponseBody();
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
