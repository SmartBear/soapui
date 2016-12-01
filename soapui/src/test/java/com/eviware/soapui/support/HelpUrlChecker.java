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

package com.eviware.soapui.support;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;


/**
 * @author joel.jonsson
 */
public class HelpUrlChecker {
    public static void main(String[] args) throws IllegalAccessException, InterruptedException, IOException {
        openHelpUrls(HelpUrls.class);
    }

    private static void openHelpUrls(Class<?> helpUrlsClass) throws IllegalAccessException, InterruptedException, IOException {
        for (Field field : helpUrlsClass.getDeclaredFields()) {
            String url = (String) field.get(null);
            if (!StringUtils.isNullOrEmpty(url)) {
                String helpUrl = url;
                if (url.substring(0, 1).equals("/")) {
                    helpUrl = HelpUrls.BASE_URL_PROD + url;
                }

                try {
                    int responseCode = ((HttpURLConnection) new URL(helpUrl).openConnection()).getResponseCode();
                    if (responseCode != 200) {
                        System.out.println(responseCode + " " + helpUrl);
                    }
                } catch (UnknownHostException e) {
                    System.out.println("No host: " + helpUrl);
                } catch (Exception e) {
                    System.out.println("Error: " + helpUrl);
                    e.printStackTrace();
                }



            }
//            Thread.sleep(2000);
        }
    }
}

