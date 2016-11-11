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

package com.eviware.soapui.impl.rest.panels.request;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class that determines what parameter has been clicked in a parameters string.
 */
class ParameterFinder {


    private List<String> tokens;

    public ParameterFinder(String parametersString) {
        StringTokenizer parser = new StringTokenizer(parametersString, "?&=;", true);
        List<String> parsedTokens = new ArrayList<String>();
        while (parser.hasMoreTokens()) {
            parsedTokens.add(parser.nextToken());
        }
        tokens = parsedTokens;
    }

    public String findParameterAt(int dot) {
        int tokenIndex = getTokenIndexForChar(dot == 0 ? 1 : dot);
        if (tokenIndex == -1) {
            // shouldn't really happen, but just in case ...
            return "";
        }
        String token = tokens.get(tokenIndex);
        if (isSeparator(token)) {
            return tokenIndex < tokens.size() - 1 ? tokens.get(tokenIndex + 1) : "";
        } else if (token.equals("=")) {
            return tokenIndex > 1 ? tokens.get(tokenIndex - 1) : "";
        }
        if (tokenIndex > 1 && tokens.get(tokenIndex - 1).equals("=")) {
            return tokens.get(tokenIndex - 2);
        }
        return token;
    }

    private boolean isSeparator(String token) {
        return token.equals("&") || token.equals(";");
    }

    private int getTokenIndexForChar(int index) {
        int currentIndex = 0;
        int tokenIndex = 0;
        for (String token : tokens) {
            if (index >= currentIndex && index < currentIndex + token.length()) {
                return tokenIndex;
            }
            currentIndex += token.length();
            tokenIndex++;
        }
        return tokens.size() - 1;
    }

}
