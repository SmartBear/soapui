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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

import org.syntax.jedit.tokenmarker.Token;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.eviware.soapui.SoapUI;

/**
 * The text area repaint manager. It performs double buffering and paints lines
 * of text.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class TextAreaPainter extends JComponent implements TabExpander {
    private int areaWidth;
    private int areaHeight;

    /**
     * Creates a new repaint manager. This should be not be called directly.
     */
    public TextAreaPainter(JEditTextArea textArea, TextAreaDefaults defaults) {
        this.textArea = textArea;

        setAutoscrolls(true);
        setDoubleBuffered(true);
        setOpaque(true);
        setBorder(null);

        ToolTipManager.sharedInstance().registerComponent(this);

        currentLine = new Segment();
        currentLineIndex = -1;

        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        setFont(new Font("Monospaced", Font.PLAIN, 14));
        setForeground(Color.black);
        setBackground(Color.white);

        blockCaret = defaults.blockCaret;
        styles = defaults.styles;
        // cols = defaults.cols;
        // rows = defaults.rows;
        caretColor = defaults.caretColor;
        selectionColor = defaults.selectionColor;
        lineHighlightColor = defaults.lineHighlightColor;
        lineHighlight = defaults.lineHighlight;
        bracketHighlightColor = defaults.bracketHighlightColor;
        bracketHighlight = defaults.bracketHighlight;
        paintInvalid = defaults.paintInvalid;
        eolMarkerColor = defaults.eolMarkerColor;
        eolMarkers = defaults.eolMarkers;
    }

    /**
     * Returns if this component can be traversed by pressing the Tab key. This
     * returns false.
     */
    public final boolean isManagingFocus() {
        return false;
    }

    /**
     * Returns the syntax styles used to paint colorized text. Entry <i>n</i>
     * will be used to paint tokens with id = <i>n</i>.
     *
     * @see org.syntax.jedit.Token
     */
    public final SyntaxStyle[] getStyles() {
        return styles;
    }

    /**
     * Sets the syntax styles used to paint colorized text. Entry <i>n</i> will
     * be used to paint tokens with id = <i>n</i>.
     *
     * @param styles The syntax styles
     * @see org.syntax.jedit.Token
     */
    public final void setStyles(SyntaxStyle[] styles) {
        this.styles = styles;
        repaint();
    }

    /**
     * Returns the caret color.
     */
    public final Color getCaretColor() {
        return caretColor;
    }

    /**
     * Sets the caret color.
     *
     * @param caretColor The caret color
     */
    public final void setCaretColor(Color caretColor) {
        this.caretColor = caretColor;
        invalidateSelectedLines();
    }

    /**
     * Returns the selection color.
     */
    public final Color getSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the selection color.
     *
     * @param selectionColor The selection color
     */
    public final void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
        invalidateSelectedLines();
    }

    /**
     * Returns the line highlight color.
     */
    public final Color getLineHighlightColor() {
        return lineHighlightColor;
    }

    /**
     * Sets the line highlight color.
     *
     * @param lineHighlightColor The line highlight color
     */
    public final void setLineHighlightColor(Color lineHighlightColor) {
        this.lineHighlightColor = lineHighlightColor;
        invalidateSelectedLines();
    }

    /**
     * Returns true if line highlight is enabled, false otherwise.
     */
    public final boolean isLineHighlightEnabled() {
        return lineHighlight;
    }

    /**
     * Enables or disables current line highlighting.
     *
     * @param lineHighlight True if current line highlight should be enabled, false
     *                      otherwise
     */
    public final void setLineHighlightEnabled(boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
        invalidateSelectedLines();
    }

    /**
     * Returns the bracket highlight color.
     */
    public final Color getBracketHighlightColor() {
        return bracketHighlightColor;
    }

    /**
     * Sets the bracket highlight color.
     *
     * @param bracketHighlightColor The bracket highlight color
     */
    public final void setBracketHighlightColor(Color bracketHighlightColor) {
        this.bracketHighlightColor = bracketHighlightColor;
        invalidateLine(textArea.getBracketLine());
    }

    /**
     * Returns true if bracket highlighting is enabled, false otherwise. When
     * bracket highlighting is enabled, the bracket matching the one before the
     * caret (if any) is highlighted.
     */
    public final boolean isBracketHighlightEnabled() {
        return bracketHighlight;
    }

    /**
     * Enables or disables bracket highlighting. When bracket highlighting is
     * enabled, the bracket matching the one before the caret (if any) is
     * highlighted.
     *
     * @param bracketHighlight True if bracket highlighting should be enabled, false otherwise
     */
    public final void setBracketHighlightEnabled(boolean bracketHighlight) {
        this.bracketHighlight = bracketHighlight;
        invalidateLine(textArea.getBracketLine());
    }

    /**
     * Returns true if the caret should be drawn as a block, false otherwise.
     */
    public final boolean isBlockCaretEnabled() {
        return blockCaret;
    }

    /**
     * Sets if the caret should be drawn as a block, false otherwise.
     *
     * @param blockCaret True if the caret should be drawn as a block, false otherwise.
     */
    public final void setBlockCaretEnabled(boolean blockCaret) {
        this.blockCaret = blockCaret;
        invalidateSelectedLines();
    }

    /**
     * Returns the EOL marker color.
     */
    public final Color getEOLMarkerColor() {
        return eolMarkerColor;
    }

    /**
     * Sets the EOL marker color.
     *
     * @param eolMarkerColor The EOL marker color
     */
    public final void setEOLMarkerColor(Color eolMarkerColor) {
        this.eolMarkerColor = eolMarkerColor;
        repaint();
    }

    /**
     * Returns true if EOL markers are drawn, false otherwise.
     */
    public final boolean getEOLMarkersPainted() {
        return eolMarkers;
    }

    /**
     * Sets if EOL markers are to be drawn.
     *
     * @param eolMarkers True if EOL markers should be drawn, false otherwise
     */
    public final void setEOLMarkersPainted(boolean eolMarkers) {
        this.eolMarkers = eolMarkers;
        repaint();
    }

    /**
     * Returns true if invalid lines are painted as red tildes (~), false
     * otherwise.
     */
    public boolean getInvalidLinesPainted() {
        return paintInvalid;
    }

    /**
     * Sets if invalid lines are to be painted as red tildes.
     *
     * @param paintInvalid True if invalid lines should be drawn, false otherwise
     */
    public void setInvalidLinesPainted(boolean paintInvalid) {
        this.paintInvalid = paintInvalid;
    }

    /**
     * Adds a custom highlight painter.
     *
     * @param highlight The highlight
     */
    public void addCustomHighlight(Highlight highlight) {
        highlight.init(textArea, highlights);
        highlights = highlight;
    }

    /**
     * Highlight interface.
     */
    public interface Highlight {
        /**
         * Called after the highlight painter has been added.
         *
         * @param textArea The text area
         * @param next     The painter this one should delegate to
         */
        void init(JEditTextArea textArea, Highlight next);

        /**
         * This should paint the highlight and delgate to the next highlight
         * painter.
         *
         * @param gfx  The graphics context
         * @param line The line number
         * @param y    The y co-ordinate of the line
         */
        void paintHighlight(Graphics gfx, int line, int y);

        /**
         * Returns the tool tip to display at the specified location. If this
         * highlighter doesn't know what to display, it should delegate to the
         * next highlight painter.
         *
         * @param evt The mouse event
         */
        String getToolTipText(MouseEvent evt);
    }

    /**
     * Returns the tool tip to display at the specified location.
     *
     * @param evt The mouse event
     */
    public String getToolTipText(MouseEvent evt) {
        if (highlights != null) {
            return highlights.getToolTipText(evt);
        } else {
            return null;
        }
    }

    /**
     * Returns the font metrics used by this component.
     */
    public FontMetrics getFontMetrics() {
        return fm;
    }

    /**
     * Sets the font for this component. This is overridden to update the cached
     * font metrics and to recalculate which lines are visible.
     *
     * @param font The font
     */
    public void setFont(Font font) {
        super.setFont(font);
        fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        textArea.recalculateVisibleLines();
    }

    /**
     * Repaints the text.
     *
     * @param g The graphics context
     */
    public void paint(Graphics gfx) {
        Rectangle clipRect = gfx.getClipBounds();

        gfx.setColor(getBackground());
        gfx.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

        // We don't use yToLine() here because that method doesn't
        // return lines past the end of the document
        int height = fm.getHeight();
        int firstLine = textArea.getFirstLine();
        int firstInvalid = firstLine + clipRect.y / height;
        // Because the clipRect's height is usually an even multiple
        // of the font height, we subtract 1 from it, otherwise one
        // too many lines will always be painted.
        int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;

        try {
            TokenMarker tokenMarker = textArea.getDocument().getTokenMarker();
            int x = 0; // -textArea.getHorizontalOffset();

            for (int line = firstInvalid; line <= lastInvalid; line++) {
                paintLine(gfx, tokenMarker, line, x);
            }

            if (tokenMarker != null && tokenMarker.isNextLineRequested()) {
                int h = clipRect.y + clipRect.height;
                repaint(0, h, getWidth(), getHeight() - h);
            }
        } catch (Exception e) {
            System.err.println("Error repainting line" + " range {" + firstInvalid + "," + lastInvalid + "}:");
            SoapUI.logError(e);
        }
    }

    /**
     * Marks a line as needing a repaint.
     *
     * @param line The line to invalidate
     */
    public final void invalidateLine(int line) {
        repaint(0, textArea.lineToY(line) + fm.getMaxDescent() + fm.getLeading(), getWidth(), fm.getHeight());
    }

    /**
     * Marks a range of lines as needing a repaint.
     *
     * @param firstLine The first line to invalidate
     * @param lastLine  The last line to invalidate
     */
    public final void invalidateLineRange(int firstLine, int lastLine) {
        repaint(0, textArea.lineToY(firstLine) + fm.getMaxDescent() + fm.getLeading(), getWidth(), (lastLine
                - firstLine + 1)
                * fm.getHeight());
    }

    /**
     * Repaints the lines containing the selection.
     */
    public final void invalidateSelectedLines() {
        invalidateLineRange(textArea.getSelectionStartLine(), textArea.getSelectionEndLine());
    }

    /**
     * Implementation of TabExpander interface. Returns next tab stop after a
     * specified point.
     *
     * @param x         The x co-ordinate
     * @param tabOffset Ignored
     * @return The next tab stop after <i>x</i>
     */
    public float nextTabStop(float x, int tabOffset) {
        if (tabSize == 0) {
            initTabSize();
        }

        int offset = 0; // -textArea.getHorizontalOffset();
        int ntabs = ((int) x - offset) / tabSize;
        return (ntabs + 1) * tabSize + offset;
    }

    private void initTabSize() {
        tabSize = fm.charWidth(' ')
                * ((Integer) textArea.getDocument().getProperty(PlainDocument.tabSizeAttribute)).intValue();
        if (tabSize == 0) {
            tabSize = fm.charWidth(' ') * 4;
        }
    }

    /**
     * Returns the painter's preferred size.
     */
    public Dimension getPreferredSize() {
        Dimension dim = new Dimension();
        dim.width = fm.charWidth('w') * textArea.getMaxLineLength() + 5;
        dim.height = fm.getHeight() * textArea.getLineCount() + 5;
        return dim;
    }

    /**
     * Returns the painter's minimum size.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // package-private members
    int currentLineIndex;
    Token currentLineTokens;
    Segment currentLine;

    // protected members
    protected JEditTextArea textArea;

    protected SyntaxStyle[] styles;
    protected Color caretColor;
    protected Color selectionColor;
    protected Color lineHighlightColor;
    protected Color bracketHighlightColor;
    protected Color eolMarkerColor;

    protected boolean blockCaret;
    protected boolean lineHighlight;
    protected boolean bracketHighlight;
    protected boolean paintInvalid;
    protected boolean eolMarkers;
    // protected int cols;
    // protected int rows;

    protected int tabSize;
    protected FontMetrics fm;

    protected Highlight highlights;

    protected void paintLine(Graphics gfx, TokenMarker tokenMarker, int line, int x) {
        Font defaultFont = getFont();
        Color defaultColor = getForeground();

        currentLineIndex = line;
        int y = textArea.lineToY(line);

        int lineWidth = 0;

        if (line < 0 || line >= textArea.getLineCount()) {
            if (paintInvalid) {
                paintHighlight(gfx, line, y);
                styles[Token.INVALID].setGraphicsFlags(gfx, defaultFont);
                gfx.drawString("~", 0, y + fm.getHeight());
            }
        } else if (tokenMarker == null) {
            lineWidth = paintPlainLine(gfx, line, defaultFont, defaultColor, x, y);
        } else {
            lineWidth = paintSyntaxLine(gfx, tokenMarker, line, defaultFont, defaultColor, x, y);
        }

        if (lineWidth > areaWidth) {
            areaWidth = lineWidth;
        }
    }

    protected int paintPlainLine(Graphics gfx, int line, Font defaultFont, Color defaultColor, int x, int y) {
        paintHighlight(gfx, line, y);
        textArea.getLineText(line, currentLine);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);

        y += fm.getHeight();
        x = Utilities.drawTabbedText(currentLine, x, y, gfx, this, 0);

        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }

        return x;
    }

    protected int paintSyntaxLine(Graphics gfx, TokenMarker tokenMarker, int line, Font defaultFont,
                                  Color defaultColor, int x, int y) {
        textArea.getLineText(currentLineIndex, currentLine);
        currentLineTokens = tokenMarker.markTokens(currentLine, currentLineIndex);

        paintHighlight(gfx, line, y);

        gfx.setFont(defaultFont);
        gfx.setColor(defaultColor);
        y += fm.getHeight();
        x = SyntaxUtilities.paintSyntaxLine(currentLine, currentLineTokens, styles, this, gfx, x, y);

        if (eolMarkers) {
            gfx.setColor(eolMarkerColor);
            gfx.drawString(".", x, y);
        }

        return x;
    }

    protected void paintHighlight(Graphics gfx, int line, int y) {
        if (line >= textArea.getSelectionStartLine() && line <= textArea.getSelectionEndLine()) {
            paintLineHighlight(gfx, line, y);
        }

        if (highlights != null) {
            highlights.paintHighlight(gfx, line, y);
        }

        if (bracketHighlight && line == textArea.getBracketLine()) {
            paintBracketHighlight(gfx, line, y);
        }

        if (line == textArea.getCaretLine()) {
            paintCaret(gfx, line, y);
        }
    }

    protected void paintLineHighlight(Graphics gfx, int line, int y) {
        int height = fm.getHeight();
        y += fm.getLeading() + fm.getMaxDescent();

        int selectionStart = textArea.getSelectionStart();
        int selectionEnd = textArea.getSelectionEnd();

        if (selectionStart == selectionEnd) {
            if (lineHighlight) {
                gfx.setColor(lineHighlightColor);
                gfx.fillRect(0, y, getWidth(), height);
            }
        } else {
            gfx.setColor(selectionColor);

            int selectionStartLine = textArea.getSelectionStartLine();
            int selectionEndLine = textArea.getSelectionEndLine();
            int lineStart = textArea.getLineStartOffset(line);

            int x1, x2;
            if (textArea.isSelectionRectangular()) {
                int lineLen = textArea.getTabExpandedLineLength(line);
                x1 = textArea._offsetToX(line,
                        Math.min(lineLen, selectionStart - textArea.getLineStartOffset(selectionStartLine)));
                x2 = textArea._offsetToX(line,
                        Math.min(lineLen, selectionEnd - textArea.getLineStartOffset(selectionEndLine)));
                if (x1 == x2) {
                    x2++;
                }
            } else if (selectionStartLine == selectionEndLine) {
                x1 = textArea._offsetToX(line, selectionStart - lineStart);
                x2 = textArea._offsetToX(line, selectionEnd - lineStart);
            } else if (line == selectionStartLine) {
                x1 = textArea._offsetToX(line, selectionStart - lineStart);
                x2 = getWidth();
            } else if (line == selectionEndLine) {
                x1 = 0;
                x2 = textArea._offsetToX(line, selectionEnd - lineStart);
            } else {
                x1 = 0;
                x2 = getWidth();
            }

            // "inlined" min/max()
            gfx.fillRect(x1 > x2 ? x2 : x1, y, x1 > x2 ? (x1 - x2) : (x2 - x1), height);
        }

    }

    protected void paintBracketHighlight(Graphics gfx, int line, int y) {
        int position = textArea.getBracketPosition();
        if (position == -1) {
            return;
        }
        y += fm.getLeading() + fm.getMaxDescent();
        int x = textArea._offsetToX(line, position);
        gfx.setColor(bracketHighlightColor);
        // Hack!!! Since there is no fast way to get the character
        // from the bracket matching routine, we use ( since all
        // brackets probably have the same width anyway
        gfx.drawRect(x, y, fm.charWidth('(') - 1, fm.getHeight() - 1);
    }

    protected void paintCaret(Graphics gfx, int line, int y) {
        if (textArea.isCaretVisible()) {
            int offset = textArea.getCaretPosition() - textArea.getLineStartOffset(line);
            int caretX = textArea._offsetToX(line, offset);
            int caretWidth = ((blockCaret || textArea.isOverwriteEnabled()) ? fm.charWidth('w') : 1);
            y += fm.getLeading() + fm.getMaxDescent();
            int height = fm.getHeight();

            gfx.setColor(caretColor);

            if (textArea.isOverwriteEnabled()) {
                gfx.fillRect(caretX, y + height - 1, caretWidth, 1);
            } else {
                gfx.drawRect(caretX, y, caretWidth, height - 1);
            }
        }
    }
}
