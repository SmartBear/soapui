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

package com.eviware.soapui.impl.wsdl.support.http;

import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.UriFilter;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.params.HttpParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for setting proxy-servers correctly
 *
 * @author ole.matzura
 */

public class ProxyUtils {
    private final static Logger logger = LogManager.getLogger(ProxyUtils.class);

    private static boolean proxyEnabled;

    private static boolean autoProxy;

    static {
        setProxyEnabled(SoapUI.getSettings().getBoolean(ProxySettings.ENABLE_PROXY));
        setAutoProxy(SoapUI.getSettings().getBoolean(ProxySettings.AUTO_PROXY));
    }

    private static String getExpandedProperty(PropertyExpansionContext context, Settings settings, String property) {
        String content = settings.getString(property, null);
        return context != null ? PropertyExpander.expandProperties(context, content) : PropertyExpander.expandProperties(content);
    }

    private static CredentialsProvider getProxyCredentialsProvider(Settings settings) {
        String proxyUsername = getExpandedProperty(null, settings, ProxySettings.USERNAME);
        String proxyPassword = getExpandedProperty(null, settings, ProxySettings.PASSWORD);

        if (!StringUtils.isNullOrEmpty(proxyUsername) && !StringUtils.isNullOrEmpty(proxyPassword)) {
            Credentials proxyCreds = getProxyCredentials(proxyUsername, proxyPassword);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, proxyCreds);
            return credsProvider;
        }
        return null;
    }

    public static Credentials getProxyCredentials(String proxyUsername, String proxyPassword) {
        Credentials proxyCreds = new UsernamePasswordCredentials(proxyUsername, proxyPassword);

        // check for nt-username
        int ix = proxyUsername.indexOf('\\');
        if (ix > 0) {
            String domain = proxyUsername.substring(0, ix);
            if (proxyUsername.length() > ix + 1) {
                String user = proxyUsername.substring(ix + 1);
                proxyCreds = new NTCredentials(user, proxyPassword, getWorkstationName(), domain);
            }
        }

        return proxyCreds;
    }

    private static String getWorkstationName() {
        String workstation = "";
        try {
            workstation = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Workstation name could not be fetched.", e);
        }
        return workstation;
    }

    public static boolean excludes(String[] excludes, String proxyHost, int proxyPort) {
        for (String excludeString : excludes) {
            String exclude = excludeString.trim();
            if (exclude.length() == 0) {
                continue;
            }

            // check for port
            int ix = exclude.indexOf(':');

            if (ix >= 0 && exclude.length() > ix + 1) {
                String excludePort = exclude.substring(ix + 1);
                if (proxyPort != -1 && excludePort.equals(String.valueOf(proxyPort))) {
                    exclude = exclude.substring(0, ix);
                } else {
                    continue;
                }
            }

			/*
             * This will exclude addresses with wildcard *, too.
			 */
            // if( proxyHost.endsWith( exclude ) )
            // return true;
            String excludeIp = exclude.indexOf('*') >= 0 ? exclude : nslookup(exclude, true);
            String ip = nslookup(proxyHost, true);
            Pattern pattern = Pattern.compile(excludeIp);
            Matcher matcher = pattern.matcher(ip);
            Matcher matcher2 = pattern.matcher(proxyHost);
            if (matcher.find() || matcher2.find()) {
                return true;
            }
        }

        return false;
    }

    private static String nslookup(String s, boolean ip) {

        InetAddress host;
        String address;

        // get the bytes of the IP address
        try {
            host = InetAddress.getByName(s);
            if (ip) {
                address = host.getHostAddress();
            } else {
                address = host.getHostName();
            }
        } catch (UnknownHostException ue) {
            return s; // no host
        }

        return address;

    } // end lookup

    public static boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public static void setProxyEnabled(boolean proxyEnabled) {
        ProxyUtils.proxyEnabled = proxyEnabled;
    }

    public static boolean isAutoProxy() {
        return autoProxy;
    }

    public static void setAutoProxy(boolean autoProxy) {
        ProxyUtils.autoProxy = autoProxy;
    }

    public static void setGlobalProxy(Settings settings) {
        ProxySelector proxySelector = null;
        ProxySettingsAuthenticator authenticator = null;
        if (proxyEnabled) {
            if (autoProxy) {
                proxySelector = new ProxyVoleUtil().createAutoProxySearch().getProxySelector();
            } else {
                proxySelector = getManualProxySelector(settings);
            }
            if (proxySelector != null) {
                // Don't register any proxies for other schemes
                proxySelector = filterHttpHttpsProxy(proxySelector);
            }
            authenticator = new ProxySettingsAuthenticator();
        }
        ProxySelector.setDefault(proxySelector);
        Authenticator.setDefault(authenticator);
        HttpClientSupport.setProxySelector(proxySelector);
        HttpClientSupport.getHttpClient().setCredentialsProvider(getProxyCredentialsProvider(settings));
    }

    public static ProxySelector filterHttpHttpsProxy(ProxySelector proxySelector) {
        return new ProxyBypassListSelector(
                Arrays.<UriFilter>asList(new SchemeProxyFilter("http", "https")),
                proxySelector);
    }

    private static ProxySelector getManualProxySelector(Settings settings) {
        try {
            String proxyHost = getExpandedProperty(null, settings, ProxySettings.HOST);
            String proxyPort = getExpandedProperty(null, settings, ProxySettings.PORT);
            if (!StringUtils.isNullOrEmpty(proxyHost) && !StringUtils.isNullOrEmpty(proxyPort)) {
                String[] excludes = PropertyExpander.expandProperties(settings.getString(ProxySettings.EXCLUDES, "")).split(",");
                return new ManualProxySelector(proxyHost, Integer.valueOf(proxyPort), excludes);
            }
        } catch (Exception e) {
            SoapUI.logError(e, "Unable to expand proxy settings");
        }
        return null;
    }

    public static void setForceDirectConnection(HttpParams params) {
        OverridableProxySelectorRoutePlanner.setForceDirectConnection(params);
    }

    private static class ProxySettingsAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            if (getRequestorType() != RequestorType.PROXY) {
                return null;
            }
            Settings settings = SoapUI.getSettings();
            try {
                String proxyUsername = PropertyExpander.expandProperties(settings.getString(ProxySettings.USERNAME, null));
                String proxyPassword = PropertyExpander.expandProperties(settings.getString(ProxySettings.PASSWORD, null));
                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
            } catch (Exception e) {
                SoapUI.logError(e, "Unable to expand proxy settings");
                return null;
            }
        }
    }

}
