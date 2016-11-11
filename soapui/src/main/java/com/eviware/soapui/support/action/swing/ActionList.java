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

package com.eviware.soapui.support.action.swing;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * A simple list of actions
 *
 * @author Ole.Matzura
 */

public interface ActionList {
    public int getActionCount();

    public Action getActionAt(int index);

    public Action getDefaultAction();

    public boolean hasDefaultAction();

    public void performDefaultAction(ActionEvent event);

    public void addAction(Action action);

    public void addSeparator();

    public void insertAction(Action action, int index);

    public void insertSeparator(int index);

    public String getLabel();

    public void clear();

    public void dispatchKeyEvent(KeyEvent e);

    public void addActions(ActionList defaultActions);

    public void setDefaultAction(Action action);

    public void removeAction(int index);
}
