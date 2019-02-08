package com.eviware.soapui.impl.rest.actions.explorer;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.actions.explorer.callback.EndpointExplorerCallback;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.PagePropertyMapper;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponent;
import com.eviware.soapui.support.components.WebViewBasedBrowserComponentFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.eviware.soapui.settings.UISettings.SHOW_ENDPOINT_EXPLORER_ON_START;

public class EndpointExplorerAction extends AbstractAction {

    private JDialog dialog;

    public EndpointExplorerAction() {
        putValue(Action.NAME, "Endpoint Explorer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WebViewBasedBrowserComponent browser =
                WebViewBasedBrowserComponentFactory.createBrowserComponent
                        (false, WebViewBasedBrowserComponent.PopupStrategy.EXTERNAL_BROWSER);
        Component browserComponent = browser.getComponent();

        dialog = new JDialog(UISupport.getMainFrame(), "Endpoint Explorer", false);
        dialog.getContentPane().add(browserComponent);
        dialog.setSize(860, 435);
        dialog.setMinimumSize(new Dimension(860, 435));
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(true);
        dialog.setVisible(true);
        dialog.setIconImage((UISupport.createImageIcon("/SoapUI-OS_16-16.png")).getImage());
        dialog.getRootPane().registerKeyboardAction((event) -> close(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        UISupport.centerDialog(dialog, UISupport.getMainFrame());
        UISupport.getMainFrame().setEnabled(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UISupport.getMainFrame().setEnabled(true);
                dialog.setVisible(false);
            }
        });

        String path = "/com/eviware/soapui/explorer/soapui-pro-api-endpoint-explorer-starter-page.html";
        String resource = this.getClass().getResource(path).toString();
        browser.navigate(resource);
        PagePropertyMapper pagePropertyMapper = browser.getPagePropertyMapper();
        if (pagePropertyMapper != null) {
            pagePropertyMapper.update("dontShow", !SoapUI.getSettings().getBoolean(SHOW_ENDPOINT_EXPLORER_ON_START, true));
        }

        browser.addJavaScriptEventHandler("closeCallback", this);
        browser.addJavaScriptEventHandler("inspectorCallback", new EndpointExplorerCallback(browser));
    }

    public void close() {
        UISupport.getMainFrame().setEnabled(true);
        dialog.setVisible(false);
    }
}
