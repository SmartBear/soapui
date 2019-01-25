package com.eviware.soapui.impl.rest.actions.explorer;

import com.eviware.soapui.impl.rest.actions.explorer.callback.EndpointExplorerCallback;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EndpointExplorerAction extends AbstractAction {

    private JFrame frame;

    public EndpointExplorerAction() {
        putValue(Action.NAME, "Endpoint Explorer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WebViewBasedBrowserComponent browser =
                WebViewBasedBrowserComponentFactory.createBrowserComponent
                        (false, WebViewBasedBrowserComponent.PopupStrategy.EXTERNAL_BROWSER);
        Component browserComponent = browser.getComponent();

        frame = new JFrame();
        frame.getContentPane().add(browserComponent);
        frame.setTitle("Endpoint Explorer");
        frame.setSize(860, 419);
        frame.setMinimumSize(new Dimension(860, 419));
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setIconImage((UISupport.createImageIcon("/SoapUI-OS_16-16.png")).getImage());
        UISupport.centerDialog(frame, UISupport.getMainFrame());
        UISupport.getMainFrame().setEnabled(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UISupport.getMainFrame().setEnabled(true);
            }
        });

        String path = "/com/eviware/soapui/explorer/soapui-pro-api-endpoint-explorer-starter-page.html";
        String resource = this.getClass().getResource(path).toString();
        browser.navigate(resource);

        browser.addJavaScriptEventHandler("closeCallback", this);
        browser.addJavaScriptEventHandler("inspectorCallback", new EndpointExplorerCallback());
    }

    public void close() {
        UISupport.getMainFrame().setEnabled(true);
        frame.setVisible(false);
    }
}
