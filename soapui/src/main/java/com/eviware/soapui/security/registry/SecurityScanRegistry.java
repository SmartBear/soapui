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

package com.eviware.soapui.security.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ui.SecurityConfigurationDialogBuilder;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of SecurityScan factories
 *
 * @author SoapUI team
 */

public class SecurityScanRegistry {
    protected static SecurityScanRegistry instance;
    private Map<String, SecurityScanFactory> availableSecurityChecks = new HashMap<String, SecurityScanFactory>();
    private StringToStringMap securityCheckNames = new StringToStringMap();

    public SecurityScanRegistry() {
        addFactory(new GroovySecurityScanFactory());
        addFactory(new CrossSiteScriptingScanFactory());
        addFactory(new XmlBombSecurityScanFactory());
        addFactory(new MaliciousAttachmentSecurityScanFactory());
        addFactory(new XPathInjectionSecurityScanFactory());
        addFactory(new InvalidTypesSecurityScanFactory());
        addFactory(new BoundarySecurityScanFactory());
        addFactory(new SQLInjectionScanFactory());
        addFactory(new MalformedXmlSecurityScanFactory());
        addFactory(new FuzzerSecurityScanFactory());

        for (SecurityScanFactory factory : SoapUI.getFactoryRegistry().getFactories(SecurityScanFactory.class)) {
            addFactory(factory);
        }

    }

    /**
     * Gets the right SecurityScan Factory, depending on the type
     *
     * @param type The securityScan to get the factory for
     * @return
     */
    public SecurityScanFactory getFactory(String type) {
        for (String cc : availableSecurityChecks.keySet()) {
            SecurityScanFactory scf = availableSecurityChecks.get(cc);
            if (scf.getSecurityScanType().equals(type)) {
                return scf;
            }

        }
        return null;
    }

    /**
     * Gets the right SecurityScan Factory using name
     *
     * @param name The securityScan name to get the factory for
     * @return
     */
    public SecurityScanFactory getFactoryByName(String name) {
        String type = getSecurityScanTypeForName(name);

        if (type != null) {
            return getFactory(type);
        }

        return null;
    }

    /**
     * Adding a new factory to the registry
     *
     * @param factory
     */
    public void addFactory(SecurityScanFactory factory) {
        removeFactory(factory.getSecurityScanType());
        availableSecurityChecks.put(factory.getSecurityScanName(), factory);
        securityCheckNames.put(factory.getSecurityScanName(), factory.getSecurityScanType());
    }

    /**
     * Removing a factory from the registry
     *
     * @param type
     */
    public void removeFactory(String type) {
        for (String scfName : availableSecurityChecks.keySet()) {
            SecurityScanFactory csf = availableSecurityChecks.get(scfName);
            if (csf.getSecurityScanType().equals(type)) {
                availableSecurityChecks.remove(scfName);
                securityCheckNames.remove(scfName);
                break;
            }
        }
    }

    /**
     * @return The registry instance
     */
    public static synchronized SecurityScanRegistry getInstance() {
        if (instance == null) {
            instance = new SecurityScanRegistry();
        }

        return instance;
    }

    /**
     * Checking if the registry contains a factory.
     *
     * @param config A configuration to check the factory for
     * @return
     */
    public boolean hasFactory(SecurityScanConfig config) {
        return getFactory(config.getType()) != null;
    }

    /**
     * Returns the list of available scans
     *
     * @param monitorOnly Set this to true to get only the list of scans which can be
     *                    used in the http monitor
     * @return A String Array containing the names of all the scans
     */
    public String[] getAvailableSecurityScansNames() {
        List<String> result = new ArrayList<String>();

        for (SecurityScanFactory securityCheck : availableSecurityChecks.values()) {
            result.add(securityCheck.getSecurityScanName());
        }

        String[] sortedResult = result.toArray(new String[result.size()]);
        Arrays.sort(sortedResult);

        return sortedResult;
    }

    // TODO drso: test and implement properly
    public String[] getAvailableSecurityScansNames(TestStep testStep) {
        List<String> result = new ArrayList<String>();

        for (SecurityScanFactory securityCheck : availableSecurityChecks.values()) {
            if (securityCheck.canCreate(testStep)) {
                result.add(securityCheck.getSecurityScanName());
            }
        }

        String[] sortedResult = result.toArray(new String[result.size()]);
        Arrays.sort(sortedResult);

        return sortedResult;
    }

    public SecurityConfigurationDialogBuilder getUIBuilder() {
        return new SecurityConfigurationDialogBuilder();
    }

    public String getSecurityScanTypeForName(String name) {
        return securityCheckNames.get(name);
    }

}
