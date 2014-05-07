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

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * JavaScript token marker.
 *
 * @author Slava Pestov
 * @version $Id: JavaScriptTokenMarker.java,v 1.3 1999/12/13 03:40:29 sp Exp $
 */
public class JavaScriptTokenMarker extends CTokenMarker {
    public JavaScriptTokenMarker() {
        super(false, getKeywords());
    }

    public static KeywordMap getKeywords() {
        if (javaScriptKeywords == null) {
            javaScriptKeywords = new KeywordMap(false);
            javaScriptKeywords.add("function", Token.KEYWORD3);
            javaScriptKeywords.add("var", Token.KEYWORD3);
            javaScriptKeywords.add("else", Token.KEYWORD1);
            javaScriptKeywords.add("for", Token.KEYWORD1);
            javaScriptKeywords.add("if", Token.KEYWORD1);
            javaScriptKeywords.add("in", Token.KEYWORD1);
            javaScriptKeywords.add("new", Token.KEYWORD1);
            javaScriptKeywords.add("return", Token.KEYWORD1);
            javaScriptKeywords.add("while", Token.KEYWORD1);
            javaScriptKeywords.add("with", Token.KEYWORD1);
            javaScriptKeywords.add("break", Token.KEYWORD1);
            javaScriptKeywords.add("case", Token.KEYWORD1);
            javaScriptKeywords.add("continue", Token.KEYWORD1);
            javaScriptKeywords.add("default", Token.KEYWORD1);
            javaScriptKeywords.add("false", Token.LABEL);
            javaScriptKeywords.add("this", Token.LABEL);
            javaScriptKeywords.add("true", Token.LABEL);
        }
        return javaScriptKeywords;
    }

    // private members
    private static KeywordMap javaScriptKeywords;
}
