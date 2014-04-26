/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.support.components;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;

public class JEditorStatusBarWithProgress extends JEditorStatusBar {
    private JProgressBar progressBar;

    public JEditorStatusBarWithProgress() {
        super();

        initProgressBar();
    }

    private void initProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 3),
                BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY)));

        setStatusComponent(progressBar);
    }

    public JEditorStatusBarWithProgress(JEditorStatusBarTarget target) {
        super(target);

        initProgressBar();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setIndeterminate(boolean newValue) {
        progressBar.setIndeterminate(newValue);
    }

    public void setValue(int n) {
        progressBar.setValue(n);
    }
}
