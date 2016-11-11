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

package com.eviware.soapui.impl.wsdl.loadtest.strategy;

import com.eviware.soapui.model.testsuite.LoadTestRunContext;
import com.eviware.soapui.model.testsuite.LoadTestRunListener;
import com.eviware.soapui.model.testsuite.LoadTestRunner;
import org.apache.xmlbeans.XmlObject;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * Strategy used by WsdlLoadTest for controlling requests in each thread
 *
 * @author Ole.Matzura
 */

public interface LoadStrategy extends LoadTestRunListener {
    public final static String CONFIGURATION_PROPERTY = "configuration_property";

    public void addConfigurationChangeListener(PropertyChangeListener listener);

    public void removeConfigurationChangeListener(PropertyChangeListener listener);

    public XmlObject getConfig();

    public String getType();

    public JComponent getConfigurationPanel();

    public void updateConfig(XmlObject config);

    public boolean allowThreadCountChangeDuringRun();

    public void recalculate(LoadTestRunner loadTestRunner, LoadTestRunContext context);
}
