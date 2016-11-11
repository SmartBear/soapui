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

import com.eviware.soapui.actions.UpdateableAction;

import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Default ActionList implementation
 *
 * @author Ole.Matzura
 */

public class DefaultActionList implements ActionList {
    private List<Action> actions = new ArrayList<Action>();
    private Action defaultAction;
    private final String label;

    public DefaultActionList() {
        this(null);
    }

    public DefaultActionList(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getActionCount() {
        return actions.size();
    }

    public Action getActionAt(int index) {
        return actions.get(index);
    }

    public Action getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(Action defaultAction) {
        this.defaultAction = defaultAction;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void addAction(Action action, boolean isDefault) {
        actions.add(action);
        if (isDefault) {
            setDefaultAction(action);
        }
    }

    public void addSeparator() {
        actions.add(ActionSupport.SEPARATOR_ACTION);
    }

    public void insertAction(Action action, int index) {
        actions.add(index, action);
    }

    public void insertSeparator(int index) {
        actions.add(index, ActionSupport.SEPARATOR_ACTION);
    }

    public boolean hasDefaultAction() {
        return defaultAction != null;
    }

    public void performDefaultAction(ActionEvent event) {
        if (defaultAction != null) {
            defaultAction.actionPerformed(event);
        }
    }

    public void clear() {
        actions.clear();
        defaultAction = null;
    }

    public void dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER && defaultAction != null) {
            performDefaultAction(new ActionEvent(e.getSource(), 0, null));
            e.consume();
        } else {
            for (int c = 0; c < actions.size(); c++) {
                Action action = actions.get(c);
                KeyStroke acc = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
                if (acc == null) {
                    continue;
                }

                if (acc.equals(KeyStroke.getKeyStrokeForEvent(e))) {
                    action.actionPerformed(new ActionEvent(e.getSource(), 0, null));
                    e.consume();
                    return;
                }
            }
        }
    }

    public void addActions(ActionList defaultActions) {
        for (int c = 0; c < defaultActions.getActionCount(); c++) {
            addAction(defaultActions.getActionAt(c));
        }
    }

    public void setEnabled(boolean b) {
        for (int c = 0; c < actions.size(); c++) {
            Action action = actions.get(c);
            action.setEnabled(b);
        }
    }

    public void removeAction(int index) {
        actions.remove(index);
    }

    /**
     * Update all actions that are instances of UpdateableAction.
     */
    public void update() {
        for (Action a : actions) {
            if (a instanceof UpdateableAction) {
                ((UpdateableAction) a).update();
            }
        }
    }

}
