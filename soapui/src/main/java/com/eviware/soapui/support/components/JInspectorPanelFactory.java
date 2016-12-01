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

package com.eviware.soapui.support.components;

import com.eviware.soapui.SoapUI;

import javax.swing.JComponent;

public class JInspectorPanelFactory {
    public static Class<? extends JInspectorPanel> inspectorPanelClass = JInspectorPanelImpl.class;

    public static JInspectorPanel build(JComponent contentComponent) {
        try {
            return inspectorPanelClass.getConstructor(JComponent.class).newInstance(contentComponent);
        } catch (Throwable e) {
            SoapUI.logError(e);
            return null;
        }
    }

    public static JInspectorPanel build(JComponent contentComponent, int orientation) {
        try {
            return inspectorPanelClass.getConstructor(JComponent.class, int.class).newInstance(contentComponent,
                    orientation);
        } catch (Throwable e) {
            SoapUI.logError(e);
            return null;
        }
    }
}
