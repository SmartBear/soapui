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
 * Patch/diff token marker.
 *
 * @author Slava Pestov
 * @version $Id: PatchTokenMarker.java,v 1.7 1999/12/13 03:40:30 sp Exp $
 */
public class PatchTokenMarker extends TokenMarker {
    public byte markTokensImpl(byte token, Segment line, int lineIndex) {
        if (line.count == 0) {
            return Token.NULL;
        }
        switch (line.array[line.offset]) {
            case '+':
            case '>':
                addToken(line.count, Token.KEYWORD1);
                break;
            case '-':
            case '<':
                addToken(line.count, Token.KEYWORD2);
                break;
            case '@':
            case '*':
                addToken(line.count, Token.KEYWORD3);
                break;
            default:
                addToken(line.count, Token.NULL);
                break;
        }
        return Token.NULL;
    }

    public boolean supportsMultilineTokens() {
        return false;
    }
}
