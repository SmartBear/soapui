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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics.SoapUIMetrics;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface ExtendedHttpMethod extends HttpRequest, HttpUriRequest {
    long getMaxSize();

    void setMaxSize(long maxSize);

    long getResponseReadTime();

    long getResponseReadTimeNanos();

    void initStartTime();

    long getTimeTaken();

    long getStartTime();

    SSLInfo getSSLInfo();

    String getResponseCharSet();

    String getResponseContentType();

    String getMethod();

    void setDumpFile(String dumpFile);

    void setFailed(Throwable t);

    boolean isFailed();

    Throwable getFailureCause();

    boolean hasResponse();

    byte[] getDecompressedResponseBody() throws IOException;

    void setDecompress(boolean decompress);

    void setURI(URI uri);

    void setHttpResponse(org.apache.http.HttpResponse httpResponse);

    org.apache.http.HttpResponse getHttpResponse();

    boolean hasHttpResponse();

    byte[] getResponseBody() throws IOException;

    String getResponseBodyAsString() throws IOException;

    HttpEntity getRequestEntity();

    void afterReadResponse(SSLSession session);

    void afterWriteRequest();

    SoapUIMetrics getMetrics();

    Header[] getAllResponseHeaders();

    URL getURL() throws MalformedURLException;
}
