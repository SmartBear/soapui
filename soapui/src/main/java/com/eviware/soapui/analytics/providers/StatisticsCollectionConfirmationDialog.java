package com.eviware.soapui.analytics.providers;

import com.eviware.soapui.support.UISupport;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StatisticsCollectionConfirmationDialog {
    public static int showDialog() {
        return JOptionPane.showConfirmDialog(null,
                getPanel(),
                "Usage statistics",
                JOptionPane.YES_NO_OPTION);
    }

    private static JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Do you want to help us improve SoapUI by sending anonymous usage statistics?");
        JLabel labelEx = new JLabel("This can be turned off any time in UI settings.");
        panel.add(label);
        panel.add(labelEx);
        panel.add(UISupport.createLabelLink("http://www.soapui.org/Store-Info/privacy-policy.html", "Privacy policy"));

        return panel;
    }
}