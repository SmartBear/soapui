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
 * TeX token marker.
 *
 * @author Slava Pestov
 * @version $Id: TeXTokenMarker.java,v 1.16 1999/12/13 03:40:30 sp Exp $
 */
public class TeXTokenMarker extends TokenMarker {
    // public members
    public static final byte BDFORMULA = Token.INTERNAL_FIRST;
    public static final byte EDFORMULA = (byte) (Token.INTERNAL_FIRST + 1);

    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        char[] array = line.array;
        int offset = line.offset;
        int lastOffset = offset;
        int length = line.count + offset;
        boolean backslash = false;
        loop:
        for (int i = offset; i < length; i++) {
            int i1 = (i + 1);

            char c = array[i];
            // if a backslash is followed immediately
            // by a non-alpha character, the command at
            // the non-alpha char. If we have a backslash,
            // some text, and then a non-alpha char,
            // the command ends before the non-alpha char.
            if (Character.isLetter(c)) {
                backslash = false;
            } else {
                if (backslash) {
                    // \<non alpha>
                    // we skip over this character,
                    // hence the `continue'
                    backslash = false;
                    if (token == Token.KEYWORD2 || token == EDFORMULA) {
                        token = Token.KEYWORD2;
                    }
                    addToken(i1 - lastOffset, token);
                    lastOffset = i1;
                    if (token == Token.KEYWORD1) {
                        token = Token.NULL;
                    }
                    continue;
                } else {
                    // \blah<non alpha>
                    // we leave the character in
                    // the stream, and it's not
                    // part of the command token
                    if (token == BDFORMULA || token == EDFORMULA) {
                        token = Token.KEYWORD2;
                    }
                    addToken(i - lastOffset, token);
                    if (token == Token.KEYWORD1) {
                        token = Token.NULL;
                    }
                    lastOffset = i;
                }
            }
            switch (c) {
                case '%':
                    if (backslash) {
                        backslash = false;
                        break;
                    }
                    addToken(i - lastOffset, token);
                    addToken(length - i, Token.COMMENT1);
                    lastOffset = length;
                    break loop;
                case '\\':
                    backslash = true;
                    if (token == Token.NULL) {
                        token = Token.KEYWORD1;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                    }
                    break;
                case '$':
                    backslash = false;
                    if (token == Token.NULL) // singe $
                    {
                        token = Token.KEYWORD2;
                        addToken(i - lastOffset, Token.NULL);
                        lastOffset = i;
                    } else if (token == Token.KEYWORD1) // \...$
                    {
                        token = Token.KEYWORD2;
                        addToken(i - lastOffset, Token.KEYWORD1);
                        lastOffset = i;
                    } else if (token == Token.KEYWORD2) // $$aaa
                    {
                        if (i - lastOffset == 1 && array[i - 1] == '$') {
                            token = BDFORMULA;
                            break;
                        }
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.KEYWORD2);
                        lastOffset = i1;
                    } else if (token == BDFORMULA) // $$aaa$
                    {
                        token = EDFORMULA;
                    } else if (token == EDFORMULA) // $$aaa$$
                    {
                        token = Token.NULL;
                        addToken(i1 - lastOffset, Token.KEYWORD2);
                        lastOffset = i1;
                    }
                    break;
            }
        }
        if (lastOffset != length) {
            addToken(length - lastOffset, token == BDFORMULA || token == EDFORMULA ? Token.KEYWORD2 : token);
        }
        return (token != Token.KEYWORD1 ? token : Token.NULL);
    }
}
