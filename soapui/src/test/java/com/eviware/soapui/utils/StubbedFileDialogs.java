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

package com.eviware.soapui.utils;

import com.eviware.x.dialogs.XFileDialogs;

import java.io.File;

public class StubbedFileDialogs implements XFileDialogs {

    @Override
    public File saveAs(Object action, String title, String extension, String fileType, File defaultFile) {
        return null;
    }

    @Override
    public File saveAs(Object action, String title) {
        return null;
    }

    @Override
    public File saveAsDirectory(Object action, String title, File defaultDirectory) {
        return null;
    }

    @Override
    public File open(Object action, String title, String extension, String fileType, String current) {
        return null;
    }

    @Override
    public File openXML(Object action, String title) {
        return null;
    }

    @Override
    public File openDirectory(Object action, String string, File defaultDirectory) {
        return null;
    }

    @Override
    public File openFileOrDirectory(Object action, String title, File defaultDirectory) {
        return null;
    }
}
