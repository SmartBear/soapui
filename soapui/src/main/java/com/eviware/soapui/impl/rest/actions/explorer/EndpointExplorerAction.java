package com.eviware.soapui.impl.rest.actions.explorer;

import com.eviware.soapui.impl.rest.actions.explorer.callback.EndpointExplorerCallback;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.event.ActionEvent;

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
        frame.setSize(860, 435);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.setIconImage((UISupport.createImageIcon("/SoapUI-OS_16-16.png")).getImage());

        String path = "/com/eviware/soapui/explorer/soapui-pro-api-endpoint-explorer-starter-page.html";
        String resource = this.getClass().getResource(path).toString();
        browser.navigate(resource);

        browser.addJavaScriptEventHandler("closeCallback", this);
        browser.addJavaScriptEventHandler("inspectorCallback", new EndpointExplorerCallback());
    }

    public void close() {
        frame.setVisible(false);
    }
}
