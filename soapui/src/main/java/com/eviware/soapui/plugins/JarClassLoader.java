/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.plugins;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends URLClassLoader {

    private static final Logger log = Logger.getLogger(JarClassLoader.class);
    private final ClassLoader parent;

    public JarClassLoader(File jarFile, ClassLoader parent) throws IOException {
        super(new URL[]{jarFile.toURI().toURL()}, null);
        this.parent = parent;
        addLibrariesIn(new JarFile(jarFile));
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            return parent.loadClass(name);
        }
    }

    private void addLibrariesIn(JarFile jarFile) throws IOException {
        if (hasLibraries(jarFile)) {
            File libDirectory = createLibDirectory();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (isLibrary(jarEntry)) {
                    String fileName = jarEntry.getName().substring(4);
                    File outputFile = new File(libDirectory, fileName);
                    FileUtils.copyInputStreamToFile(jarFile.getInputStream(jarEntry), outputFile);
                    this.addURL(outputFile.toURI().toURL());
                }
            }
        }
    }

    private File createLibDirectory() throws IOException {
        String libDirectoryName = UUID.randomUUID().toString();
        final File libDirectory = new File(System.getProperty("java.io.tmpdir"), libDirectoryName);
        if (!libDirectory.mkdir()) {
            throw new IOException("Could not create directory for unpacked JAR libraries at " + libDirectory);
        }
        deleteDirectoryOnExit(libDirectory);
        return libDirectory;
    }

    private void deleteDirectoryOnExit(final File libDirectory) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(libDirectory);
                } catch (IOException e) {
                    log.warn("Could not delete temporary directory " + libDirectory);
                }
            }
        }));
    }

    private boolean hasLibraries(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (isLibrary(jarEntry)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLibrary(JarEntry jarEntry) {
        return jarEntry.getName().startsWith("lib/") && jarEntry.getName().endsWith(".jar");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        JarClassLoader jarClassLoader = new JarClassLoader(new File("/Users/manne/projekt/soapui-pro/soapui-pro/test.jar"), ClassLoader.getSystemClassLoader());
        Class<?> aClass = jarClassLoader.loadClass("com.eviware.soapui.SoapUIPro");
        System.out.println(aClass.getProtectionDomain().getCodeSource().getLocation());
    }


}
