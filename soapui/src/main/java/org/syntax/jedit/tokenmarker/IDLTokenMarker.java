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
 * IDL token marker.
 *
 * @author Slava Pestov
 * @author Juha Lindfors
 * @version $Id: IDLTokenMarker.java,v 1.2 1999/12/18 06:10:56 sp Exp $
 */
public class IDLTokenMarker extends CTokenMarker {
    public IDLTokenMarker() {
        super(true, getKeywords());
    }

    public static KeywordMap getKeywords() {
        if (idlKeywords == null) {
            idlKeywords = new KeywordMap(false);

            idlKeywords.add("any", Token.KEYWORD3);
            idlKeywords.add("attribute", Token.KEYWORD1);
            idlKeywords.add("boolean", Token.KEYWORD3);
            idlKeywords.add("case", Token.KEYWORD1);
            idlKeywords.add("char", Token.KEYWORD3);
            idlKeywords.add("const", Token.KEYWORD1);
            idlKeywords.add("context", Token.KEYWORD1);
            idlKeywords.add("default", Token.KEYWORD1);
            idlKeywords.add("double", Token.KEYWORD3);
            idlKeywords.add("enum", Token.KEYWORD3);
            idlKeywords.add("exception", Token.KEYWORD1);
            idlKeywords.add("FALSE", Token.LITERAL2);
            idlKeywords.add("fixed", Token.KEYWORD1);
            idlKeywords.add("float", Token.KEYWORD3);
            idlKeywords.add("in", Token.KEYWORD1);
            idlKeywords.add("inout", Token.KEYWORD1);
            idlKeywords.add("interface", Token.KEYWORD1);
            idlKeywords.add("long", Token.KEYWORD3);
            idlKeywords.add("module", Token.KEYWORD1);
            idlKeywords.add("Object", Token.KEYWORD3);
            idlKeywords.add("octet", Token.KEYWORD3);
            idlKeywords.add("oneway", Token.KEYWORD1);
            idlKeywords.add("out", Token.KEYWORD1);
            idlKeywords.add("raises", Token.KEYWORD1);
            idlKeywords.add("readonly", Token.KEYWORD1);
            idlKeywords.add("sequence", Token.KEYWORD3);
            idlKeywords.add("short", Token.KEYWORD3);
            idlKeywords.add("string", Token.KEYWORD3);
            idlKeywords.add("struct", Token.KEYWORD3);
            idlKeywords.add("switch", Token.KEYWORD1);
            idlKeywords.add("TRUE", Token.LITERAL2);
            idlKeywords.add("typedef", Token.KEYWORD3);
            idlKeywords.add("unsigned", Token.KEYWORD3);
            idlKeywords.add("union", Token.KEYWORD3);
            idlKeywords.add("void", Token.KEYWORD3);
            idlKeywords.add("wchar", Token.KEYWORD3);
            idlKeywords.add("wstring", Token.KEYWORD3);
        }
        return idlKeywords;
    }

    // private members
    private static KeywordMap idlKeywords;
}
