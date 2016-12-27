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

package com.eviware.soapui.support.components;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.SwingWorkerDelegator;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

/**
 * Dialog for creating progress-dialogs
 *
 * @author Ole.Matzura
 */

public class ProgressDialog extends JDialog implements XProgressDialog, XProgressMonitor {
    private JProgressBar progressBar;
    private JButton cancelButton;
    private Worker worker;

    public ProgressDialog(String title, String label, int length, String initialValue, boolean allowCancel)
            throws HeadlessException {
        super(UISupport.getMainFrame(), title, true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        progressBar = new JProgressBar(0, length);
        JPanel panel = UISupport.createProgressBarPanel(progressBar, 10, true);
        progressBar.setString(initialValue);

        getContentPane().setLayout(new BorderLayout());
        JLabel progressLabel = new JLabel(label);
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        getContentPane().add(progressLabel, BorderLayout.NORTH);
        getContentPane().add(panel, BorderLayout.CENTER);

        if (allowCancel) {
            ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
            builder.addGlue();
            cancelButton = new JButton(new CancelAction());
            builder.addFixed(cancelButton);
            builder.addGlue();
            builder.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            getContentPane().add(builder.getPanel(), BorderLayout.SOUTH);
        }

        pack();
    }

    public void run(Worker worker) {
        this.worker = worker;
        SwingWorkerDelegator swingWorker = new SwingWorkerDelegator(this, this, worker) {
            @Override
            public void finished() {
                super.finished();
                ProgressDialog.this.worker = null;
            }
        };

        swingWorker.start();
        setVisible(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.support.components.XProgressMonitor#setProgress(int,
     * java.lang.String)
     */
    public void setProgress(final int value, final String string) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(value);
                progressBar.setString(string);
                pack();
            }
        });
    }

    public void setDeterminate() {
        progressBar.setIndeterminate(false);
    }

    public void setIndeterminate() {
        progressBar.setIndeterminate(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.eviware.soapui.support.components.XProgressMonitor#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        if (visible) {
            UISupport.centerDialog(this);
        }
        super.setVisible(visible);
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent e) {
            worker.onCancel();
        }
    }

    public void setCancelLabel(String label) {
        if (cancelButton != null) {
            cancelButton.setText(label);
        }
    }
}
