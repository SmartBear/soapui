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

import javax.swing.JPopupMenu;

/**
 * Encapsulates default settings for a text area. This can be passed to the
 * constructor once the necessary fields have been filled out. The advantage of
 * doing this over calling lots of set() methods after creating the text area is
 * that this method is faster.
 */
public class TextAreaDefaults {
    // private static TextAreaDefaults DEFAULTS;

    public InputHandler inputHandler;
    public SyntaxDocument document;
    public boolean editable;

    public boolean caretVisible;
    public boolean caretBlinks;
    public boolean blockCaret;
    public int electricScroll;

    public int cols;
    public int rows;
    public SyntaxStyle[] styles;
    public Color caretColor;
    public Color selectionColor;
    public Color lineHighlightColor;
    public boolean lineHighlight;
    public Color bracketHighlightColor;
    public boolean bracketHighlight;
    public Color eolMarkerColor;
    public boolean eolMarkers;
    public boolean paintInvalid;

    public JPopupMenu popup;

    /**
     * Returns a new TextAreaDefaults object with the default values filled in.
     */
    public static TextAreaDefaults getDefaults() {
        // if(DEFAULTS == null)
        // {
        TextAreaDefaults DEFAULTS = new TextAreaDefaults();

        DEFAULTS.inputHandler = new DefaultInputHandler();
        DEFAULTS.inputHandler.addDefaultKeyBindings();
        DEFAULTS.document = new SyntaxDocument();
        DEFAULTS.editable = true;

        DEFAULTS.blockCaret = false;
        DEFAULTS.caretVisible = true;
        DEFAULTS.caretBlinks = true;
        DEFAULTS.electricScroll = 3;

        DEFAULTS.cols = 80;
        DEFAULTS.rows = 25;
        DEFAULTS.styles = SyntaxUtilities.getDefaultSyntaxStyles();
        DEFAULTS.caretColor = Color.black; // Color.red;
        DEFAULTS.selectionColor = new Color(0xccccff);
        DEFAULTS.lineHighlightColor = new Color(0xe0e0e0);
        DEFAULTS.lineHighlight = true;
        DEFAULTS.bracketHighlightColor = Color.black;
        DEFAULTS.bracketHighlight = true;
        DEFAULTS.eolMarkerColor = new Color(0x009999);
        DEFAULTS.eolMarkers = false; // true;
        DEFAULTS.paintInvalid = false; // true;
        // }

        return DEFAULTS;
    }
}
