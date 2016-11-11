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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.swing.SwingWorker;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * WsdlLoader for URLs
 *
 * @author ole.matzura
 */

public class UrlWsdlLoader extends WsdlLoader {
    private HttpContext state;
    protected HttpGet getMethod;
    private boolean aborted;
    protected Map<String, byte[]> urlCache = new HashMap<String, byte[]>();
    protected boolean finished;
    private boolean useWorker;
    private ModelItem contextModelItem;
    private org.apache.http.HttpResponse httpResponse;

    public UrlWsdlLoader(String url) {
        this(url, null);
    }

    public UrlWsdlLoader(String url, ModelItem contextModelItem) {
        super(url);
        this.contextModelItem = contextModelItem;
        state = new BasicHttpContext();
    }

    public boolean isUseWorker() {
        return useWorker;
    }

    public void setUseWorker(boolean useWorker) {
        this.useWorker = useWorker;
    }

    public InputStream load() throws Exception {
        return load(getBaseURI());
    }

    public synchronized InputStream load(String url) throws Exception {
        if (!PathUtils.isHttpPath(url)) {
            try {
                File file = new File(url.replace('/', File.separatorChar));
                if (file.exists()) {
                    url = file.toURI().toURL().toString();
                }
            } catch (Exception e) {
            }
        }

        if (urlCache.containsKey(url)) {
            setNewBaseURI(url);
            return new ByteArrayInputStream(urlCache.get(url));
        }

        if (url.startsWith("file:")) {
            return handleFile(url);
        }

        log.debug("Getting wsdl component from [" + url + "]");

        createGetMethod(url);

        if (aborted) {
            return null;
        }

        LoaderWorker worker = new LoaderWorker();
        if (useWorker) {
            worker.start();
        } else {
            worker.construct();
        }

        while (!aborted && !finished) {
            Thread.sleep(200);
        }

        // wait for method to catch up - required in unit tests..
        // limited looping to 10 loops because of eclipse plugin which entered
        // endless loop without it
        int counter = 0;
        byte[] content = null;

        if (httpResponse != null && httpResponse.getEntity() != null) {
            content = EntityUtils.toByteArray(new BufferedHttpEntity(httpResponse.getEntity()));
        }

        while (!aborted && content == null && counter < 10) {
            Thread.sleep(200);
            counter++;
        }

        if (aborted) {
            throw new Exception("Load of url [" + url + "] was aborted");
        } else {
            if (content != null) {
                String compressionAlg = HttpClientSupport.getResponseCompressionType(httpResponse);
                if (compressionAlg != null) {
                    content = CompressionSupport.decompress(compressionAlg, content);
                }

                urlCache.put(url, content);
                String newUrl = getMethod.getURI().toString();
                if (!url.equals(newUrl)) {
                    log.info("BaseURI was redirected to [" + newUrl + "]");
                }
                setNewBaseURI(newUrl);
                urlCache.put(newUrl, content);
                return new ByteArrayInputStream(content);
            } else {
                throw new Exception("Failed to load url; " + url + ", "
                        + (httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : 0) + " - "
                        + (httpResponse != null ? httpResponse.getStatusLine().getReasonPhrase() : ""));
            }
        }
    }

    protected InputStream handleFile(String url) throws Exception {
        setNewBaseURI(url);
        return new URL(url).openStream();
    }

    protected void createGetMethod(String url) {
        getMethod = new HttpGet(url);
        getMethod.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
        state.setAttribute(ClientContext.CREDS_PROVIDER, new WsdlCredentialsProvider());

        if (SoapUI.getSettings().getBoolean(HttpSettings.AUTHENTICATE_PREEMPTIVELY)) {
            if (!StringUtils.isNullOrEmpty(getUsername()) && !StringUtils.isNullOrEmpty(getPassword())) {
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getUsername(), getPassword());
                getMethod.addHeader(BasicScheme.authenticate(creds, "utf-8", false));
            }
        }
    }

    public final class LoaderWorker extends SwingWorker {
        public Object construct() {
            HttpClientSupport.SoapUIHttpClient httpClient = HttpClientSupport.getHttpClient();
            try {
                Settings soapuiSettings = SoapUI.getSettings();

                HttpClientSupport.applyHttpSettings(getMethod, soapuiSettings);

                httpResponse = httpClient.execute(getMethod, state);
            } catch (Exception e) {
                return e;
            } finally {
                finished = true;
            }

            return null;
        }
    }

    public boolean abort() {
        if (getMethod != null) {
            getMethod.abort();
        }

        aborted = true;

        return true;
    }

    public boolean isAborted() {
        return aborted;
    }

    /**
     * CredentialsProvider for providing login information during WSDL loading
     *
     * @author ole.matzura
     */

    private static Map<AuthScope, Credentials> cache = new HashMap<AuthScope, Credentials>();

    public final class WsdlCredentialsProvider implements CredentialsProvider {
        private XFormDialog basicDialog;
        private XFormDialog ntDialog;

        public WsdlCredentialsProvider() {
        }

        public Credentials getCredentials(final AuthScope authScope) {
            if (authScope == null) {
                throw new IllegalArgumentException("Authentication scope may not be null");
            }

            //	if( cache.containsKey( authScope ) )
            //	{
            //	return cache.get( authScope );
            //	}

            String pw = getPassword();
            if (pw == null) {
                pw = "";
            }

            if (AuthPolicy.NTLM.equalsIgnoreCase(authScope.getScheme())
                    || AuthPolicy.SPNEGO.equalsIgnoreCase(authScope.getScheme())) {
                String workstation = "";
                try {
                    workstation = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                }

                if (hasCredentials()) {
                    log.info("Returning url credentials");
                    return new NTCredentials(getUsername(), pw, workstation, null);
                }

                log.info(authScope.getHost() + ":" + authScope.getPort() + " requires Windows authentication");
                if (ntDialog == null) {
                    buildNtDialog();
                }

                StringToStringMap values = new StringToStringMap();
                values.put("Info", "Authentication required for [" + authScope.getHost() + ":" + authScope.getPort() + "]");
                ntDialog.setValues(values);

                if (ntDialog.show()) {
                    values = ntDialog.getValues();
                    NTCredentials credentials = new NTCredentials(values.get("Username"), values.get("Password"),
                            workstation, values.get("Domain"));

                    cache.put(authScope, credentials);
                    return credentials;
                }
            } else if (AuthPolicy.BASIC.equalsIgnoreCase(authScope.getScheme())
                    || AuthPolicy.DIGEST.equalsIgnoreCase(authScope.getScheme())) {
                if (hasCredentials()) {
                    log.info("Returning url credentials");
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUsername(), pw);
                    cache.put(authScope, credentials);
                    return credentials;
                }

                log.info(authScope.getHost() + ":" + authScope.getPort() + " requires authentication with the realm '"
                        + authScope.getRealm() + "'");
                ShowDialog showDialog = new ShowDialog();
                showDialog.values.put("Info",
                        "Authentication required for [" + authScope.getHost() + ":" + authScope.getPort() + "]");

                UISupport.getUIUtils().runInUIThreadIfSWT(showDialog);
                if (showDialog.result) {
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                            showDialog.values.get("Username"), showDialog.values.get("Password"));
                    cache.put(authScope, credentials);
                    return credentials;
                }
            }

            return null;
        }

        private void buildBasicDialog() {
            XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Basic Authentication");
            XForm mainForm = builder.createForm("Basic");
            mainForm.addLabel("Info", "");
            mainForm.addTextField("Username", "Username for authentication", XForm.FieldType.TEXT);
            mainForm.addTextField("Password", "Password for authentication", XForm.FieldType.PASSWORD);

            basicDialog = builder.buildDialog(builder.buildOkCancelActions(), "Specify Basic Authentication Credentials",
                    UISupport.OPTIONS_ICON);
        }

        private void buildNtDialog() {
            XFormDialogBuilder builder = XFormFactory.createDialogBuilder("NT Authentication");
            XForm mainForm = builder.createForm("Basic");
            mainForm.addLabel("Info", "");
            mainForm.addTextField("Username", "Username for authentication", XForm.FieldType.TEXT);
            mainForm.addTextField("Password", "Password for authentication", XForm.FieldType.PASSWORD);
            mainForm.addTextField("Domain", "NT Domain for authentication", XForm.FieldType.TEXT);

            ntDialog = builder.buildDialog(builder.buildOkCancelActions(), "Specify NT Authentication Credentials",
                    UISupport.OPTIONS_ICON);
        }

        private class ShowDialog implements Runnable {
            StringToStringMap values = new StringToStringMap();
            boolean result;

            public void run() {
                if (basicDialog == null) {
                    buildBasicDialog();
                }

                basicDialog.setValues(values);

                result = basicDialog.show();
                if (result) {
                    values = basicDialog.getValues();
                }
            }
        }

        public void clear() {
            cache.clear();
        }

        public void setCredentials(AuthScope arg0, Credentials arg1) {
        }
    }

    public void close() {
    }
}
