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

/**
 * A linked list of tokens. Each token has three fields - a token identifier,
 * which is a byte value that can be looked up in the array returned by
 * <code>SyntaxDocument.getColors()</code> to get a color value, a length value
 * which is the length of the token in the text, and a pointer to the next token
 * in the list.
 *
 * @author Slava Pestov
 * @version $Id$
 */

public class Token {
    /**
     * Normal text token id. This should be used to mark normal text.
     */
    public static final byte NULL = 0;

    /**
     * Comment 1 token id. This can be used to mark a comment.
     */
    public static final byte COMMENT1 = 1;

    /**
     * Comment 2 token id. This can be used to mark a comment.
     */
    public static final byte COMMENT2 = 2;

    /**
     * Literal 1 token id. This can be used to mark a string literal (eg, C mode
     * uses this to mark "..." literals)
     */
    public static final byte LITERAL1 = 3;

    /**
     * Literal 2 token id. This can be used to mark an object literal (eg, Java
     * mode uses this to mark true, false, etc)
     */
    public static final byte LITERAL2 = 4;

    /**
     * Label token id. This can be used to mark labels (eg, C mode uses this to
     * mark ...: sequences)
     */
    public static final byte LABEL = 5;

    /**
     * Keyword 1 token id. This can be used to mark a keyword. This should be
     * used for general language constructs.
     */
    public static final byte KEYWORD1 = 6;

    /**
     * Keyword 2 token id. This can be used to mark a keyword. This should be
     * used for preprocessor commands, or variables.
     */
    public static final byte KEYWORD2 = 7;

    /**
     * Keyword 3 token id. This can be used to mark a keyword. This should be
     * used for data types.
     */
    public static final byte KEYWORD3 = 8;

    /**
     * Operator token id. This can be used to mark an operator. (eg, SQL mode
     * marks +, -, etc with this token type)
     */
    public static final byte OPERATOR = 9;

    /**
     * Invalid token id. This can be used to mark invalid or incomplete tokens,
     * so the user can easily spot syntax errors.
     */
    public static final byte INVALID = 10;

    /**
     * The total number of defined token ids.
     */
    public static final byte ID_COUNT = 11;

    /**
     * The first id that can be used for internal state in a token marker.
     */
    public static final byte INTERNAL_FIRST = 100;

    /**
     * The last id that can be used for internal state in a token marker.
     */
    public static final byte INTERNAL_LAST = 126;

    /**
     * The token type, that along with a length of 0 marks the end of the token
     * list.
     */
    public static final byte END = 127;

    /**
     * The length of this token.
     */
    public int length;

    /**
     * The id of this token.
     */
    public byte id;

    /**
     * The next token in the linked list.
     */
    public Token next;

    /**
     * Creates a new token.
     *
     * @param length The length of the token
     * @param id     The id of the token
     */
    public Token(int length, byte id) {
        this.length = length;
        this.id = id;
    }

    /**
     * Returns a string representation of this token.
     */
    public String toString() {
        return "[id=" + id + ",length=" + length + "]";
    }
}
