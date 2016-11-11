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

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.actions.MarkerAction;
import com.eviware.soapui.support.components.JXToolBar;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.util.HashMap;
import java.util.Map;

public class JXSoapUIActionListToolBar extends JXToolBar {
    private Map<String, Action> actionMap = new HashMap<String, Action>();

    @SuppressWarnings("unchecked")
    public JXSoapUIActionListToolBar(ActionList actions, ModelItem modelItem) {
        addSpace(1);
        setRollover(true);
        putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
        setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

        for (int i = 0; i < actions.getActionCount(); i++) {
            Action action = actions.getActionAt(i);

            if (action instanceof MarkerAction) {
                continue;
            }

            if (action == ActionSupport.SEPARATOR_ACTION) {
                addSeparator();
            } else if (action instanceof ActionSupport.ActionListAction) {
                // JMenu subMenu = buildMenu(
                // ((ActionListAction)action).getActionList() );
                // if( subMenu == null )
                // subMenu = new JMenu(
                // ((ActionListAction)action).getActionList().getLabel() );
                // menu.add( subMenu);
            } else if (action != null) {
                JComponent component = null;

                if (action instanceof SoapUIActionMarker) {
                    SoapUIAction soapUIAction = ((SoapUIActionMarker) action).getSoapUIAction();
                    component = ActionComponentRegistry.buildActionComponent(soapUIAction, modelItem);
                    actionMap.put(soapUIAction.getId(), action);
                }

                if (component != null) {
                    add(component);
                } else {
                    add(action);
                }
            }
        }
    }

    public JXSoapUIActionListToolBar(ModelItem modelItem) {
        this(ActionListBuilder.buildActions(modelItem, "EditorToolbar"), modelItem);
    }

    public void setEnabled(String actionId, boolean enabled) {
        if (actionMap.containsKey(actionId)) {
            actionMap.get(actionId).setEnabled(enabled);
        }
    }

}
