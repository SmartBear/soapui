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

import com.eviware.soapui.config.MaliciousAttachmentSecurityScanConfig;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.scan.AbstractSecurityScan;
import com.eviware.soapui.security.scan.MaliciousAttachmentSecurityScan;

/**
 * Factory for creation GroovyScript steps
 *
 * @author SoapUI team
 */

public class MaliciousAttachmentSecurityScanFactory extends AbstractSecurityScanFactory {

    public MaliciousAttachmentSecurityScanFactory() {
        super(MaliciousAttachmentSecurityScan.TYPE, MaliciousAttachmentSecurityScan.NAME,
                "Performs a scan for Malicious Attachment Vulnerabilities", "/malicious_attachment_scan.gif");
    }

    public boolean canCreate(TestStep testStep) {
        return testStep instanceof WsdlTestRequestStep;
    }

    @Override
    public AbstractSecurityScan buildSecurityScan(TestStep testStep, SecurityScanConfig config, ModelItem parent) {
        return new MaliciousAttachmentSecurityScan(testStep, config, parent, "/malicious_attachment_scan.gif");
    }

    @Override
    public SecurityScanConfig createNewSecurityScan(String name) {
        SecurityScanConfig securityCheckConfig = SecurityScanConfig.Factory.newInstance();
        securityCheckConfig.setType(MaliciousAttachmentSecurityScan.TYPE);
        securityCheckConfig.setName(name);
        MaliciousAttachmentSecurityScanConfig sic = MaliciousAttachmentSecurityScanConfig.Factory.newInstance();
        securityCheckConfig.setConfig(sic);
        return securityCheckConfig;
    }

}
