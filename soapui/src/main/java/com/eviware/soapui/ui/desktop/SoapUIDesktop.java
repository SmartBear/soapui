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

package com.eviware.soapui.ui.desktop;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;

import javax.swing.JComponent;

/**
 * Behaviour for a SoapUI Desktop implementation
 *
 * @author ole.matzura
 */

public interface SoapUIDesktop {
    public boolean closeDesktopPanel(DesktopPanel desktopPanel);

    public boolean hasDesktopPanel(ModelItem modelItem);

    public void addDesktopListener(DesktopListener listener);

    public void removeDesktopListener(DesktopListener listener);

    public DesktopPanel showDesktopPanel(ModelItem modelItem);

    public boolean closeDesktopPanel(ModelItem modelItem);

    public ActionList getActions();

    public DesktopPanel[] getDesktopPanels();

    public DesktopPanel getDesktopPanel(ModelItem modelItem);

    public DesktopPanel showDesktopPanel(DesktopPanel desktopPanel);

    public JComponent getDesktopComponent();

    public void transferTo(SoapUIDesktop newDesktop);

    public boolean closeAll();

    public void release();

    public void init();

    public void minimize(DesktopPanel desktopPanel);

    public void maximize(DesktopPanel dp);

    public void showInspector( JComponent component );
}
