/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionLoader;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.URI;

/**
 * Abstract WSDLLocator for loading definitions from either URL or cache..
 *
 * @author ole.matzura
 */

public abstract class WsdlLoader extends AbstractDefinitionLoader implements WsdlDefinitionLoader {
    protected static final Logger log = LogManager.getLogger(WsdlLoader.class);

    private String url;
    private String firstNewURI;
    private String last;
    private String username;
    private String password;

    public WsdlLoader(String url) {
        this.url = url;

        if (!PathUtils.isFilePath(url) && !PathUtils.isRelativePath(url)) {
            // check for username/password
            try {
                URI uri = new URI(url);
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    int colonIndex = userInfo.indexOf(':');
                    username = userInfo.substring(0, colonIndex);
                    password = userInfo.substring(colonIndex + 1);
                } else {
                    //userInfo may be null if username and password have some special chars and are not url encoded
                    String authority = uri.getAuthority();
                    if (authority != null) {
                        int atIndex = authority.lastIndexOf('@');
                        int colonIndex = authority.indexOf(':');

                        if (atIndex > colonIndex && colonIndex > 0) {
                            username = authority.substring(0, colonIndex);
                            password = authority.substring(colonIndex + 1, atIndex);
                        }
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public InputSource getBaseInputSource() {
        try {
            log.debug("Returning baseInputSource [" + url + "]");
            return new InputSource(load(url));
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public abstract InputStream load(String url) throws Exception;

    public XmlObject loadXmlObject(String url, XmlOptions options) throws Exception {
        try {
            if (options == null) {
                options = new XmlOptions();
            }

            if (monitor != null) {
                monitor.setProgress(progressIndex, "Loading [" + url + "]");
            }

            options.setLoadLineNumbers();
            return XmlUtils.createXmlObject(readCleanWsdlFrom(url), options);
        } catch (XmlException e) {
            XmlError error = e.getError();
            if (error != null) {
                InvalidDefinitionException ex = new InvalidDefinitionException(e);
                ex.setMessage("Error loading [" + url + "]");
                throw ex;
            } else {
                throw makeInvalidDefinitionException(url, e);
            }
        } catch (Exception e) {
            throw makeInvalidDefinitionException(url, e);
        }
    }

    private InvalidDefinitionException makeInvalidDefinitionException(String url, Exception e) throws InvalidDefinitionException {
        e.printStackTrace();
        log.error("Failed to load url [" + url + "]");
        return new InvalidDefinitionException("Error loading [" + url + "]: " + e);
    }

    private String readCleanWsdlFrom(String url) throws Exception {
        String content = XmlUtils.createXmlObject(load(url)).xmlText();
        if (SoapUI.getSettings().getBoolean(WsdlSettings.TRIM_WSDL)) {
            content = content.trim();
        }
        return Tools.removePropertyExpansions(url, content);
    }

    public String getBaseURI() {
        // log.debug( "Returning baseURI [" + url + "]" );
        return url;
    }

    public void setNewBaseURI(String newUrl) {
        if (firstNewURI == null) {
            firstNewURI = newUrl;
        }
        url = newUrl;
    }

    public String getFirstNewURI() {
        return firstNewURI == null ? url : firstNewURI;
    }

    public InputSource getImportInputSource(String parent, String imp) {
        if (isAbsoluteUrl(imp)) {
            last = imp;
        } else {
            last = Tools.joinRelativeUrl(parent, imp);
        }

        try {
            InputStream input = load(last);
            return input == null ? null : new InputSource(input);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    protected boolean isAbsoluteUrl(String tempImp) {
        tempImp = tempImp.toUpperCase();
        return tempImp.startsWith("HTTP:") || tempImp.startsWith("HTTPS:") || tempImp.startsWith("FILE:");
    }

    public String getLatestImportURI() {
        String result = last == null ? url : last;
        log.debug("Returning latest import URI [" + result + "]");
        return result;
    }

    public boolean hasCredentials() {
        return !StringUtils.isNullOrEmpty(getUsername()) && !StringUtils.isNullOrEmpty(getPassword());
        // return !StringUtils.isNullOrEmpty( username ) &&
        // !StringUtils.isNullOrEmpty( password );
    }

    public String getPassword() {
        return StringUtils.isNullOrEmpty(password) ? System.getProperty("soapui.loader.password", password)
                : password;
    }

    public String getUsername() {
        return StringUtils.isNullOrEmpty(username) ? System.getProperty("soapui.loader.username", username)
                : username;
    }

}
