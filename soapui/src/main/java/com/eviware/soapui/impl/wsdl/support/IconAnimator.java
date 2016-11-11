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

package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.support.AnimatableItem;
import com.eviware.soapui.support.UISupport;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.util.concurrent.Future;

/**
 * Class to animate the icon of an AnimatableItem
 *
 * @author ole.matzura
 */

public class IconAnimator<T extends AnimatableItem> implements Runnable {
    private final T target;
    private int index = 0;
    private volatile boolean stopped = true;
    private boolean enabled = true;
    private ImageIcon baseIcon;
    private ImageIcon[] animateIcons;
    private volatile Future<?> future;

    public IconAnimator(T target, String iconName, String animationBaseIconName, int num) {
        this.baseIcon = UISupport.createImageIcon(iconName);
        this.target = target;

        createAnimatedIcons(animationBaseIconName, num);
    }

    private void createAnimatedIcons(String animationBaseIcon, int num) {
        String[] parts = animationBaseIcon.split("\\.", 2);
        String baseName = parts[0];
        String type = parts[1];

        animateIcons = new ImageIcon[num];

        for (int c = 0; c < animateIcons.length; c++) {
            animateIcons[c] = UISupport.createImageIcon(baseName + "_" + (c + 1) + "." + type);
        }
    }

    public void stop() {
        stopped = true;
    }

    public int getIndex() {
        return index;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        if (!enabled) {
            return;
        }
        if (!SoapUI.usingGraphicalEnvironment()) {
            // Don't use animation if we're not in the SoapUI GUI.
            return;
        }

		/*
         * mock service to be run needs to be stopped first.
		 * 
		 * if service is restart action occurs while it is running, than run()
		 * needs to finish first so service can be started again. If that is 
		 * case than force stopping mock service.
		 * 
		 */
        if (isStopped()) {

            Future<?> localFuture = future;
            if (future != null && !localFuture.isDone()) {
                localFuture.cancel(true);
                while (future != null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            stopped = false;
            future = SoapUI.getThreadPool().submit(this);
        }
    }

    public ImageIcon getBaseIcon() {
        return baseIcon;
    }

    public ImageIcon getIcon() {
        if (!isStopped()) {
            return animateIcons[getIndex()];
        }

        return baseIcon;
    }

    public void run() {
        if (!SoapUI.usingGraphicalEnvironment()) {
            // Don't use animation if we're not in the SoapUI GUI.
            return;
        }
        String oldThreadName = Thread.currentThread().getName();
        if (future != null) {
            if (System.getProperty("soapui.enablenamedthreads") != null) {
                Thread.currentThread().setName("IconAnimator for " + target.getName());
            }
        }
        try {
            while (!stopped) {
                try {
                    if (stopped) {
                        break;
                    }

                    index = index >= animateIcons.length - 1 ? 0 : index + 1;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            target.setIcon(getIcon());
                        }
                    });
                    Thread.sleep(500);
                } catch (InterruptedException e) {
//				SoapUI.log( "Mock Service Force Stopped!" );
                    stopped = true;
                }
            }

            target.setIcon(getIcon());
            future = null;
            notify();
            // iconAnimationThread = null;
        } finally {
            if (System.getProperty("soapui.enablenamedthreads") != null) {
                Thread.currentThread().setName(oldThreadName);
            }
        }
    }

    public T getTarget() {
        return target;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!stopped) {
            stopped = enabled;
        }
    }
}
