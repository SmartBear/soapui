package com.eviware.soapui.support;

import com.eviware.soapui.impl.wsdl.support.HelpUrls;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.*;


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

