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

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Locale;

/**
 * FileFilter for a specified extensions
 */

final public class ExtensionFileFilter extends FileFilter {
    private final String extension;
    private final String description;

    public ExtensionFileFilter(String extension, String description) {
        this.extension = extension.toLowerCase();
        this.description = description;
    }

    public boolean accept(File f) {
        return f.isDirectory() || "*".equals(extension)
                || f.getName().toLowerCase(Locale.getDefault()).endsWith(extension);
    }

    public String getDescription() {
        return description;
    }
}
