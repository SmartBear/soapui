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

package com.eviware.soapui.security.support;

import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.security.SecurityScanParameterListener;

/**
 * Adapter class for SecurityScanParameterListener
 *
 * @author robert
 */
public class SecurityScanParameterListenerAdapter implements SecurityScanParameterListener {

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.security.SecurityScanParameterListener#
     * parameterCheckedChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter)
     */
    @Override
    public void parameterCheckedChanged(SecurityCheckedParameter parameter) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.security.SecurityScanParameterListener#
     * parameterLabelChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void parameterLabelChanged(SecurityCheckedParameter parameter, String oldLabel, String newLabel) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.security.SecurityScanParameterListener#
     * parameterNameChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void parameterNameChanged(SecurityCheckedParameter parameter, String oldName, String newName) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.security.SecurityScanParameterListener#
     * parameterTypeChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void parameterTypeChanged(SecurityCheckedParameter paramter, String oldType, String newType) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see com.eviware.soapui.model.security.SecurityScanParameterListener#
     * parameterXPathChanged
     * (com.eviware.soapui.model.security.SecurityCheckedParameter,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void parameterXPathChanged(SecurityCheckedParameter parameter, String oldXPath, String newXPath) {
        // TODO Auto-generated method stub

    }

}
