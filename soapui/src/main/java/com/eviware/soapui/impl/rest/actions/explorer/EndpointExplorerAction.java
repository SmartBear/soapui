package com.eviware.soapui.impl.rest.actions.explorer;

import com.eviware.soapui.analytics.ModuleType;
import com.eviware.soapui.impl.rest.actions.explorer.callback.EndpointExplorerCallback;
import com.eviware.soapui.impl.rest.actions.explorer.callback.ModuleStarterPageCallback;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import static com.eviware.soapui.SoapUI.getWorkspace;

public class EndpointExplorerAction extends AbstractAction {

    private JFrame frame;

    public void close() {
        frame.setVisible(false);
    }

    public EndpointExplorerAction() {
        putValue(Action.NAME, "Endpoint Explorer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WebViewBasedBrowserComponent browser =
                WebViewBasedBrowserComponentFactory.createBrowserComponent
                        (false, WebViewBasedBrowserComponent.PopupStrategy.EXTERNAL_BROWSER);
        Component component = browser.getComponent();
        component.setMinimumSize(new Dimension(860, 450));

        frame = new JFrame();
        frame.getContentPane().add(component);
        frame.setTitle("Endpoint Explorer");
        frame.setSize(860, 450);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setIconImage((UISupport.createImageIcon("/SoapUI-OS_16-16.png")).getImage());

        browser.navigate("file://" + "C:\\Users\\Christina.Zelenko\\Desktop\\soapOS\\soapui\\soapui\\src\\main\\resources\\com\\eviware\\soapui\\explorer\\soapui-pro-api-endpoint-explorer-starter-page.html");

        browser.addJavaScriptEventHandler("inspectorCallback", new EndpointExplorerCallback());
        browser.addJavaScriptEventHandler("closeCallback", this);
        browser.addJavaScriptEventHandler("moduleStarterPageCallback", new ModuleStarterPageCallback(getWorkspace(), ModuleType.SOAPUI_NG.getId()));
    }
}
