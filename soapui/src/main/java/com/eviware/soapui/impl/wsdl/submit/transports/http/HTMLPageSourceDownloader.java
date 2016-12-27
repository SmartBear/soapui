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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.support.RequestFileAttachment;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Request;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HTMLPageSourceDownloader {
    WebClient client = new WebClient();
    List<String> missingResourcesList = new ArrayList<String>();
    public static final String MISSING_RESOURCES_LIST = "MissingResourcesList";

    public static final HashMap<String, String> acceptTypes = new HashMap<String, String>() {
        {
            put("html", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            put("img", "image/png,image/*;q=0.8,*/*;q=0.5");
            put("script", "*/*");
            put("style", "text/css,*/*;q=0.1");
        }
    };

    List<Attachment> attachmentList = new ArrayList<Attachment>();

    protected List<Attachment> downloadCssAndImages(String endpoint, HttpRequest request)
            throws MalformedURLException, IOException {
        HtmlPage htmlPage = client.getPage(endpoint);
        String xPathExpression = "//*[name() = 'img' or name() = 'link' and @type = 'text/css']";
        List<?> resultList = htmlPage.getByXPath(xPathExpression);
        byte[] bytes = null;
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        Iterator<?> i = resultList.iterator();
        while (i.hasNext()) {
            try {
                HtmlElement htmlElement = (HtmlElement) i.next();
                String path = htmlElement.getAttribute("src").equals("") ? htmlElement.getAttribute("href")
                        : htmlElement.getAttribute("src");
                if (path == null || path.equals("")) {
                    continue;
                }
                URL url = htmlPage.getFullyQualifiedUrl(path);
                try {
                    bytes = downloadResource(htmlPage, htmlElement, url);
                } catch (FailingHttpStatusCodeException fhsce) {
                    SoapUI.log.warn(fhsce.getMessage());
                    attachmentList.add(createMissingAttachment(request, url, fhsce));
                    continue;
                }

                attachmentList.add(createAttachment(bytes, url, request));
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
        client.removeRequestHeader("Accept");
        return attachmentList;
    }

    private RequestFileAttachment createMissingAttachment(HttpRequest request, URL url,
                                                          FailingHttpStatusCodeException fhsce) throws IOException {
        File temp = new File(fhsce.getStatusCode() + "_" + fhsce.getStatusMessage() + "_" + url.toString());
        RequestFileAttachment missingFile = new RequestFileAttachment(temp, false, (AbstractHttpRequest<?>) request);
        missingResourcesList.add(fhsce.getStatusCode() + " " + fhsce.getStatusMessage() + " " + url.toString());
        return missingFile;
    }

    public Attachment createAttachment(byte[] bytes, URL url, Request request) throws IOException {
        String fileName = url.getPath()
                .substring(url.getPath().lastIndexOf("/") + 1, url.getPath().lastIndexOf("."));
        String extension = url.getPath().substring(url.getPath().lastIndexOf("."));

        // handling -> java.lang.IllegalArgumentException: Prefix string too short
        if (fileName.length() < 3) {
            fileName += "___";
        }

        File temp = File.createTempFile(fileName, extension);
        OutputStream out = new FileOutputStream(temp);
        out.write(bytes);
        out.close();
        return new RequestFileAttachment(temp, false, (AbstractHttpRequest<?>) request);
    }

    private byte[] downloadResource(HtmlPage page, HtmlElement htmlElement, URL url) throws IOException {
        WebRequestSettings wrs = null;

        wrs = new WebRequestSettings(url);
        wrs.setAdditionalHeader("Referer", page.getWebResponse().getRequestSettings().getUrl().toString());
        client.addRequestHeader("Accept", acceptTypes.get(htmlElement.getTagName().toLowerCase()));
        return client.getPage(wrs).getWebResponse().getContentAsBytes();

    }

    public List<String> getMissingResourcesList() {
        return missingResourcesList;
    }

}
