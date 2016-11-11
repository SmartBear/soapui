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

package com.eviware.soapui.support.actions;

import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSplitPane;
import java.awt.event.ActionEvent;

/**
 * Changes the orientation of a JSplitPane
 *
 * @author Ole.Matzura
 */

public class ChangeSplitPaneOrientationAction extends AbstractAction {
    private final JSplitPane splitPane;

    public ChangeSplitPaneOrientationAction(JSplitPane splitPane) {
        super();
        this.splitPane = splitPane;

        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/split_request_pane.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Changes the orientation of the request pane split");
        putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt O"));
    }

    public void actionPerformed(ActionEvent e) {
        int orientation = splitPane.getOrientation();
        splitPane.setOrientation(orientation == JSplitPane.HORIZONTAL_SPLIT ? JSplitPane.VERTICAL_SPLIT
                : JSplitPane.HORIZONTAL_SPLIT);
        splitPane.resetToPreferredSizes();
    }
}
