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

import com.eviware.soapui.config.GroovySecurityScanConfig;
import com.eviware.soapui.config.ScriptConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.GroovySecurityScan;

/**
 * Factory for creation GroovyScript steps
 *
 * @author SoapUI team
 */

public class GroovySecurityScanFactory extends AbstractSecurityScanFactory {

    public GroovySecurityScanFactory() {
        super(GroovySecurityScan.TYPE, GroovySecurityScan.NAME,
                "Executes the specified groovy script for security scan", "/groovy_script_scan.gif");
    }

    public boolean canCreate(TestStep testStep) {
        return true;
    }

    @Override
    public AbstractSecurityScan buildSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent) {
        return new GroovySecurityScan(testStep, config, parent, "/groovy_script_scan.gif");
    }

    @Override
    public SecurityScanConfig createNewSecurityScan(String name) {
        SecurityScanConfig securityCheckConfig = SecurityScanConfig.Factory.newInstance();
        securityCheckConfig.setType(GroovySecurityScan.TYPE);
        securityCheckConfig.setName(name);
        GroovySecurityScanConfig groovyscc = GroovySecurityScanConfig.Factory.newInstance();
        groovyscc.setExecuteScript(ScriptConfig.Factory.newInstance());
        // securityCheckConfig.changeType( GroovySecurityScanConfig.type );
        securityCheckConfig.setConfig(groovyscc);
        return securityCheckConfig;
    }

}
