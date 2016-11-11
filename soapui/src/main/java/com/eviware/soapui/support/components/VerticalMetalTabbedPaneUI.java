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

import javax.swing.Icon;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.FontMetrics;
import java.awt.Insets;

public class VerticalMetalTabbedPaneUI extends MetalTabbedPaneUI {
    protected void installDefaults() {
        super.installDefaults();

        textIconGap = 0;
        tabInsets = new Insets(0, 0, 0, 0);
    }

    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        Icon icon = getIconForTab(tabIndex);
        return icon == null ? 2 : icon.getIconWidth() + 2;
    }
}
