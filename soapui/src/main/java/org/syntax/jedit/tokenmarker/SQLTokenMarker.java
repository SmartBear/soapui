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
 * SQL token marker.
 *
 * @author mike dillon
 * @version $Id: SQLTokenMarker.java,v 1.6 1999/04/19 05:38:20 sp Exp $
 */
public class SQLTokenMarker extends TokenMarker {
    private int offset, lastOffset, lastKeyword, length;

    // public members
    public SQLTokenMarker(KeywordMap k) {
        this(k, false);
    }

    public SQLTokenMarker(KeywordMap k, boolean tsql) {
        keywords = k;
        isTSQL = tsql;
    }

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        offset = lastOffset = lastKeyword = line.offset;
        length = line.count + offset;

        loop:
        for (int i = offset; i < length; i++) {
            switch (line.array[i]) {
                case '*':
                    if (token == Token.COMMENT1 && length - i >= 1 && line.array[i + 1] == '/') {
                        token = Token.NULL;
                        i++;
                        addToken((i + 1) - lastOffset, Token.COMMENT1);
                        lastOffset = i + 1;
                    } else if (token == Token.NULL) {
                        searchBack(line, i);
                        addToken(1, Token.OPERATOR);
                        lastOffset = i + 1;
                    }
                    break;
                case '[':
                    if (token == Token.NULL) {
                        searchBack(line, i);
                        token = Token.LITERAL1;
                        literalChar = '[';
                        lastOffset = i;
                    }
                    break;
                case ']':
                    if (token == Token.LITERAL1 && literalChar == '[') {
                        token = Token.NULL;
                        literalChar = 0;
                        addToken((i + 1) - lastOffset, Token.LITERAL1);
                        lastOffset = i + 1;
                    }
                    break;
                case '.':
                case ',':
                case '(':
                case ')':
                    if (token == Token.NULL) {
                        searchBack(line, i);
                        addToken(1, Token.NULL);
                        lastOffset = i + 1;
                    }
                    break;
                case '+':
                case '%':
                case '&':
                case '|':
                case '^':
                case '~':
                case '<':
                case '>':
                case '=':
                    if (token == Token.NULL) {
                        searchBack(line, i);
                        addToken(1, Token.OPERATOR);
                        lastOffset = i + 1;
                    }
                    break;
                case ' ':
                case '\t':
                    if (token == Token.NULL) {
                        searchBack(line, i, false);
                    }
                    break;
                case ':':
                    if (token == Token.NULL) {
                        addToken((i + 1) - lastOffset, Token.LABEL);
                        lastOffset = i + 1;
                    }
                    break;
                case '/':
                    if (token == Token.NULL) {
                        if (length - i >= 2 && line.array[i + 1] == '*') {
                            searchBack(line, i);
                            token = Token.COMMENT1;
                            lastOffset = i;
                            i++;
                        } else {
                            searchBack(line, i);
                            addToken(1, Token.OPERATOR);
                            lastOffset = i + 1;
                        }
                    }
                    break;
                case '-':
                    if (token == Token.NULL) {
                        if (length - i >= 2 && line.array[i + 1] == '-') {
                            searchBack(line, i);
                            addToken(length - i, Token.COMMENT1);
                            lastOffset = length;
                            break loop;
                        } else {
                            searchBack(line, i);
                            addToken(1, Token.OPERATOR);
                            lastOffset = i + 1;
                        }
                    }
                    break;
                case '!':
                    if (isTSQL && token == Token.NULL && length - i >= 2
                            && (line.array[i + 1] == '=' || line.array[i + 1] == '<' || line.array[i + 1] == '>')) {
                        searchBack(line, i);
                        addToken(1, Token.OPERATOR);
                        lastOffset = i + 1;
                    }
                    break;
                case '"':
                case '\'':
                    if (token == Token.NULL) {
                        token = Token.LITERAL1;
                        literalChar = line.array[i];
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                    } else if (token == Token.LITERAL1 && literalChar == line.array[i]) {
                        token = Token.NULL;
                        literalChar = 0;
                        addToken((i + 1) - lastOffset, Token.LITERAL1);
                        lastOffset = i + 1;
                    }
                    break;
                default:
                    break;
            }
        }
        if (token == Token.NULL) {
            searchBack(line, length, false);
        }
        if (lastOffset != length) {
            addToken(length - lastOffset, token);
        }
        return token;
    }

    // protected members
    protected boolean isTSQL = false;

    // private members
    private KeywordMap keywords;
    private char literalChar = 0;

    private void searchBack(Segment line, int pos) {
        searchBack(line, pos, true);
    }

    private void searchBack(Segment line, int pos, boolean padNull) {
        int len = pos - lastKeyword;
        byte id = keywords.lookup(line, lastKeyword, len);
        if (id != Token.NULL) {
            if (lastKeyword != lastOffset) {
                addToken(lastKeyword - lastOffset, Token.NULL);
            }
            addToken(len, id);
            lastOffset = pos;
        }
        lastKeyword = pos + 1;
        if (padNull && lastOffset < pos) {
            addToken(pos - lastOffset, Token.NULL);
        }
    }
}
