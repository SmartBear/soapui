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

import javax.swing.JFrame;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public class ModalFrameUtil {
    static class EventPump implements InvocationHandler {
        Frame frame;

        public EventPump(Frame frame) {
            this.frame = frame;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return frame.isShowing();
        }

        // when the reflection calls in this method has to be
        // replaced once Sun provides a public API to pump events.
        public void start() throws Exception {
            Class<?> clazz = Class.forName("java.awt.Conditional");
            Object conditional = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
            Method pumpMethod = Class.forName("java.awt.EventDispatchThread").getDeclaredMethod("pumpEvents",
                    new Class[]{clazz});
            pumpMethod.setAccessible(true);
            pumpMethod.invoke(Thread.currentThread(), new Object[]{conditional});
        }
    }

    // show the given frame as modal to the specified owner.
    // NOTE: this method returns only after the modal frame is closed.
    public static void showAsModal(final Frame frame, final Frame owner) {
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                owner.setEnabled(false);
            }

            public void windowClosing(WindowEvent e) {
                owner.setEnabled(true);
                frame.removeWindowListener(this);
            }

            public void windowClosed(WindowEvent e) {
                owner.setEnabled(true);
                frame.removeWindowListener(this);
            }
        });

        owner.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                if (frame.isShowing()) {
                    frame.setExtendedState(JFrame.NORMAL);
                    frame.toFront();
                } else {
                    owner.removeWindowListener(this);
                }
            }
        });

        frame.setVisible(true);
        try {
            new EventPump(frame).start();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
