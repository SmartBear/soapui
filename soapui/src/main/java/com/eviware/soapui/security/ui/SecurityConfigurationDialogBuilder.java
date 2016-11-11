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

package com.eviware.soapui.security.ui;

import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class SecurityConfigurationDialogBuilder {

    public SecurityConfigurationDialog buildSecurityScanConfigurationDialog(SecurityScan securityCheck) {
        return new SecurityConfigurationDialog(securityCheck);
    }

    @AForm(description = "Strategy", name = "Strategy")
    protected interface Strategy {

        @AField(description = "Strategy", name = "Select strategy", type = AFieldType.RADIOGROUP)
        public final static String STRATEGY = "Select strategy";

        @AField(description = "Request Delay", name = "Request Delay (ms)", type = AFieldType.INT)
        public final static String DELAY = "Request Delay (ms)";

        @AField(description = "Apply to Failed TestSteps", name = "Apply to Failed TestSteps", type = AFieldType.BOOLEAN)
        public final static String APPLY_TO_FAILED_STEPS = "Apply to Failed TestSteps";

        // indicates if security scan should run only once in case of DataSource
        // Loop involved
        @AField(description = "Run only once", name = "Run only once", type = AFieldType.BOOLEAN)
        public final static String RUN_ONLY_ONCE = "Run only once";

    }

}
