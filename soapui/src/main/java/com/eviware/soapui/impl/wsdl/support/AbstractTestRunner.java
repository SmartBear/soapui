/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 *
 * @author Ole.Matzura
 */

public abstract class AbstractTestRunner<T extends TestRunnable, T2 extends TestRunContext> implements Runnable,
        TestRunner {
    private final T testRunnable;
    private Status status;
    private Throwable error;
    private T2 runContext;
    private long startTime;
    private String reason;
    private volatile Future<?> future;
    private int id;
    private final static Logger log = LogManager.getLogger(AbstractTestRunner.class);

    private static int idCounter = 0;

    private Timer timeoutTimer;
    private TimeoutTimerTask timeoutTimerTask;
    private Thread thread;
    private long timeTaken;

    public AbstractTestRunner(T modelItem, StringToObjectMap properties) {
        this.testRunnable = modelItem;
        status = Status.INITIALIZED;
        id = ++idCounter;

        runContext = createContext(properties);
    }

    public abstract T2 createContext(StringToObjectMap properties);

    public T2 getRunContext() {
        return runContext;
    }

    public void start(boolean async) {
        status = Status.RUNNING;
        if (async) {
            future = SoapUI.getThreadPool().submit(this);
        } else {
            run();
        }
    }

    public void cancel(String reason) {
        if (status == Status.CANCELED || status == Status.FINISHED || status == Status.FAILED || runContext == null) {
            return;
        }
        onCancel(reason);
        status = Status.CANCELED;
        this.reason = reason;
    }

    protected void onCancel(String reason2) {
    }

    public void fail(String reason) {
        if (status == Status.CANCELED || status == Status.FAILED || runContext == null) {
            return;
        }
        onFail(reason);
        status = Status.FAILED;
        this.reason = reason;
    }

    protected void onFail(String reason) {
    }

    public Status getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public Thread getThread() {
        return thread;
    }

    public void run() {
        if (future != null) {
            thread = Thread.currentThread();
            if (System.getProperty("soapui.enablenamedthreads") != null) {
                thread.setName("TestRunner Thread for " + testRunnable.getName());
            }
        }

        try {
            status = Status.RUNNING;
            setStartTime();

            internalRun(runContext);
        } catch (Throwable t) {
            log.error("Exception during Test Execution", t);

            if (t instanceof OutOfMemoryError && UISupport.confirm("Exit now without saving?", "Out of Memory Error")) {
                System.exit(0);
            }

            status = Status.FAILED;
            error = t;
            reason = t.toString();
        } finally {
            setTimeTaken();
            if (timeoutTimer != null) {
                timeoutTimer.cancel();
            }

            if (status == Status.RUNNING) {
                status = Status.FINISHED;
            }

            internalFinally(runContext);
        }
    }

    protected void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public boolean isRunning() {
        return getStatus() == Status.RUNNING;
    }

    public boolean isCanceled() {
        return getStatus() == Status.CANCELED;
    }

    public boolean isFailed() {
        return getStatus() == Status.FAILED;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    protected void setError(Throwable error) {
        this.error = error;
    }

    protected abstract void internalRun(T2 runContext2) throws Exception;

    protected abstract void internalFinally(T2 runContext2);

    protected void startTimeoutTimer(long timeout) {
        timeoutTimer = new Timer();
        timeoutTimerTask = new TimeoutTimerTask();
        timeoutTimer.schedule(timeoutTimerTask, timeout);
    }

    public T getTestRunnable() {
        return testRunnable;
    }

    public synchronized Status waitUntilFinished() {
        if (future != null) {
            if (!future.isDone()) {
                try {
                    future.get();
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        } else {
            throw new RuntimeException("cannot wait on null future");
        }

        return getStatus();
    }

    protected void setTimeTaken() {
        timeTaken = System.currentTimeMillis() - startTime;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getStartTime() {
        return startTime;
    }

    public Throwable getError() {
        return error;
    }

    public String getReason() {
        return reason == null ? error == null ? null : error.toString() : reason;
    }

    private final class TimeoutTimerTask extends TimerTask {
        @Override
        public void run() {
            fail("TestCase timed out");
        }
    }

}
