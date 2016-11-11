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

import com.eviware.soapui.SoapUI;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.io.File;

public class ScriptingSupport {
    public static SoapUIGroovyShell createGsroovyShell(Binding binding) {
        // LoaderConfiguration config = new LoaderConfiguration();
        //
        // String libraries = SoapUI.getSettings().getString(
        // ToolsSettings.SCRIPT_LIBRARIES, null );
        // if( libraries != null )
        // {
        // File libs = new File( libraries );
        // File[] list = libs.listFiles();
        //
        // for( File lib : list)
        // {
        // if( lib.getName().toLowerCase().endsWith( ".jar" ))
        // {
        // config.addFile( lib );
        // }
        // }
        // }

        // RootLoader loader = new RootLoader( config.getClassPathUrls(), );
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(SoapUI.class.getClassLoader());
        SoapUIGroovyShell groovyShell = binding == null ? new SoapUIGroovyShell(groovyClassLoader)
                : new SoapUIGroovyShell(groovyClassLoader, binding);

        return groovyShell;
    }

    public static class SoapUIGroovyShell extends GroovyShell {
        private final GroovyClassLoader classLoader;

        protected SoapUIGroovyShell(GroovyClassLoader classLoader, Binding binding) {
            super(classLoader, binding);

            this.classLoader = classLoader;

            reloadExternalClasses();
        }

        protected SoapUIGroovyShell(GroovyClassLoader classLoader) {
            super(classLoader);

            this.classLoader = classLoader;

            reloadExternalClasses();
        }

        public void reloadExternalClasses() {
            resetLoadedClasses();
            classLoader.clearCache();

            try {
                File scripts = new File(new File("").getAbsolutePath() + File.separatorChar + "scripts");
                if (scripts.exists() && scripts.isDirectory()) {
                    File[] listFiles = scripts.listFiles();
                    for (File file : listFiles) {
                        if (file.isDirectory() || !file.getName().endsWith(".groovy")) {
                            continue;
                        }

                        System.out.println("parsing " + file.getAbsolutePath());
                        classLoader.parseClass(file);
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e);
            }
        }
    }
}
