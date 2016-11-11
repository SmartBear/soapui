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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of WsdlTestStep factories
 *
 * @author Ole.Matzura
 */

public class WsdlTestStepRegistry implements SoapUIFactoryRegistryListener {
    private static WsdlTestStepRegistry instance;
    private List<WsdlTestStepFactory> factories = new ArrayList<WsdlTestStepFactory>();

    public WsdlTestStepRegistry() {
        addFactory(new WsdlTestRequestStepFactory());
        addFactory(new RestRequestStepFactory());
        addFactory(new HttpRequestStepFactory());
        addFactory(new AMFRequestStepFactory());
        addFactory(new JdbcRequestTestStepFactory());

        addFactory(new PropertiesStepFactory());
        addFactory(new PropertyTransfersStepFactory());

        addFactory(new ProPlaceholderStepFactory("datasource", "SoapUI Pro DataSource", "/datasource_step.png"));
        addFactory(new ProPlaceholderStepFactory("datasink", "SoapUI Pro DataSink", "/datasink_step.png"));
        addFactory(new ProPlaceholderStepFactory("datagen", "SoapUI Pro DataGen", "/datagen_step.png"));
        addFactory(new ProPlaceholderStepFactory("datasourceloop", "SoapUI Pro DataSourceLoop", "/datasource_loop_step.png"));

        addFactory(new GotoStepFactory());
        addFactory(new RunTestCaseStepFactory());

        addFactory(new GroovyScriptStepFactory());
        addFactory(new ProPlaceholderStepFactory("assertionteststep", "SoapUI Pro Assertion TestStep", "/assertion_test_step.gif"));
        addFactory(new DelayStepFactory());
        addFactory(new WsdlMockResponseStepFactory());
        addFactory(new ManualTestStepFactory());

        for (WsdlTestStepFactory factory : SoapUI.getFactoryRegistry().getFactories(WsdlTestStepFactory.class)) {
            addFactory(factory);
        }

        SoapUI.getFactoryRegistry().addFactoryRegistryListener( this );
    }

    public WsdlTestStepFactory getFactory(String type) {
        for (WsdlTestStepFactory factory : factories) {
            if (factory.getType().equals(type)) {
                return factory;
            }
        }

        return null;
    }

    public void addFactory(WsdlTestStepFactory factory) {
        int replaceIndex = removeFactory(factory.getType());
        if (replaceIndex == -1) {
            factories.add(factory);
        } else {
            factories.add(replaceIndex, factory);
        }
    }

    public int removeFactory(String type) {
        int index = 0;
        for (WsdlTestStepFactory factory : factories) {
            if (factory.getType().equals(type)) {
                factories.remove(factory);
                return index;
            }
            index++;
        }
        return -1;
    }

    public static synchronized WsdlTestStepRegistry getInstance() {
        if (instance == null) {
            instance = new WsdlTestStepRegistry();
        }

        return instance;
    }

    public WsdlTestStepFactory[] getFactories() {
        return factories.toArray(new WsdlTestStepFactory[factories.size()]);
    }

    public boolean hasFactory(TestStepConfig config) {
        return getFactory(config.getType()) != null;
    }

    @Override
    public void factoryAdded(Class<?> factoryType, Object factory) {
        if( factory instanceof WsdlTestStepFactory )
            addFactory((WsdlTestStepFactory) factory);
    }

    @Override
    public void factoryRemoved(Class<?> factoryType, Object factory) {
        if( factory instanceof WsdlTestStepFactory )
            removeFactory(((WsdlTestStepFactory) factory).getType());
    }
}
