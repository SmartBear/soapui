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

package com.eviware.soapui.integration.impl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.settings.LoadUISettings;
import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.io.IOException;

public class CajoServer {

    public static final String DEFAULT_SOAPUI_CAJO_PORT = "1198";

    private String server = null;
    private String port = DEFAULT_SOAPUI_CAJO_PORT;
    private String itemName = "soapuiIntegration";

    public static CajoServer getInstance() {
        return SingletonHolder.instance;
    }

    static class SingletonHolder {
        static CajoServer instance = new CajoServer();
    }

    private CajoServer() {
    }

    public void start() {
        String cajoPort = IntegrationUtils.getIntegrationPort("SoapUI", LoadUISettings.SOAPUI_CAJO_PORT,
                DEFAULT_SOAPUI_CAJO_PORT);
        Remote.config(server, Integer.valueOf(cajoPort), null, 0);
        try {
            ItemServer.bind(new TestCaseEditIntegrationImpl(), itemName);
            SoapUI.log("The cajo server is running on localhost:" + cajoPort + "/" + itemName);
        } catch (IOException e) {
            SoapUI.log(e.getMessage());
        }

        CajoClient.getInstance().testConnection();
    }

    public String getServer() {
        return server;
    }

    public String getPort() {
        return port;
    }

    public String getItemName() {
        return itemName;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
