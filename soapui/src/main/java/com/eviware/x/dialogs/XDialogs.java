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

package com.eviware.x.dialogs;

import java.awt.Component;
import java.awt.Dimension;

/**
 * @author Lars
 */

public interface XDialogs {
    void showErrorMessage(String message);

    void showInfoMessage(String message);

    void showInfoMessage(String message, String title);

    void showExtendedInfo(String title, String description, String content, Dimension size);

    boolean confirm(String question, String title);

    boolean confirm(String question, String title, Component parent);

    Boolean confirmOrCancel(String question, String title);

    int yesYesToAllOrNo(String question, String title);

    String prompt(String question, String title, String value);

    String prompt(String question, String title);

    Object prompt(String question, String title, Object[] objects);

    Object prompt(String question, String title, Object[] objects, String value);

    char[] promptPassword(String question, String title);

    XProgressDialog createProgressDialog(String label, int length, String initialValue, boolean canCancel);

    boolean confirmExtendedInfo(String title, String description, String content, Dimension size);

    Boolean confirmOrCancleExtendedInfo(String title, String description, String content, Dimension size);

    String selectXPath(String title, String info, String xml, String xpath);

    String selectJsonPath(String title, String info, String json, String jsonPath);
}
