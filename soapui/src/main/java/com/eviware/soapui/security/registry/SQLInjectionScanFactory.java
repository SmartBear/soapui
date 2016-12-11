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

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.SQLInjectionScan;

/**
 * Factory for creation GroovyScript steps
 *
 * @author SoapUI team
 */

public class SQLInjectionScanFactory extends AbstractSecurityScanFactory {

    public SQLInjectionScanFactory() {
        super(SQLInjectionScan.TYPE, SQLInjectionScan.NAME, "Preforms a scan for SQL Injection Vulnerabilities",
                "/sql_injection_scan.gif");
    }

    public boolean canCreate(TestStep testStep) {
        return testStep instanceof WsdlTestRequestStep || testStep instanceof RestTestRequestStep
                || testStep instanceof HttpTestRequestStep;
    }

    @Override
    public AbstractSecurityScan buildSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent) {
        return new SQLInjectionScan(testStep, config, parent, "/sql_injection_scan.gif");
    }

    @Override
    public SecurityScanConfig createNewSecurityScan(String name) {
        SecurityScanConfig securityCheckConfig = SecurityScanConfig.Factory.newInstance();
        securityCheckConfig.setType(SQLInjectionScan.TYPE);
        securityCheckConfig.setName(name);
        return securityCheckConfig;
    }

}
