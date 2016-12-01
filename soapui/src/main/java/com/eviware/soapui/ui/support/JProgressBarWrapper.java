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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.support.UISupport;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Dimension;

/**
 * Due to a limitation in the swing implementation for OSX we, for the time being,
 * need to avoid using indeterminate, long running JProgressBars.
 * The problem is that the thread animating the progress bar can hog a full CPU core.
 */
public class JProgressBarWrapper {


    private JProgressBar progressBar;

    public void setIndeterminate(boolean b) {
        if (progressBar != null) {
            progressBar.setIndeterminate(b);
        }
    }


    public void addToToolBar(JComponent jComponent) {
        if (!UISupport.isMac()) {
            progressBar = new JProgressBar();
            JPanel progressBarPanel = UISupport.createProgressBarPanel(progressBar, 2, false);
            progressBarPanel.setPreferredSize(new Dimension(60, 20));
            jComponent.add(progressBarPanel);
        }
    }
}
