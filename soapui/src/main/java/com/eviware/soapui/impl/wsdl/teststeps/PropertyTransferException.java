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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.model.testsuite.TestProperty;

/**
 * Exception than can occur during property-transfers
 *
 * @author ole.matzura
 */

public class PropertyTransferException extends Exception {
    private String message;
    private final String sourceStepName;
    private final String targetStepName;
    private String sourcePropertyName;
    private String sourcePropertyValue;
    private String targetPropertyName;
    private String targetPropertyValue;

    public PropertyTransferException(String message, String sourceStepName, TestProperty source, String targetStepName,
                                     TestProperty target) {
        this.message = message;
        this.sourceStepName = sourceStepName;
        this.targetStepName = targetStepName;

        if (source != null) {
            sourcePropertyName = source.getName();
            sourcePropertyValue = source.getValue();
        }
        if (target != null) {
            targetPropertyName = target.getName();
            targetPropertyValue = target.getValue();
        }
    }

    public String getMessage() {
        return message;
    }

    public String getSourcePropertyName() {
        return sourcePropertyName;
    }

    public String getSourcePropertyValue() {
        return sourcePropertyValue;
    }

    public String getSourceStepName() {
        return sourceStepName;
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public String getTargetPropertyValue() {
        return targetPropertyValue;
    }

    public String getTargetStepName() {
        return targetStepName;
    }
}
