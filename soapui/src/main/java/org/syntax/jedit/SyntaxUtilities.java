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

package org.syntax.jedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

import org.syntax.jedit.tokenmarker.Token;

/**
 * Class with several utility functions used by jEdit's syntax colorizing
 * subsystem.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxUtilities {
    /**
     * Checks if a subregion of a <code>Segment</code> is equal to a string.
     *
     * @param ignoreCase True if case should be ignored, false otherwise
     * @param text       The segment
     * @param offset     The offset into the segment
     * @param match      The string to match
     */
    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, String match) {
        int length = offset + match.length();
        char[] textArray = text.array;
        if (length > text.offset + text.count) {
            return false;
        }
        for (int i = offset, j = 0; i < length; i++, j++) {
            char c1 = textArray[i];
            char c2 = match.charAt(j);
            if (ignoreCase) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a subregion of a <code>Segment</code> is equal to a character
     * array.
     *
     * @param ignoreCase True if case should be ignored, false otherwise
     * @param text       The segment
     * @param offset     The offset into the segment
     * @param match      The character array to match
     */
    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, char[] match) {
        int length = offset + match.length;
        char[] textArray = text.array;
        if (length > text.offset + text.count) {
            return false;
        }
        for (int i = offset, j = 0; i < length; i++, j++) {
            char c1 = textArray[i];
            char c2 = match[j];
            if (ignoreCase) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the default style table. This can be passed to the
     * <code>setStyles()</code> method of <code>SyntaxDocument</code> to use the
     * default syntax styles.
     */
    public static SyntaxStyle[] getDefaultSyntaxStyles() {
        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

        styles[Token.COMMENT1] = new SyntaxStyle(Color.black, true, false);
        styles[Token.COMMENT2] = new SyntaxStyle(new Color(0x990033), true, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(Color.black, false, true);
        styles[Token.KEYWORD2] = new SyntaxStyle(Color.magenta, false, false);
        styles[Token.KEYWORD3] = new SyntaxStyle(new Color(0x009600), false, false);
        styles[Token.LITERAL1] = new SyntaxStyle(new Color(0x650099), false, false);
        styles[Token.LITERAL2] = new SyntaxStyle(new Color(0x650099), false, true);
        styles[Token.LABEL] = new SyntaxStyle(new Color(0x990033), false, true);
        styles[Token.OPERATOR] = new SyntaxStyle(Color.black, false, true);
        styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);

        return styles;
    }

    /**
     * Paints the specified line onto the graphics context. Note that this method
     * munges the offset and count values of the segment.
     *
     * @param line     The line segment
     * @param tokens   The token list for the line
     * @param styles   The syntax style list
     * @param expander The tab expander used to determine tab stops. May be null
     * @param gfx      The graphics context
     * @param x        The x co-ordinate
     * @param y        The y co-ordinate
     * @return The x co-ordinate, plus the width of the painted string
     */
    public static int paintSyntaxLine(Segment line, Token tokens, SyntaxStyle[] styles, TabExpander expander,
                                      Graphics gfx, int x, int y) {
        Font defaultFont = gfx.getFont();
        Color defaultColor = gfx.getColor();

        int offset = 0;
        for (; ; ) {
            byte id = tokens.id;
            if (id == Token.END) {
                break;
            }

            int length = tokens.length;
            if (id == Token.NULL) {
                if (!defaultColor.equals(gfx.getColor())) {
                    gfx.setColor(defaultColor);
                }
                if (!defaultFont.equals(gfx.getFont())) {
                    gfx.setFont(defaultFont);
                }
            } else {
                styles[id].setGraphicsFlags(gfx, defaultFont);
            }

            line.count = length;
            x = Utilities.drawTabbedText(line, x, y, gfx, expander, 0);
            line.offset += length;
            offset += length;

            tokens = tokens.next;
        }

        return x;
    }

    // private members
    private SyntaxUtilities() {
    }
}
