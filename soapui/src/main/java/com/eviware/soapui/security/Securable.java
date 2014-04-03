/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.security;

/**
 * Behavior for an object that can be securityScanned this stays for now in case
 * we decide we need it later, for now it's not used
 *
 * @author dragica.soldo
 */

public interface Securable {
    // public SecurityCheck addSecurityCheck( String checkType, String checkName
    // );
    //
    // public void addSecurityChecksListener( SecurityTestListener listener );
    //
    // public int getSecurityCheckCount();
    //
    // public SecurityCheck getSecurityCheckAt( int c );
    //
    // public void removeSecurityChecksListener( SecurityTestListener listener );
    //
    // public void removeSecurityCheck( SecurityCheck securityCheck );
    //
    // // public AssertionStatus getAssertionStatus();
    // //
    // // public enum AssertionStatus
    // // {
    // // UNKNOWN, VALID, FAILED
    // // }
    //
    // // public String getAssertableContent();
    //
    // // public String getDefaultAssertableContent();
    //
    // // public AssertableType getAssertableType();
    //
    // public List<SecurityCheck> getSecurityCheckList();
    //
    // public SecurityCheck getSecurityCheckByName( String name );
    //
    // public ModelItem getModelItem();
    //
    // public Interface getInterface();
    //
    // public SecurityCheck cloneSecurityCheck( SecurityCheck source, String name
    // );
    //
    // public Map<String, SecurityCheck> getSecurityChecks();
    //
    // public SecurityCheck moveSecurityCheck( int ix, int offset );
}
