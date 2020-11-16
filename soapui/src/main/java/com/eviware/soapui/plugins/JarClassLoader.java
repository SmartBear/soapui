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

package com.eviware.soapui.plugins;

import com.eviware.soapui.support.Tools;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends URLClassLoader implements PluginClassLoader {

    private static final Logger log = LogManager.getLogger(JarClassLoader.class);
    public static final String LIB_PREFIX = "lib/";
    private final ClassLoader parent;
    private Collection<JarClassLoader> dependencyClassLoaders;
    private GroovyClassLoader scriptClassLoader;

    public JarClassLoader(File jarFile, ClassLoader parent, Collection<JarClassLoader> dependencyClassLoaders) throws IOException {
        super(new URL[]{jarFile.toURI().toURL()}, null);
        this.parent = parent;
        this.dependencyClassLoaders = dependencyClassLoaders;
        JarFile file = new JarFile(jarFile);
        addLibrariesIn(file);
        addScriptsIn(file);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            for (JarClassLoader dependencyClassLoader : dependencyClassLoaders) {
                try {
                    return dependencyClassLoader.loadClass(name);
                }
                catch (ClassNotFoundException ignore) {
                }
                catch (NoClassDefFoundError ignore) {
                }
            }
            return parent.loadClass(name);
        }
        catch (NoClassDefFoundError e) {
            for (JarClassLoader dependencyClassLoader : dependencyClassLoaders) {
                try {
                    return dependencyClassLoader.loadClass(name);
                }
                catch (ClassNotFoundException ignore) {

                }
                catch (NoClassDefFoundError ignore) {

                }
            }
            return parent.loadClass(name);
        }
    }

    private void addLibrariesIn(JarFile jarFile) throws IOException {
        if (containsLibraries(jarFile)) {
            File libDirectory = Tools.createTemporaryDirectory();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (isLibrary(jarEntry)) {
                    String fileName = jarEntry.getName().substring(LIB_PREFIX.length());
                    File outputFile = new File(libDirectory, fileName);
                    FileUtils.copyInputStreamToFile(jarFile.getInputStream(jarEntry), outputFile);
                    this.addURL(outputFile.toURI().toURL());
                }
            }
        }
    }

    private void addScriptsIn(JarFile jarFile) throws IOException {

        boolean hasScripts = false;

        if (containsScripts(jarFile)) {
            File scriptsDirectory = Tools.createTemporaryDirectory();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (isScript(jarEntry)) {
                    String pathToScript = jarEntry.getName();

                    File outputFile = null;
                    int lastSlashIndex = pathToScript.lastIndexOf('/');
                    if (lastSlashIndex >= 0) {
                        File packageDirectory = new File(scriptsDirectory, pathToScript.substring(0, lastSlashIndex));
                        if (!packageDirectory.exists() || !packageDirectory.isDirectory()) {
                            if (!packageDirectory.mkdirs()) {
                                log.error("Failed to create directory for [" + pathToScript + "]");
                                packageDirectory = null;
                            }
                        }

                        if (packageDirectory != null) {
                            outputFile = new File(packageDirectory, pathToScript.substring(lastSlashIndex + 1));
                        }
                    }

                    if (outputFile != null) {
                        FileUtils.copyInputStreamToFile(jarFile.getInputStream(jarEntry), outputFile);
                        hasScripts = true;
                    }
                }
            }

            /*
            if (hasScripts) {
                URL scriptsUrl = scriptsDirectory.toURI().toURL();
                SoapUIPro.getSoapUIGroovyClassLoader().addURL(scriptsUrl);
                scriptClassLoader = new GroovyClassLoader(SoapUIPro.getSoapUIGroovyClassLoader());
                scriptClassLoader.addURL(scriptsUrl);
            }
            */
        }
    }

    private boolean containsLibraries(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (isLibrary(jarEntry)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsScripts(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (isScript(jarEntry)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLibrary(JarEntry jarEntry) {
        return jarEntry.getName().startsWith(LIB_PREFIX) && jarEntry.getName().endsWith(".jar");
    }

    private boolean isScript(JarEntry jarEntry) {
        return jarEntry.getName().endsWith(".groovy");
    }


    public boolean hasScripts() {
        return scriptClassLoader != null;
    }

    public Class loadScriptClass(String path) throws ClassNotFoundException {
        path = path.substring(0, path.length() - ".groovy".length());
        return scriptClassLoader.loadClass(path, true, true, true);
    }

    public GroovyClassLoader getScriptClassLoader() {
        return scriptClassLoader;
    }
}
