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

import com.eviware.x.dialogs.XDialogs;
import com.eviware.x.dialogs.XProgressDialog;

import java.awt.Component;
import java.awt.Dimension;

public class ConsoleDialogs implements XDialogs {
    public boolean confirm(String question, String title) {
        return false;
    }

    @Override
    public boolean confirm(String question, String title, Component parent) {
        return confirm(question, title);
    }

    public Boolean confirmOrCancel(String question, String title) {
        return null;
    }

    public String prompt(String question, String title, String value) {
        return null;
    }

    public String prompt(String question, String title) {
        return null;
    }

    public String prompt(String question, String title, Object[] objects) {
        return null;
    }

    public String prompt(String question, String title, Object[] objects, String value) {
        return null;
    }

    public void showErrorMessage(String message) {
        System.err.println(message);
    }

    public void showInfoMessage(String message) {
        System.out.println(message);
    }

    public void showInfoMessage(String message, String title) {
        System.out.println(title + ": " + message);
    }

    public XProgressDialog createProgressDialog(String label, int length, String initialValue, boolean canCancel) {
        return new NullProgressDialog();
    }

    public void showExtendedInfo(String title, String description, String content, Dimension size) {
    }

    public boolean confirmExtendedInfo(String title, String description, String content, Dimension size) {
        return false;
    }

    public Boolean confirmOrCancleExtendedInfo(String title, String description, String content, Dimension size) {
        return null;
    }

    public String selectXPath(String title, String info, String xml, String xpath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String selectJsonPath(String title, String info, String json, String jsonPath) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public char[] promptPassword(String question, String title) {
        // TODO Auto-generated method stub
        return null;
    }

    public int yesYesToAllOrNo(String question, String title) {
        // TODO Auto-generated method stub
        return 2;
    }
}
