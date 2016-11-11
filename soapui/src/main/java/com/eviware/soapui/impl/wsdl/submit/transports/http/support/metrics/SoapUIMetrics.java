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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.metrics;

import com.eviware.soapui.support.DateUtil;
import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.io.HttpTransportMetrics;

import java.util.Date;

public class SoapUIMetrics extends HttpConnectionMetricsImpl {
    private long timestamp = -1;
    private int httpStatus = -1;
    private long contentLength = -1;

    private String httpMethod = "";
    private String ipAddress = "";
    private int port = -1;

    private final Stopwatch readTimer;
    private final Stopwatch totalTimer;
    private final Stopwatch DNSTimer;
    private final Stopwatch connectTimer;
    private final Stopwatch timeToFirstByteTimer;

    private boolean done = false;

    public SoapUIMetrics(final HttpTransportMetrics inTransportMetric, final HttpTransportMetrics outTransportMetric) {
        super(inTransportMetric, outTransportMetric);
        readTimer = new NanoStopwatch();
        totalTimer = new NanoStopwatch();
        DNSTimer = new NanoStopwatch();
        connectTimer = new NanoStopwatch();
        timeToFirstByteTimer = new NanoStopwatch();
    }

    public void reset() {
        readTimer.reset();
        totalTimer.reset();
        DNSTimer.reset();
        connectTimer.reset();
        timeToFirstByteTimer.reset();

        httpStatus = -1;
        contentLength = -1;

        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public static String formatTimestamp(long timestamp) {
        return DateUtil.formatFull(new Date(timestamp));
    }

    public Stopwatch getDNSTimer() {
        return DNSTimer;
    }

    public Stopwatch getTimeToFirstByteTimer() {
        return timeToFirstByteTimer;
    }

    public Stopwatch getConnectTimer() {
        return connectTimer;
    }

    public Stopwatch getReadTimer() {
        return readTimer;
    }

    public Stopwatch getTotalTimer() {
        return totalTimer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimeStamp() {
        return DateUtil.formatFull(new Date(getTimestamp()));
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port, int defaultPort) {
        if (port != -1) {
            this.port = port;
        } else {
            this.port = defaultPort;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("timestamp:").append(getFormattedTimeStamp()).append(";status:").append(getHttpStatus())
                .append(";length:").append(getContentLength()).append(";DNS time:")
                .append(getDNSTimer().getDuration()).append(" ms;connect time:")
                .append(getConnectTimer().getDuration()).append(" ms;time to first byte:")
                .append(getTimeToFirstByteTimer().getDuration()).append(" ms;read time:")
                .append(getReadTimer().getDuration()).append(" ms;total time:").append(getTotalTimer().getDuration());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SoapUIMetrics)) {
            return false;
        }
        SoapUIMetrics that = (SoapUIMetrics) o;

        return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
