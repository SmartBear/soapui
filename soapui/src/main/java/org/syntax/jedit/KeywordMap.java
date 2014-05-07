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

import javax.swing.text.Segment;

import org.syntax.jedit.tokenmarker.Token;

/**
 * A <code>KeywordMap</code> is similar to a hashtable in that it maps keys to
 * values. However, the `keys' are Swing segments. This allows lookups of text
 * substrings without the overhead of creating a new string object.
 * <p/>
 * This class is used by <code>CTokenMarker</code> to map keywords to ids.
 *
 * @author Slava Pestov, Mike Dillon
 * @version $Id$
 */
public class KeywordMap {
    /**
     * Creates a new <code>KeywordMap</code>.
     *
     * @param ignoreCase True if keys are case insensitive
     */
    public KeywordMap(boolean ignoreCase) {
        this(ignoreCase, 52);
        this.ignoreCase = ignoreCase;
    }

    /**
     * Creates a new <code>KeywordMap</code>.
     *
     * @param ignoreCase True if the keys are case insensitive
     * @param mapLength  The number of `buckets' to create. A value of 52 will give good
     *                   performance for most maps.
     */
    public KeywordMap(boolean ignoreCase, int mapLength) {
        this.mapLength = mapLength;
        this.ignoreCase = ignoreCase;
        map = new Keyword[mapLength];
    }

    /**
     * Looks up a key.
     *
     * @param text   The text segment
     * @param offset The offset of the substring within the text segment
     * @param length The length of the substring
     */
    public byte lookup(Segment text, int offset, int length) {
        if (length == 0) {
            return Token.NULL;
        }
        Keyword k = map[getSegmentMapKey(text, offset, length)];
        while (k != null) {
            if (length != k.keyword.length) {
                k = k.next;
                continue;
            }
            if (SyntaxUtilities.regionMatches(ignoreCase, text, offset, k.keyword)) {
                return k.id;
            }
            k = k.next;
        }
        return Token.NULL;
    }

    /**
     * Adds a key-value mapping.
     *
     * @param keyword The key
     * @Param id The value
     */
    public void add(String keyword, byte id) {
        int key = getStringMapKey(keyword);
        map[key] = new Keyword(keyword.toCharArray(), id, map[key]);
    }

    /**
     * Returns true if the keyword map is set to be case insensitive, false
     * otherwise.
     */
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Sets if the keyword map should be case insensitive.
     *
     * @param ignoreCase True if the keyword map should be case insensitive, false
     *                   otherwise
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    // protected members
    protected int mapLength;

    protected int getStringMapKey(String s) {
        return (Character.toUpperCase(s.charAt(0)) + Character.toUpperCase(s.charAt(s.length() - 1)))
                % mapLength;
    }

    protected int getSegmentMapKey(Segment s, int off, int len) {
        return (Character.toUpperCase(s.array[off]) + Character.toUpperCase(s.array[off + len - 1])) % mapLength;
    }

    // private members
    class Keyword {
        public Keyword(char[] keyword, byte id, Keyword next) {
            this.keyword = keyword;
            this.id = id;
            this.next = next;
        }

        public char[] keyword;
        public byte id;
        public Keyword next;
    }

    private Keyword[] map;
    private boolean ignoreCase;
}
