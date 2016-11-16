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

package com.eviware.soapui.support.swing;

import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;

/**
 * @author Lars Höidahl
 */
public class SwingWorkerDelegator extends SwingWorker {
    private XProgressMonitor monitor;
    private Worker delegate;
    private XProgressDialog dialog;

    /**
     * Start a thread that will call <code>delegate.construct</code> and then
     * exit.
     */
    public SwingWorkerDelegator(XProgressMonitor monitor, XProgressDialog dialog, Worker delegate) {
        this.monitor = monitor;
        this.dialog = dialog;
        this.delegate = delegate;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */

    public Object construct() {
        return delegate.construct(monitor);
    }

    /**
     * Called on the event dispatching thread (not on the worker thread) after
     * the <code>construct</code> method has returned.
     */
    public void finished() {
        delegate.finished();
        if (dialog != null) {
            dialog.setVisible(false);
        }
        delegate = null;
        monitor = null;
        dialog = null;
    }
}
