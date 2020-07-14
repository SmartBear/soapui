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

package com.eviware.soapui.support;

import hermes.HermesLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HermesJMSClasspathHacker {
    private static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }// end method

    private static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }// end method

    private static void addURL(URL u) throws IOException {
        ClassLoader classLoader = HermesLoader.class.getClassLoader();
        // ClasspathHacker.addUrlToClassLoader( u, classLoader );
    }// end method

}// end class
