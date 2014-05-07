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

/*
 * XMLTokenMarker.java - XML token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 * Copyright (C) 2001 Tom Bradford
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

import javax.swing.text.Segment;

import org.syntax.jedit.SyntaxUtilities;

/**
 * XML Token Marker Rewrite
 *
 * @author Tom Bradford
 * @version $Id$
 */
public class XMLTokenMarker extends TokenMarker {
    public XMLTokenMarker() {
    }

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int length = line.count + offset;

        // Ugly hack to handle multi-line tags
        boolean sk1 = token == Token.KEYWORD1;

        for (int i = offset; i < length; i++) {
            int ip1 = i + 1;
            char c = array[i];
            switch (token) {
                case Token.NULL: // text
                    switch (c) {
                        case '<':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            if (SyntaxUtilities.regionMatches(false, line, ip1, "!--")) {
                                i += 3;
                                token = Token.COMMENT1;
                            } else if (array[ip1] == '!') {
                                i += 1;
                                token = Token.COMMENT2;
                            } else if (array[ip1] == '?') {
                                i += 1;
                                token = Token.KEYWORD3;
                            } else {
                                token = Token.KEYWORD1;
                            }
                            break;

                        case '&':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            token = Token.LABEL;
                            break;
                    }
                    break;

                case Token.KEYWORD1: // tag
                    switch (c) {
                        case '>':
                            addToken(ip1 - lastOffset, token);
                            lastOffset = ip1;
                            token = Token.NULL;
                            sk1 = false;
                            break;

                        case ' ':
                        case '\t':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            token = Token.KEYWORD2;
                            sk1 = false;
                            break;

                        default:
                            if (sk1) {
                                token = Token.KEYWORD2;
                                sk1 = false;
                            }
                            break;
                    }
                    break;

                case Token.KEYWORD2: // attribute
                    switch (c) {
                        case '>':
                            addToken(ip1 - lastOffset, token);
                            lastOffset = ip1;
                            token = Token.NULL;
                            break;

                        case '/':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            token = Token.KEYWORD1;
                            break;

                        case '=':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            token = Token.OPERATOR;
                    }
                    break;

                case Token.OPERATOR: // equal for attribute
                    switch (c) {
                        case '\"':
                        case '\'':
                            addToken(i - lastOffset, token);
                            lastOffset = i;
                            if (c == '\"') {
                                token = Token.LITERAL1;
                            } else {
                                token = Token.LITERAL2;
                            }
                            break;
                    }
                    break;

                case Token.LITERAL1:
                case Token.LITERAL2: // strings
                    if ((token == Token.LITERAL1 && c == '\"') || (token == Token.LITERAL2 && c == '\'')) {
                        addToken(ip1 - lastOffset, token);
                        lastOffset = ip1;
                        token = Token.KEYWORD1;
                    }
                    break;

                case Token.LABEL: // entity
                    if (c == ';') {
                        addToken(ip1 - lastOffset, token);
                        lastOffset = ip1;
                        token = Token.NULL;
                        break;
                    }
                    break;

                case Token.COMMENT1: // Inside a comment
                    if (SyntaxUtilities.regionMatches(false, line, i, "-->")) {
                        addToken((i + 3) - lastOffset, token);
                        lastOffset = i + 3;
                        token = Token.NULL;
                    }
                    break;

                case Token.COMMENT2: // Inside a declaration
                    if (SyntaxUtilities.regionMatches(false, line, i, ">")) {
                        addToken(ip1 - lastOffset, token);
                        lastOffset = ip1;
                        token = Token.NULL;
                    }
                    break;

                case Token.KEYWORD3: // Inside a processor instruction
                    if (SyntaxUtilities.regionMatches(false, line, i, "?>")) {
                        addToken((i + 2) - lastOffset, token);
                        lastOffset = i + 2;
                        token = Token.NULL;
                    }
                    break;

                default:
                    throw new InternalError("Invalid state: " + token);
            }
        }

        switch (token) {
            case Token.LABEL:
                addToken(length - lastOffset, Token.INVALID);
                token = Token.NULL;
                break;

            default:
                addToken(length - lastOffset, token);
                break;
        }

        return token;
    }
}
