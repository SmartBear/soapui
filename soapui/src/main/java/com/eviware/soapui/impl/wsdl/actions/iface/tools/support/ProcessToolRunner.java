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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;

import java.io.InputStream;

/**
 * ToolRunner for running command-line processes
 *
 * @author ole.matzura
 */

public class ProcessToolRunner implements ToolRunner {

    private final ProcessBuilder[] builders;
    private boolean running;
    private Process process;
    private RunnerContext context;
    private final String name;
    private final ModelItem modelItem;
    private boolean canCancel = true;
    private boolean showLog = true;
    private ArgumentBuilder args;

    public ProcessToolRunner(ProcessBuilder builder, String name, ModelItem modelItem, ArgumentBuilder args) {
        this(new ProcessBuilder[]{builder}, name, modelItem, args);
    }

    public ProcessToolRunner(ProcessBuilder[] builders, String name, ModelItem modelItem, ArgumentBuilder args) {
        this.builders = builders;
        this.name = name;
        this.modelItem = modelItem;
        this.args = args;
    }

    public ProcessToolRunner(ProcessBuilder[] processBuilders, String s, ModelItem modelItem) {
        this(processBuilders, s, modelItem, null);
    }

    public ProcessToolRunner(ProcessBuilder builder, String s, ModelItem modelItem) {
        this(builder, s, modelItem, null);
    }

    public ProcessBuilder[] getBuilders() {
        return builders;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel() {
        getProcess().destroy();
        try {
            getProcess().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        running = false;
    }

    public void run() {
        try {
            int exitCode = -1;

            beforeRun(context);

            for (int c = 0; c < builders.length; c++) {
                beforeProcess(builders[c], context);

                logRunInfo(builders[c]);
                process = builders[c].start();
                if (c == 0) {
                    context.setStatus(RunnerContext.RunnerStatus.RUNNING);
                }

                running = true;

                InputStream in = process.getInputStream();
                InputStream err = process.getErrorStream();

                exitCode = -1;

                while (exitCode == -1 && running) {
                    try {
                        exitCode = process.exitValue();
                        break;
                    } catch (IllegalThreadStateException e) {
                    } finally {
                        while (in.available() > 0) {
                            byte[] data = new byte[in.available()];
                            in.read(data);

                            context.log(new String(data));
                        }

                        while (err.available() > 0) {
                            byte[] data = new byte[err.available()];
                            err.read(data);

                            context.logError(new String(data));
                        }
                    }

                    Thread.sleep(25);
                }

                afterProcess(process, context);
            }

            context.setStatus(RunnerContext.RunnerStatus.FINISHED);

            if (running) {
                running = false;
                afterRun(exitCode, context);
            }
        } catch (Exception ex) {
            context.setStatus(RunnerContext.RunnerStatus.ERROR);
            UISupport.showErrorMessage(ex);
            running = false;
            afterRun(-1, context);
        } finally {
            context.disposeContext();
        }
    }

    protected void beforeRun(RunnerContext context) {
    }

    protected void beforeProcess(ProcessBuilder processBuilder, RunnerContext context) {
    }

    protected void afterProcess(Process process2, RunnerContext context) {
    }

    protected void afterRun(int exitCode, RunnerContext context) {
        if (exitCode == 0) {
            UISupport.showInfoMessage("Execution finished successfully", context.getTitle());
        } else {
            UISupport.showInfoMessage("Execution finished with errorCode " + exitCode
                    + ",\r\nplease check log for error messages", context.getTitle());
        }
    }

    private void logRunInfo(ProcessBuilder builder) {
        context.log("directory: " + builder.directory().getAbsolutePath() + "\r\n");
        context.log("command: " + (args == null ? builder.command() : args) + "\r\n");
    }

    public void setContext(RunnerContext context) {
        this.context = context;
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public String getName() {
        return name;
    }

    public boolean canCancel() {
        return canCancel;
    }

    public boolean showLog() {
        return showLog;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public String getDescription() {
        return null;
    }
}
