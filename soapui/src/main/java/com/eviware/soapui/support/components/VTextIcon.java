/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.support.components;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * VTextIcon is an Icon implementation which draws a short string vertically.
 * It's useful for JTabbedPanes with LEFT or RIGHT tabs but can be used in any
 * component which supports Icons, such as JLabel or JButton
 * <p/>
 * You can provide a hint to indicate whether to rotate the string to the left
 * or right, or not at all, and it checks to make sure that the rotation is
 * legal for the given string (for example, Chinese/Japanese/Korean scripts have
 * special rules when drawn vertically and should never be rotated)
 */
public class VTextIcon implements Icon, PropertyChangeListener {
    String fLabel;
    String[] fCharStrings; // for efficiency, break the fLabel into one-char
    // strings to be passed to drawString
    int[] fCharWidths; // Roman characters should be centered when not rotated
    // (Japanese fonts are monospaced)
    int[] fPosition; // Japanese half-height characters need to be shifted when
    // drawn vertically
    int fWidth, fHeight, fCharHeight, fDescent; // Cached for speed
    int fRotation;
    Component fComponent;

    static final int POSITION_NORMAL = 0;
    static final int POSITION_TOP_RIGHT = 1;
    static final int POSITION_FAR_TOP_RIGHT = 2;

    public static final int ROTATE_DEFAULT = 0x00;
    public static final int ROTATE_NONE = 0x01;
    public static final int ROTATE_LEFT = 0x02;
    public static final int ROTATE_RIGHT = 0x04;

    /**
     * Creates a <code>VTextIcon</code> for the specified <code>component</code>
     * with the specified <code>label</code>. It sets the orientation to the
     * default for the string
     *
     * @see #verifyRotation
     */
    public VTextIcon(Component component, String label) {
        this(component, label, ROTATE_DEFAULT);
    }

    /**
     * Creates a <code>VTextIcon</code> for the specified <code>component</code>
     * with the specified <code>label</code>. It sets the orientation to the
     * provided value if it's legal for the string
     *
     * @see #verifyRotation
     */
    public VTextIcon(Component component, String label, int rotateHint) {
        fComponent = component;
        fLabel = label;
        fRotation = verifyRotation(label, rotateHint);
        calcDimensions();
        fComponent.addPropertyChangeListener(this);
    }

    /**
     * sets the label to the given string, updating the orientation as needed and
     * invalidating the layout if the size changes
     *
     * @see #verifyRotation
     */
    public void setLabel(String label) {
        fLabel = label;
        fRotation = verifyRotation(label, fRotation); // Make sure the current
        // rotation is still legal
        recalcDimensions();
    }

    /**
     * Checks for changes to the font on the fComponent so that it can invalidate
     * the layout if the size changes
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if ("font".equals(prop)) {
            recalcDimensions();
        }
    }

    /**
     * Calculates the dimensions. If they've changed, invalidates the component
     */
    void recalcDimensions() {
        int wOld = getIconWidth();
        int hOld = getIconHeight();
        calcDimensions();
        if (wOld != getIconWidth() || hOld != getIconHeight()) {
            fComponent.invalidate();
        }
    }

    void calcDimensions() {
        FontMetrics fm = fComponent.getFontMetrics(fComponent.getFont());
        fCharHeight = fm.getAscent() + fm.getDescent();
        fDescent = fm.getDescent();
        if (fRotation == ROTATE_NONE) {
            int len = fLabel.length();
            char data[] = new char[len];
            fLabel.getChars(0, len, data, 0);
            // if not rotated, width is that of the widest char in the string
            fWidth = 0;
            // we need an array of one-char strings for drawString
            fCharStrings = new String[len];
            fCharWidths = new int[len];
            fPosition = new int[len];
            char ch;
            for (int i = 0; i < len; i++) {
                ch = data[i];
                fCharWidths[i] = fm.charWidth(ch);
                if (fCharWidths[i] > fWidth) {
                    fWidth = fCharWidths[i];
                }
                fCharStrings[i] = new String(data, i, 1);
                // small kana and punctuation
                if (sDrawsInTopRight.indexOf(ch) >= 0) // if ch is in
                // sDrawsInTopRight
                {
                    fPosition[i] = POSITION_TOP_RIGHT;
                } else if (sDrawsInFarTopRight.indexOf(ch) >= 0) {
                    fPosition[i] = POSITION_FAR_TOP_RIGHT;
                } else {
                    fPosition[i] = POSITION_NORMAL;
                }
            }
            // and height is the font height * the char count, + one extra leading
            // at the bottom
            fHeight = fCharHeight * len + fDescent;
        } else {
            // if rotated, width is the height of the string
            fWidth = fCharHeight;
            // and height is the width, plus some buffer space
            fHeight = fm.stringWidth(fLabel) + 2 * kBufferSpace;
        }
    }

    /**
     * Draw the icon at the specified location. Icon implementations may use the
     * Component argument to get properties useful for painting, e.g. the
     * foreground or background color.
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // We don't insist that it be on the same Component

        g.setColor(fComponent.getForeground());
        g.setFont(fComponent.getFont());
        if (fRotation == ROTATE_NONE) {
            int yPos = y + fCharHeight;
            for (int i = 0; i < fCharStrings.length; i++) {
                // Special rules for Japanese - "half-height" characters (like ya,
                // yu, yo in combinations)
                // should draw in the top-right quadrant when drawn vertically
                // - they draw in the bottom-left normally
                int tweak;
                switch (fPosition[i]) {
                    case POSITION_NORMAL:
                        // Roman fonts should be centered. Japanese fonts are always
                        // monospaced.
                        g.drawString(fCharStrings[i], x + ((fWidth - fCharWidths[i]) / 2), yPos);
                        break;
                    case POSITION_TOP_RIGHT:
                        tweak = fCharHeight / 3; // Should be 2, but they aren't actually
                        // half-height
                        g.drawString(fCharStrings[i], x + (tweak / 2), yPos - tweak);
                        break;
                    case POSITION_FAR_TOP_RIGHT:
                        tweak = fCharHeight - fCharHeight / 3;
                        g.drawString(fCharStrings[i], x + (tweak / 2), yPos - tweak);
                        break;
                }
                yPos += fCharHeight;
            }
        } else if (fRotation == ROTATE_LEFT) {
            g.translate(x + fWidth, y + fHeight);
            ((Graphics2D) g).rotate(-NINETY_DEGREES);
            // g.setColor( fComponent.getBackground() );
            // g.fillRect( 1, 1, fWidth - 2, fHeight - 2 );
            // g.setColor( fComponent.getForeground() );
            g.drawString(fLabel, kBufferSpace, -fDescent);
            g.setColor(fComponent.getBackground());
            ((Graphics2D) g).rotate(NINETY_DEGREES);
            g.translate(-(x + fWidth), -(y + fHeight));
        } else if (fRotation == ROTATE_RIGHT) {
            g.translate(x, y);
            ((Graphics2D) g).rotate(NINETY_DEGREES);
            g.setColor(fComponent.getBackground());
            g.fillRect(0, 0, fWidth, fHeight);
            g.setColor(fComponent.getForeground());
            g.drawString(fLabel, kBufferSpace, -fDescent);
            g.setColor(fComponent.getBackground());
            ((Graphics2D) g).rotate(-NINETY_DEGREES);
            g.translate(-x, -y);
        }

    }

    /**
     * Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    public int getIconWidth() {
        return fWidth;
    }

    /**
     * Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    public int getIconHeight() {
        return fHeight;
    }

    /**
     * verifyRotation
     * <p/>
     * returns the best rotation for the string (ROTATE_NONE, ROTATE_LEFT,
     * ROTATE_RIGHT)
     * <p/>
     * This is public static so you can use it to test a string without creating
     * a VTextIcon
     * <p/>
     * from http://www.unicode.org/unicode/reports/tr9/tr9-3.html When setting
     * text using the Arabic script in vertical lines, it is more common to
     * employ a horizontal baseline that is rotated by 90 % counterclockwise so
     * that the characters are ordered from top to bottom. Latin text and numbers
     * may be rotated 90 % clockwise so that the characters are also ordered from
     * top to bottom.
     * <p/>
     * Rotation rules - Roman can rotate left, right, or none - default right
     * (counterclockwise) - CJK can't rotate - Arabic must rotate - default left
     * (clockwise)
     * <p/>
     * from the online edition of _The Unicode Standard, Version 3.0_, file
     * ch10.pdf page 4 Ideographs are found in three blocks of the Unicode
     * Standard... U+4E00-U+9FFF, U+3400-U+4DFF, U+F900-U+FAFF
     * <p/>
     * Hiragana is U+3040-U+309F, katakana is U+30A0-U+30FF
     * <p/>
     * from http://www.unicode.org/unicode/faq/writingdirections.html East Asian
     * scripts are frequently written in vertical lines which run from
     * top-to-bottom and are arrange columns either from left-to-right
     * (Mongolian) or right-to-left (other scripts). Most characters use the same
     * shape and orientation when displayed horizontally or vertically, but many
     * punctuation characters will change their shape when displayed vertically.
     * <p/>
     * Letters and words from other scripts are generally rotated through ninety
     * degree angles so that they, too, will read from top to bottom. That is,
     * letters from left-to-right scripts will be rotated clockwise and letters
     * from right-to-left scripts counterclockwise, both through ninety degree
     * angles.
     * <p/>
     * Unlike the bidirectional case, the choice of vertical layout is usually
     * treated as a formatting style; therefore, the Unicode Standard does not
     * define default rendering behavior for vertical text nor provide
     * directionality controls designed to override such behavior
     */
    public static int verifyRotation(String label, int rotateHint) {
        boolean hasCJK = false;
        boolean hasMustRotate = false; // Arabic, etc

        int len = label.length();
        char data[] = new char[len];
        char ch;
        label.getChars(0, len, data, 0);
        for (int i = 0; i < len; i++) {
            ch = data[i];
            if ((ch >= '\u4E00' && ch <= '\u9FFF') || (ch >= '\u3400' && ch <= '\u4DFF')
                    || (ch >= '\uF900' && ch <= '\uFAFF') || (ch >= '\u3040' && ch <= '\u309F')
                    || (ch >= '\u30A0' && ch <= '\u30FF')) {
                hasCJK = true;
            }
            if ((ch >= '\u0590' && ch <= '\u05FF') || // Hebrew
                    (ch >= '\u0600' && ch <= '\u06FF') || // Arabic
                    (ch >= '\u0700' && ch <= '\u074F')) // Syriac
            {
                hasMustRotate = true;
            }
        }
        // If you mix Arabic with Chinese, you're on your own
        if (hasCJK) {
            return DEFAULT_CJK;
        }

        int legal = hasMustRotate ? LEGAL_MUST_ROTATE : LEGAL_ROMAN;
        if ((rotateHint & legal) > 0) {
            return rotateHint;
        }

        // The hint wasn't legal, or it was zero
        return hasMustRotate ? DEFAULT_MUST_ROTATE : DEFAULT_ROMAN;
    }

    // The small kana characters and Japanese punctuation that draw in the top
    // right quadrant:
    // small a, i, u, e, o, tsu, ya, yu, yo, wa (katakana only) ka ke
    static final String sDrawsInTopRight = "\u3041\u3043\u3045\u3047\u3049\u3063\u3083\u3085\u3087\u308E" + // hiragana
            "\u30A1\u30A3\u30A5\u30A7\u30A9\u30C3\u30E3\u30E5\u30E7\u30EE\u30F5\u30F6"; // katakana
    static final String sDrawsInFarTopRight = "\u3001\u3002"; // comma, full stop

    static final int DEFAULT_CJK = ROTATE_NONE;
    static final int LEGAL_ROMAN = ROTATE_NONE | ROTATE_LEFT | ROTATE_RIGHT;
    static final int DEFAULT_ROMAN = ROTATE_RIGHT;
    static final int LEGAL_MUST_ROTATE = ROTATE_LEFT | ROTATE_RIGHT;
    static final int DEFAULT_MUST_ROTATE = ROTATE_LEFT;

    static final double NINETY_DEGREES = Math.toRadians(90.0);
    static final int kBufferSpace = 5;
}
