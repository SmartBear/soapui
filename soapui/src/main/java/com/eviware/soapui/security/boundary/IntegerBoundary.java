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

package com.eviware.soapui.security.boundary;

/**
 * @author nebojsa.tasic
 */
public class IntegerBoundary extends AbstractBoundary {
    private static final String AVAILABLE_VALUES = "1234567890";

    @Override
    public String outOfBoundary(int restrictionAttribute, String value) {
        switch (restrictionAttribute) {
            case LENGTH:
                return BoundaryUtils.createCharacterArray(AVAILABLE_VALUES, Integer.valueOf(value));
            case MIN_LENGTH:
                return BoundaryUtils.createCharacterArray(AVAILABLE_VALUES, Integer.valueOf(value) - 1);
            case MAX_LENGTH:
                return BoundaryUtils.createCharacterArray(AVAILABLE_VALUES, Integer.valueOf(value) + 1);
            case TOTAL_DIGITS:
                return BoundaryUtils.createCharacterArray(AVAILABLE_VALUES, Integer.valueOf(value) + 1);
            default:
                return null;
        }
    }
}
