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

import com.eviware.soapui.support.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of LoadFactorys
 *
 * @author Ole.Matzura
 */

public class LoadStrategyRegistry {
    private static LoadStrategyRegistry instance;
    private Map<String, LoadStrategyFactory> factories = new HashMap<String, LoadStrategyFactory>();

    public LoadStrategyRegistry() {
        addFactory(new SimpleLoadStrategy.Factory());
        addFactory(new BurstLoadStrategy.Factory());
        addFactory(new VarianceLoadStrategy.Factory());
        addFactory(new ThreadCountChangeLoadStrategy.Factory());
    }

    public void addFactory(LoadStrategyFactory factory) {
        factories.put(factory.getType(), factory);
    }

    public String[] getStrategies() {
        return StringUtils.sortNames(factories.keySet().toArray(new String[factories.size()]));
    }

    public static LoadStrategyRegistry getInstance() {
        if (instance == null) {
            instance = new LoadStrategyRegistry();
        }

        return instance;
    }

    public LoadStrategyFactory getFactory(String type) {
        return factories.get(type);
    }
}
