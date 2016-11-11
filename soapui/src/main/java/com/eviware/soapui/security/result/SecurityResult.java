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

package com.eviware.soapui.security.result;

import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Interface for all result classes used in Security testing
 *
 * @author dragica.soldo
 */
public interface SecurityResult {
    /**
     * INITIALIZED - just started, for distinguishing if icon should be added in
     * the security log UNKNOWN - when no assertions are added OK - finished with
     * no errors/warnings FAILED CANCELED note:
     * MISSING_ASSERTIONS,MISSING_PARAMETERS - are used only for indicating
     * progress execution for security log entry icons
     */
    public enum ResultStatus {
        INITIALIZED, UNKNOWN, OK, FAILED, CANCELED, MISSING_ASSERTIONS, MISSING_PARAMETERS, SKIPPED
    }

    /**
     * Gets type of specific result, i.e. SecurityTestStep, SecurityCheck or
     * SecurityCheckRequest used in displaying result details from SecurityLog
     *
     * @return
     */
    public String getResultType();

    /**
     * Gets execution progress status used for indicating icon color in the
     * SecurityLog introduced in general in case of missing assertions and
     * missing parameters to match status in progress bars and yet not to need
     * resultStatus changed
     *
     * @return
     */
    public ResultStatus getExecutionProgressStatus();

    public ResultStatus getLogIconStatus();

    public ResultStatus getStatus();

    /**
     * Returns a list of actions that can be applied to this result
     */

    public ActionList getActions();
}
