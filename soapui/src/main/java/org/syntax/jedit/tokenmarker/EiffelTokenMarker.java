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

import javax.swing.text.Segment;

import org.syntax.jedit.KeywordMap;

/**
 * Eiffel token Marker.
 *
 * @author Artur Biesiadowski
 */
public class EiffelTokenMarker extends TokenMarker {

    public EiffelTokenMarker() {
        this.keywords = getKeywords();
    }

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        lastOffset = offset;
        lastKeyword = offset;
        int length = line.count + offset;
        boolean backslash = false;

        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            char c = array[i];
            if (c == '%') {
                backslash = !backslash;
                continue;
            }

            switch (token) {
                case Token.NULL:
                    switch (c) {
                        case '"':
                            doKeyword(line, i, c);
                            if (backslash) {
                                backslash = false;
                            } else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL1;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case '\'':
                            doKeyword(line, i, c);
                            if (backslash) {
                                backslash = false;
                            } else {
                                addToken(i - lastOffset, token);
                                token = Token.LITERAL2;
                                lastOffset = lastKeyword = i;
                            }
                            break;
                        case ':':
                            if (lastKeyword == offset) {
                                if (doKeyword(line, i, c)) {
                                    break;
                                }
                                backslash = false;
                                addToken(i1 - lastOffset, Token.LABEL);
                                lastOffset = lastKeyword = i1;
                            } else if (doKeyword(line, i, c)) {
                                break;
                            }
                            break;
                        case '-':
                            backslash = false;
                            doKeyword(line, i, c);
                            if (length - i > 1) {
                                switch (array[i1]) {
                                    case '-':
                                        addToken(i - lastOffset, token);
                                        addToken(length - i, Token.COMMENT1);
                                        lastOffset = lastKeyword = length;
                                        break loop;
                                }
                            }
                            break;
                        default:
                            backslash = false;
                            if (!Character.isLetterOrDigit(c) && c != '_') {
                                doKeyword(line, i, c);
                            }
                            break;
                    }
                    break;
                case Token.COMMENT1:
                case Token.COMMENT2:
                    throw new RuntimeException("Wrong eiffel parser state");
                case Token.LITERAL1:
                    if (backslash) {
                        backslash = false;
                    } else if (c == '"') {
                        addToken(i1 - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                case Token.LITERAL2:
                    if (backslash) {
                        backslash = false;
                    } else if (c == '\'') {
                        addToken(i1 - lastOffset, Token.LITERAL1);
                        token = Token.NULL;
                        lastOffset = lastKeyword = i1;
                    }
                    break;
                default:
                    throw new InternalError("Invalid state: " + token);
            }
        }

        if (token == Token.NULL) {
            doKeyword(line, length, '\0');
        }

        switch (token) {
            case Token.LITERAL1:
            case Token.LITERAL2:
                addToken(length - lastOffset, Token.INVALID);
                token = Token.NULL;
                break;
            case Token.KEYWORD2:
                addToken(length - lastOffset, token);
                if (!backslash) {
                    token = Token.NULL;
                }
            default:
                addToken(length - lastOffset, token);
                break;
        }

        return token;
    }

    public static KeywordMap getKeywords() {
        if (eiffelKeywords == null) {
            eiffelKeywords = new KeywordMap(true);
            eiffelKeywords.add("alias", Token.KEYWORD1);
            eiffelKeywords.add("all", Token.KEYWORD1);
            eiffelKeywords.add("and", Token.KEYWORD1);
            eiffelKeywords.add("as", Token.KEYWORD1);
            eiffelKeywords.add("check", Token.KEYWORD1);
            eiffelKeywords.add("class", Token.KEYWORD1);
            eiffelKeywords.add("creation", Token.KEYWORD1);
            eiffelKeywords.add("debug", Token.KEYWORD1);
            eiffelKeywords.add("deferred", Token.KEYWORD1);
            eiffelKeywords.add("do", Token.KEYWORD1);
            eiffelKeywords.add("else", Token.KEYWORD1);
            eiffelKeywords.add("elseif", Token.KEYWORD1);
            eiffelKeywords.add("end", Token.KEYWORD1);
            eiffelKeywords.add("ensure", Token.KEYWORD1);
            eiffelKeywords.add("expanded", Token.KEYWORD1);
            eiffelKeywords.add("export", Token.KEYWORD1);
            eiffelKeywords.add("external", Token.KEYWORD1);
            eiffelKeywords.add("feature", Token.KEYWORD1);
            eiffelKeywords.add("from", Token.KEYWORD1);
            eiffelKeywords.add("frozen", Token.KEYWORD1);
            eiffelKeywords.add("if", Token.KEYWORD1);
            eiffelKeywords.add("implies", Token.KEYWORD1);
            eiffelKeywords.add("indexing", Token.KEYWORD1);
            eiffelKeywords.add("infix", Token.KEYWORD1);
            eiffelKeywords.add("inherit", Token.KEYWORD1);
            eiffelKeywords.add("inspect", Token.KEYWORD1);
            eiffelKeywords.add("invariant", Token.KEYWORD1);
            eiffelKeywords.add("is", Token.KEYWORD1);
            eiffelKeywords.add("like", Token.KEYWORD1);
            eiffelKeywords.add("local", Token.KEYWORD1);
            eiffelKeywords.add("loop", Token.KEYWORD1);
            eiffelKeywords.add("not", Token.KEYWORD1);
            eiffelKeywords.add("obsolete", Token.KEYWORD1);
            eiffelKeywords.add("old", Token.KEYWORD1);
            eiffelKeywords.add("once", Token.KEYWORD1);
            eiffelKeywords.add("or", Token.KEYWORD1);
            eiffelKeywords.add("prefix", Token.KEYWORD1);
            eiffelKeywords.add("redefine", Token.KEYWORD1);
            eiffelKeywords.add("rename", Token.KEYWORD1);
            eiffelKeywords.add("require", Token.KEYWORD1);
            eiffelKeywords.add("rescue", Token.KEYWORD1);
            eiffelKeywords.add("retry", Token.KEYWORD1);
            eiffelKeywords.add("select", Token.KEYWORD1);
            eiffelKeywords.add("separate", Token.KEYWORD1);
            eiffelKeywords.add("then", Token.KEYWORD1);
            eiffelKeywords.add("undefine", Token.KEYWORD1);
            eiffelKeywords.add("until", Token.KEYWORD1);
            eiffelKeywords.add("variant", Token.KEYWORD1);
            eiffelKeywords.add("when", Token.KEYWORD1);
            eiffelKeywords.add("xor", Token.KEYWORD1);

            eiffelKeywords.add("current", Token.LITERAL2);
            eiffelKeywords.add("false", Token.LITERAL2);
            eiffelKeywords.add("precursor", Token.LITERAL2);
            eiffelKeywords.add("result", Token.LITERAL2);
            eiffelKeywords.add("strip", Token.LITERAL2);
            eiffelKeywords.add("true", Token.LITERAL2);
            eiffelKeywords.add("unique", Token.LITERAL2);
            eiffelKeywords.add("void", Token.LITERAL2);

        }
        return eiffelKeywords;
    }

    // private members
    private static KeywordMap eiffelKeywords;

    private boolean cpp;
    private KeywordMap keywords;
    private int lastOffset;
    private int lastKeyword;

    private boolean doKeyword(Segment line, int i, char c) {
        int i1 = i + 1;
        boolean klassname = false;

        int len = i - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id == Token.NULL) {
            klassname = true;
            for (int at = lastKeyword; at < lastKeyword + len; at++) {
                char ch = line.array[at];
                if (ch != '_' && !Character.isUpperCase(ch)) {
                    klassname = false;
                    break;
                }
            }
            if (klassname) {
                id = Token.KEYWORD3;
            }
        }

        if (id != Token.NULL) {
            if (lastKeyword != lastOffset) {
                addToken(lastKeyword - lastOffset, Token.NULL);
            }
            addToken(len, id);
            lastOffset = i;
        }
        lastKeyword = i1;
        return false;
    }
}
