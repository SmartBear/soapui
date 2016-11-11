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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for LoadTests
 *
 * @author Ole.Matzura
 */

public class WsdlLoadTestPanelBuilder<T extends WsdlLoadTest> extends EmptyPanelBuilder<T> {
    public WsdlLoadTestPanelBuilder() {
    }

    public DesktopPanel buildDesktopPanel(T loadTest) {
        return new WsdlLoadTestDesktopPanel(loadTest);
    }

    public boolean hasDesktopPanel() {
        return true;
    }
}
