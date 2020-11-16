/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.tools;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Removes property expansions from an input string.
 */
public class PropertyExpansionRemover {

    private static final Logger log = LogManager.getLogger(PropertyExpansionRemover.class);

    public static final String EXPANSION_START = "${";

    public static String removeExpansions(String input) {
        if (input == null) {
            return null;
        }
        String output = input;
        while (containsPropertyExpansion(output)) {
            output = removeExpansionAt(output, output.indexOf(EXPANSION_START));
        }
        return output;
    }

    private static String removeExpansionAt(String input, int startIndex) {
        String output = input;
        while (containsNestedExpansion(output, startIndex)) {
            output = removeExpansionAt(output, output.indexOf(EXPANSION_START, startIndex + 1));
        }
        int endIndex = output.indexOf('}', startIndex);
        return endIndex == -1 ? output : output.substring(0, startIndex) + output.substring(endIndex + 1);
    }

    private static boolean containsNestedExpansion(String output, int startIndex) {
        String textToProcess = output.substring(startIndex + EXPANSION_START.length());
        return textToProcess.contains(EXPANSION_START) &&
                textToProcess.indexOf(EXPANSION_START) < textToProcess.indexOf('}');
    }

    private static boolean containsPropertyExpansion(String input) {
        if (input == null || !input.contains(EXPANSION_START)) {
            return false;
        }
        int startIndex = input.indexOf(EXPANSION_START);
        return input.indexOf('}', startIndex) != -1;
    }
}
