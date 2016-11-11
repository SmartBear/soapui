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

package com.eviware.soapui.monitor;

import com.eviware.soapui.SoapUI;
import org.mortbay.thread.ThreadPool;

import java.util.concurrent.TimeUnit;

public final class SoapUIJettyThreadPool implements ThreadPool {
    @Override
    public boolean dispatch(Runnable arg0) {
        SoapUI.getThreadPool().execute(arg0);
        return true;
    }

    @Override
    public int getIdleThreads() {
        return 0;
    }

    @Override
    public int getThreads() {
        return SoapUI.getThreadPool().getActiveCount();
    }

    @Override
    public boolean isLowOnThreads() {
        return false;
    }

    @Override
    public void join() throws InterruptedException {
        SoapUI.getThreadPool().awaitTermination(30, TimeUnit.SECONDS);
    }
}
