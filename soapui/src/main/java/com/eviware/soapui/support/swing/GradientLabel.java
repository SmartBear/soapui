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

package com.eviware.soapui.support.swing;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

public class GradientLabel extends JLabel {
    // ------------------------------ FIELDS ------------------------------

    private Color start;
    private Color end;

    // --------------------------- CONSTRUCTORS ---------------------------

    public GradientLabel(String text) {
        super(text);

        start = Color.LIGHT_GRAY;
        end = getBackground();
    }

    public GradientLabel(String text, Color start, Color end) {
        super(text);
        this.start = start;
        this.end = end;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // Create the gradient paint
        GradientPaint paint = new GradientPaint(0, 0, start, width, height, end, true);

        // we need to cast to Graphics2D for this operation
        Graphics2D g2d = (Graphics2D) g;

        // save the old paint
        Paint oldPaint = g2d.getPaint();

        // set the paint to use for this operation
        g2d.setPaint(paint);

        // fill the background using the paint
        g2d.fillRect(0, 0, width, height);

        // restore the original paint
        g2d.setPaint(oldPaint);

        super.paint(g);
    }
}
