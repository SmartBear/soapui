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

package com.eviware.soapui.impl.wsdl.submit.transports.jms.util;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.SoapUIExtensionClassLoader.SoapUIClassLoaderState;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import hermes.Hermes;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class HermesUtils {
    private static ClassLoader hermesClassLoader;
    private static Map<String, Context> contextMap = new HashMap<String, Context>();
    public static String HERMES_CONFIG_XML = "hermes-config.xml";

    private static HashSet<String> classesToBeLoadedByParentClassLoader = new HashSet<>();

    static {
        classesToBeLoadedByParentClassLoader.add("hermes.browser.tasks.ThreadPool");
        classesToBeLoadedByParentClassLoader.add("hermes.browser.HermesUI");
        classesToBeLoadedByParentClassLoader.add("hermes.Hermes");
        classesToBeLoadedByParentClassLoader.add("hermes.config.SessionConfig");
        classesToBeLoadedByParentClassLoader.add("hermes.browser.UIMessageSink");
        classesToBeLoadedByParentClassLoader.add("hermes.Domain");
        classesToBeLoadedByParentClassLoader.add("hermes.EventManager");
        classesToBeLoadedByParentClassLoader.add("hermes.HermesDispatcher");
        classesToBeLoadedByParentClassLoader.add("hermes.config.DestinationConfig");
        classesToBeLoadedByParentClassLoader.add("hermes.config.PropertyConfig");
        classesToBeLoadedByParentClassLoader.add("hermes.config.PropertySetConfig");
        classesToBeLoadedByParentClassLoader.add("hermes.config.RendererConfig");
        classesToBeLoadedByParentClassLoader.add("hermes.ProviderMetaData");
        classesToBeLoadedByParentClassLoader.add("hermes.impl.DestinationManager");
        classesToBeLoadedByParentClassLoader.add("hermes.browser.MessageRenderer");

    }

    public static ClassLoader getHermesClassLoader() {
        return hermesClassLoader;
    }

    public static Context hermesContext(WsdlProject project) throws NamingException, MalformedURLException,
            IOException {
        String expandedHermesConfigPath = PropertyExpander.expandProperties(project, project.getHermesConfig());
        String key = project.getName() + expandedHermesConfigPath;
        return getHermes(key, expandedHermesConfigPath);
    }

    public static Context hermesContext(WsdlProject project, String hermesConfigPath) throws NamingException,
            MalformedURLException, IOException {
        String expandedHermesConfigPath = PropertyExpander.expandProperties(project, hermesConfigPath);
        String key = project.getName() + expandedHermesConfigPath;
        return getHermes(key, expandedHermesConfigPath);
    }

    // private static URLClassLoader hermesClassLoader;

    private static Context getHermes(String key, String hermesConfigPath) throws IOException, MalformedURLException,
            NamingException {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
        if (hermesClassLoader == null) {
            addHermesJarsToClasspath();
        }

        if (contextMap.containsKey(key)) {
            return contextMap.get(key);
        }

        // ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(hermesClassLoader);
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "hermes.HermesInitialContextFactory");
            props.put(Context.PROVIDER_URL, hermesConfigPath + File.separator + HERMES_CONFIG_XML);
            props.put("hermes.loader", "hermes.JAXBHermesLoader");
            Context ctx = new InitialContext(props);
            contextMap.put(key, ctx);
            return ctx;
        } finally {
            state.restore();
        }
    }

    private static void addHermesJarsToClasspath() throws IOException, MalformedURLException {
        String hermesHome = SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS, defaultHermesJMSPath());

        if (hermesHome == null || "".equals(hermesHome)) {
            hermesHome = createHermesHomeSetting();
            if (hermesHome == null) {
                throw new FileNotFoundException("HermesJMS home not specified !!!");
            }
        }

        System.setProperty("hermes.home", hermesHome);

        String hermesLib = hermesHome + File.separator + "lib";
        File dir = new File(hermesLib);

        File[] children = dir.listFiles();
        if (children != null) {
            ClassLoader currentClassLoader = Hermes.class.getClassLoader();
            List<URL> urls = new ArrayList<URL>();
            for (File file : children) {
                // fix for users using version of hermesJMS which still has
                // cglib-2.1.3.jar in lib directory
                String filename = file.getName();
                if (!filename.endsWith(".jar") || filename.equals("cglib-2.1.3.jar") ||
                        filename.equals("slf4j-jdk14-1.0.1.jar")) {
                    continue;
                }

                if (!mustBeAddedToCurrentClassLoader(filename)) {
                    urls.add(file.toURI().toURL());
                }
            }
            hermesClassLoader = new ReverseOrderClassLoader(urls.toArray(new URL[urls.size()]),
                    currentClassLoader, classesToBeLoadedByParentClassLoader);
        }

    }

    private static boolean mustBeAddedToCurrentClassLoader(String filename) {
        return filename.equals("jide-action.jar") ||
                filename.equals("jaxb-api.jar") ||
                filename.equals("xerces.jar") ||
                filename.equals("xercesImpl.jar") ||
                filename.equals("jide-components.jar") ||
                filename.equals(("xml-apis.jar")) ||
                filename.equals("javax.jms.jar") ||
                filename.equals("jms.jar") ||
                filename.equals("jta-spec1_0_1.jar") ||
                filename.equals("jmx.jar") ||
                filename.equals("jms-jmx.jar");
    }

    public static void flushHermesCache() {
        contextMap.clear();
    }

    private static String createHermesHomeSetting() {
        if (Tools.isEmpty(SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS, defaultHermesJMSPath()))) {
            UISupport.showErrorMessage("HermesJMS Home must be set in global preferences");

            if (UISupport.getMainFrame() != null) {
                if (SoapUIPreferencesAction.getInstance().show(SoapUIPreferencesAction.INTEGRATED_TOOLS)) {
                    return SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS, defaultHermesJMSPath());
                }
            }
        }
        return null;
    }

    public static String defaultHermesJMSPath() {
        try {
            String path = SoapUI.getSettings().getString(ToolsSettings.HERMES_JMS, null);
            if (path == null || "".equals(path)) {
                String temp = System.getProperty("soapui.home").substring(0,
                        System.getProperty("soapui.home").lastIndexOf("bin") - 1);
                path = new File(temp + File.separator + "hermesJMS").getAbsolutePath().toString();
                SoapUI.log("HermesJMS path: " + path);
            }
            setHermesJMSPath(path);
            return path;
        } catch (Exception e) {
            SoapUI.log("No HermesJMS on default path %SOAPUI_HOME%/hermesJMS");
            return null;
        }

    }

    public static void setHermesJMSPath(String path) {
        if (path != null) {
            SoapUI.getSettings().setString(ToolsSettings.HERMES_JMS, path);
        }
    }

    /**
     * @param project
     * @param sessionName
     * @return hermes.Hermes
     * @throws NamingException
     */
    public static Hermes getHermes(WsdlProject project, String sessionName) throws NamingException {
        SoapUIClassLoaderState state = SoapUIExtensionClassLoader.ensure();
        try {
            Context ctx = hermesContext(project);

            Hermes hermes = (Hermes) ctx.lookup(sessionName);
            return hermes;
        } catch (NamingException ne) {
            UISupport
                    .showErrorMessage("Hermes configuration is not valid. Please check that 'Hermes Config' project property is set to path of proper hermes-config.xml file");
            SoapUI.logError(ne, "Error when trying to find JMS session");
            throw new NamingException("Session name '" + sessionName
                    + "' does not exist in Hermes configuration or path to Hermes config ( " + project.getHermesConfig()
                    + " )is not valid!");
        } catch (MalformedURLException mue) {
            SoapUI.logError(mue);
        } catch (IOException ioe) {
            SoapUI.logError(ioe);
        } finally {
            state.restore();
        }
        return null;
    }

    public static boolean isHermesJMSSupported() {
        return !UISupport.isIdePlugin();
    }

    public static class ReverseOrderClassLoader extends URLClassLoader {
        final private Set<String> toBeLoadedByParent;

        public ReverseOrderClassLoader(URL[] urls, ClassLoader parent, Set<String> toBeLoadedByParent) {
            super(urls, parent);
            this.toBeLoadedByParent = toBeLoadedByParent != null ? toBeLoadedByParent : Collections.emptySet();
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = innerLoadClass(name, resolve);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

        private Class<?> innerLoadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (toBeLoadedByParent.contains(name)) {
                return super.loadClass(name, resolve);
            }
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }

        @Override
        public URL getResource(String name) {
            URL url = findResource(name);
            if (url == null) {
                url = super.getResource(name);
            }
            return url;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {

            Enumeration<URL> localUrls = findResources(name);

            Enumeration<URL> parentUrls = null;
            if (getParent() != null) {
                parentUrls = getParent().getResources(name);
            }

            final List<URL> urlList = new ArrayList<>();
            addURLsIntoList(urlList, localUrls);
            addURLsIntoList(urlList, parentUrls);

            return Collections.enumeration(urlList);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            URL url = getResource(name);
            try {
                return url != null ? url.openStream() : null;
            } catch (IOException ignore) {
            }
            return null;
        }


        private void addURLsIntoList(List<URL> urlList, Enumeration<URL> urlEnumeration) {
            if (urlEnumeration != null && urlList != null) {
                while (urlEnumeration.hasMoreElements()) {
                    urlList.add(urlEnumeration.nextElement());
                }
            }
        }

    }
}
