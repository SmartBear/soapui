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

import com.eviware.soapui.support.UIUtils;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Lars Höidahl
 */
public class SwingUtils implements UIUtils {
    public void invokeLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

    public void invokeAndWait(Runnable runnable) throws Exception {
        SwingUtilities.invokeAndWait(runnable);
    }

    // TODO Change this to run in the UI thread on Swing too, and then rename the
    // function to "runInUIThread".
    public void runInUIThreadIfSWT(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void invokeAndWaitIfNotInEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
