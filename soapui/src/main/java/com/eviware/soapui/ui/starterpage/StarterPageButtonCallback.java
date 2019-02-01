package com.eviware.soapui.ui.starterpage;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.NewRestProjectAction;
import com.eviware.soapui.impl.actions.NewWsdlProjectAction;
import com.eviware.soapui.impl.rest.actions.explorer.EndpointExplorerAction;
import com.eviware.soapui.model.workspace.Workspace;

import javax.swing.SwingUtilities;

public class StarterPageButtonCallback {

    public static String CALLBACK = "buttonCallback";

    private WorkspaceImpl workspace;

    public StarterPageButtonCallback(Workspace workspace) {
        this.workspace = (WorkspaceImpl) workspace;
    }

    public void createSoapProject() {
        Analytics.trackAction(SoapUIActions.OS_START_PAGE_NEW_SOAP_PROJECT);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NewWsdlProjectAction().perform(workspace, null);
            }
        });
    }

    public void createRestProject() {
        Analytics.trackAction(SoapUIActions.OS_START_PAGE_NEW_REST_PROJECT);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NewRestProjectAction().perform(workspace, null);
            }
        });
    }

    public void launchEndpointExplorer() {
        Analytics.trackAction(SoapUIActions.OS_START_PAGE_LAUNCH_EXPLORER);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new EndpointExplorerAction().actionPerformed(null);
            }
        });
    }

    public void sendTryProAnalytics(String location) {
        Analytics.trackAction(SoapUIActions.OS_START_PAGE_TRY_SUI_PRO, "Type", location);
    }
}
