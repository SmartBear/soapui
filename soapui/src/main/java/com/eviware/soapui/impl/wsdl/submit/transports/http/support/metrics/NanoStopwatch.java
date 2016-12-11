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

public class NanoStopwatch implements Stopwatch {

    protected long start;

    protected long stop;

    @Override
    public long getDuration() {
        long nanoTime = stop - start;
        // removing time differences by excluding rounding errors
        // so we collect data in milliseconds
        long msTime = nanoTime;/// 1000000;
        return msTime;
    }

    @Override
    public void start() {
        start = getCurrentTime();
    }

    @Override
    public void stop() {
        stop = getCurrentTime();
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }

    @Override
    public void reset() {
        start = 0;
        stop = 0;
    }

    protected long getCurrentTime() {
        // return System.nanoTime();
        return System.currentTimeMillis();
    }

    @Override
    public void add(long value) {
        stop += value;
    }

    public boolean isStarted() {
        return getStart() > 0;
    }

    public boolean isStopped() {
        return getStop() > 0;
    }

    public void set(long start, long stop) {
        this.start = start;
        this.stop = stop;
    }

}
