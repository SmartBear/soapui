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

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

import org.syntax.jedit.tokenmarker.TokenMarker;

import com.eviware.soapui.SoapUI;

/**
 * A document implementation that can be tokenized by the syntax highlighting
 * system.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxDocument extends PlainDocument {
    /**
     * Returns the token marker that is to be used to split lines of this
     * document up into tokens. May return null if this document is not to be
     * colorized.
     */
    public TokenMarker getTokenMarker() {
        return tokenMarker;
    }

    /**
     * Sets the token marker that is to be used to split lines of this document
     * up into tokens. May throw an exception if this is not supported for this
     * type of document.
     *
     * @param tm The new token marker
     */
    public void setTokenMarker(TokenMarker tm) {
        tokenMarker = tm;
        if (tm == null) {
            return;
        }
        tokenMarker.insertLines(0, getDefaultRootElement().getElementCount());
        tokenizeLines();
    }

    /**
     * Reparses the document, by passing all lines to the token marker. This
     * should be called after the document is first loaded.
     */
    public void tokenizeLines() {
        tokenizeLines(0, getDefaultRootElement().getElementCount());
    }

    /**
     * Reparses the document, by passing the specified lines to the token marker.
     * This should be called after a large quantity of text is first inserted.
     *
     * @param start The first line to parse
     * @param len   The number of lines, after the first one to parse
     */
    public void tokenizeLines(int start, int len) {
        if (tokenMarker == null || !tokenMarker.supportsMultilineTokens()) {
            return;
        }

        Segment lineSegment = new Segment();
        Element map = getDefaultRootElement();

        len += start;

        try {
            for (int i = start; i < len; i++) {
                Element lineElement = map.getElement(i);
                int lineStart = lineElement.getStartOffset();
                getText(lineStart, lineElement.getEndOffset() - lineStart - 1, lineSegment);
                tokenMarker.markTokens(lineSegment, i);
            }
        } catch (BadLocationException bl) {
            SoapUI.logError(bl);
        }
    }

    /**
     * Starts a compound edit that can be undone in one operation. Subclasses
     * that implement undo should override this method; this class has no undo
     * functionality so this method is empty.
     */
    public void beginCompoundEdit() {
    }

    /**
     * Ends a compound edit that can be undone in one operation. Subclasses that
     * implement undo should override this method; this class has no undo
     * functionality so this method is empty.
     */
    public void endCompoundEdit() {
    }

    /**
     * Adds an undoable edit to this document's undo list. The edit should be
     * ignored if something is currently being undone.
     *
     * @param edit The undoable edit
     * @since jEdit 2.2pre1
     */
    public void addUndoableEdit(UndoableEdit edit) {
    }

    // protected members
    protected TokenMarker tokenMarker;

    /**
     * We overwrite this method to update the token marker state immediately so
     * that any event listeners get a consistent token marker.
     */
    protected void fireInsertUpdate(DocumentEvent evt) {
        if (tokenMarker != null) {
            DocumentEvent.ElementChange ch = evt.getChange(getDefaultRootElement());
            if (ch != null) {
                tokenMarker.insertLines(ch.getIndex() + 1, ch.getChildrenAdded().length - ch.getChildrenRemoved().length);
            }
        }

        super.fireInsertUpdate(evt);
    }

    /**
     * We overwrite this method to update the token marker state immediately so
     * that any event listeners get a consistent token marker.
     */
    protected void fireRemoveUpdate(DocumentEvent evt) {
        if (tokenMarker != null) {
            DocumentEvent.ElementChange ch = evt.getChange(getDefaultRootElement());
            if (ch != null) {
                tokenMarker.deleteLines(ch.getIndex() + 1, ch.getChildrenRemoved().length - ch.getChildrenAdded().length);
            }
        }

        super.fireRemoveUpdate(evt);
    }
}
