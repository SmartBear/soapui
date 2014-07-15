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

/**
 * Java properties/DOS INI token marker.
 *
 * @author Slava Pestov
 * @version $Id: PropsTokenMarker.java,v 1.9 1999/12/13 03:40:30 sp Exp $
 */
public class PropsTokenMarker extends TokenMarker {
    public static final byte VALUE = Token.INTERNAL_FIRST;

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int length = line.count + offset;
        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            switch (token) {
                case Token.NULL:
                    switch (array[i]) {
                        case '#':
                        case ';':
                            if (i == offset) {
                                addToken(line.count, Token.COMMENT1);
                                lastOffset = length;
                                break loop;
                            }
                            break;
                        case '[':
                            if (i == offset) {
                                addToken(i - lastOffset, token);
                                token = Token.KEYWORD2;
                                lastOffset = i;
                            }
                            break;
                        case '=':
                            addToken(i - lastOffset, Token.KEYWORD1);
                            token = VALUE;
                            lastOffset = i;
                            break;
                    }
                    break;
                case Token.KEYWORD2:
                    if (array[i] == ']') {
                        addToken(i1 - lastOffset, token);
                        token = Token.NULL;
                        lastOffset = i1;
                    }
                    break;
                case VALUE:
                    break;
                default:
                    throw new InternalError("Invalid state: " + token);
            }
        }
        if (lastOffset != length) {
            addToken(length - lastOffset, Token.NULL);
        }
        return Token.NULL;
    }

    public boolean supportsMultilineTokens() {
        return false;
    }
}
