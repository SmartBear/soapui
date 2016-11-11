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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;

/**
 * Swing-based ToolHost
 *
 * @author ole.matzura
 */

public class SwingToolHost implements ToolHost {
    public void run(ToolRunner runner) throws Exception {
        ProcessDialog processDialog = null;

        try {
            processDialog = new ProcessDialog(runner.getName(), runner.getDescription(), runner.showLog(),
                    runner.canCancel());
            ModelItem modelItem = runner.getModelItem();
            if (modelItem == null) {
                processDialog.log("Running " + runner.getName() + "\r\n");
            } else {
                processDialog.log("Running " + runner.getName() + " for [" + modelItem.getName() + "]\r\n");
            }
            processDialog.run(runner);
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex);
            throw ex;
        } finally {
            Analytics.trackAction(SoapUIActions.RUN_TOOL.getActionName(), "Tool", runner.getName());
            if (processDialog != null) {
                processDialog.setVisible(false);
            }
        }
    }
}
