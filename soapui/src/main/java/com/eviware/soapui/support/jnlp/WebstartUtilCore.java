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

package com.eviware.soapui.support.jnlp;

import java.io.File;

public class WebstartUtilCore extends WebstartUtil {

    public static void init() {
        if (isWebStart()) {
            try {
                // if( System.getProperty( "deployment.user.tmp" ) != null
                // && System.getProperty( "deployment.user.tmp" ).length() > 0 )
                // {
                // System.setProperty( "GRE_HOME", System.getProperty(
                // "deployment.user.tmp" ) );
                // }

                // wsi-test-tools
                System.setProperty("wsi.dir",
                        createWebStartDirectory("wsi-test-tools", System.getProperty("wsitesttools.jar.url"))
                                + File.separator + "wsi-test-tools");
                System.out.println(System.getProperty("wsi.dir"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
