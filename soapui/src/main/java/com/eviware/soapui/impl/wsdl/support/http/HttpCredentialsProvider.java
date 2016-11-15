package com.eviware.soapui.impl.wsdl.support.http;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import com.eviware.soapui.support.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.AuthPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HttpCredentialsProvider implements CredentialsProvider {
    private final static Logger logger = LoggerFactory.getLogger(HttpCredentialsProvider.class);
    private String requestUsername;
    private String requestPassword;
    private String requestDomain;
    private String requestAuthPolicy;
    private boolean checkedCredentials;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    @Override
    public void setCredentials(AuthScope authScope, Credentials credentials) {
    }

    @Override
    public Credentials getCredentials(AuthScope authScope) {
        if (authScope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }

        Credentials credentials;
        if (StringUtils.hasContent(proxyHost) && isProxyAuthScope(authScope)) {
            credentials = getProxyCredentials();
        } else {
            credentials = getRequestCredentials(authScope);
        }

        return credentials;
    }

    @Override
    public void clear() {
    }

    public void setRequestCredentials(String requestUsername, String requestPassword, String requestDomain, String requestAuthPolicy) {
        this.requestUsername = requestUsername;
        this.requestPassword = requestPassword == null ? "" : requestPassword;
        this.requestDomain = requestDomain;
        this.requestAuthPolicy = requestAuthPolicy;
    }

    public void setProxyCredentials(String proxyHost, String proxyPort, String proxyUsername, String proxyPassword) {
        setProxy(proxyHost, proxyPort);
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword == null ? "" : proxyPassword;
    }

    public void setProxy(String proxyHost, String proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public void loadProxyCredentialsFromSettings() {
        Settings settings = SoapUI.getSettings();
        boolean proxyEnabled = settings.getBoolean(ProxySettings.ENABLE_PROXY);
        boolean autoProxy = settings.getBoolean(ProxySettings.AUTO_PROXY);

        if (proxyEnabled) {
            String host = null;
            String port = null;
            if (!autoProxy) {
                host = PropertyExpander.expandProperties(settings.getString(ProxySettings.HOST, null));
                port = PropertyExpander.expandProperties(settings.getString(ProxySettings.PORT, null));
            }
            String username = PropertyExpander.expandProperties(settings.getString(ProxySettings.USERNAME, null));
            String password = PropertyExpander.expandProperties(settings.getString(ProxySettings.PASSWORD, null));
            setProxyCredentials(host, port, username, password);
        } else {
            setProxyCredentials(null, null, null, null);
        }

    }

    private boolean isProxyAuthScope(AuthScope authScope) {
        if (proxyHost.equalsIgnoreCase(authScope.getHost())) {
            if (StringUtils.hasContent(proxyPort) && (Integer.parseInt(proxyPort) == authScope.getPort())) {
                return true;
            }
        }
        return false;
    }

    private Credentials getRequestCredentials(AuthScope authScope) {
        if (checkedCredentials) {
            return null;
        }

        if (requestAuthPolicy != null && !requestAuthPolicy.equalsIgnoreCase(authScope.getScheme())) {
            return null;
        }

        try {
            if (AuthPolicy.NTLM.equalsIgnoreCase(authScope.getScheme())) {
                logger.debug("{}:{} requires Windows authentication", authScope.getHost(), authScope.getPort());
                return getNTCredentials(requestUsername, requestPassword, requestDomain);
            } else if (AuthPolicy.BASIC.equalsIgnoreCase(authScope.getScheme())
                    || AuthPolicy.DIGEST.equalsIgnoreCase(authScope.getScheme())
                    || AuthPolicy.SPNEGO.equalsIgnoreCase(authScope.getScheme())) {
                logger.debug("{}:{} requires authentication with the realm '{}'",
                        new Object[] {authScope.getHost(), authScope.getPort(), authScope.getRealm()});
                if (requestUsername == null && AuthPolicy.BASIC.equalsIgnoreCase(authScope.getScheme())) {
                    logger.warn("Username is empty");
                    return new UsernamePasswordCredentials("", requestPassword);
                }
                return new UsernamePasswordCredentials(requestUsername, requestPassword);
            }
        } finally {
            checkedCredentials = true;
        }
        return null;
    }

    private Credentials getProxyCredentials() {
        if (StringUtils.hasContent(proxyUsername) && StringUtils.hasContent(proxyPassword)) {
            return ProxyUtils.getProxyCredentials(proxyUsername, proxyPassword);
        }
        return null;
    }

    private NTCredentials getNTCredentials(String username, String password, String domain) {
        String workstation = "";
        try {
            workstation = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) {
        }
        return new NTCredentials(username, password, workstation, domain);
    }
}
