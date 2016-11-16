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

import java.io.IOException;
import java.rmi.ConnectException;

public class CajoClient {

    public static final String DEFAULT_LOADUI_CAJO_PORT = "1199";

    private String server = "localhost";
    private String port = DEFAULT_LOADUI_CAJO_PORT;
    private String itemName = "loaduiIntegration";

    private static CajoClient instance;

    public static CajoClient getInstance() {
        if (instance == null) {
            instance = new CajoClient();
            instance.port = IntegrationUtils.getIntegrationPort("loadUI", LoadUISettings.LOADUI_CAJO_PORT,
                    DEFAULT_LOADUI_CAJO_PORT);
            return instance;
        } else {
            return instance;
        }
    }

    private CajoClient() {
    }

    public Object getItem() throws Exception {
        return gnu.cajo.invoke.Remote.getItem("//" + server + ":" + port + "/" + itemName);
    }

    public Object invoke(String method, Object object) throws Exception {
        try {
            return gnu.cajo.invoke.Remote.invoke(getItem(), method, object);
        } catch (ConnectException e) {
            SoapUI.log.info("Could not connect to SoapUI cajo server on " + getConnectionString());
            return null;
        } catch (IOException e) {
            // case of loadUI project opening failure
            throw e;
        } catch (Exception e) {
            SoapUI.log.info("Connected SoapUI cajo server, but with exception: ");
            e.printStackTrace();
            return null;
        }
    }

    public boolean testConnection() {
        try {
            gnu.cajo.invoke.Remote.invoke(getItem(), "test", null);
            setLoadUIPath();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * If loadUI bat folder is not specified in SoapUI and there is an running
     * instance of loadUI, takes the path of that instance and sets it to SoapUI.
     */
    public void setLoadUIPath() {
        String loadUIPath = SoapUI.getSettings().getString(LoadUISettings.LOADUI_PATH, "");
        if (loadUIPath == null || loadUIPath.trim().length() == 0) {
            try {
                loadUIPath = (String) invoke("getLoadUIPath", null);
                if (loadUIPath != null) {
                    SoapUI.getSettings().setString(LoadUISettings.LOADUI_PATH, loadUIPath);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public String getConnectionString() {
        return "//" + server + ":" + port + "/" + itemName;
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
