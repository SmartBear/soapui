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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;

/**
 * Created by IntelliJ IDEA.
 */

public class GradientPanel extends JPanel {
    // ------------------------------ FIELDS ------------------------------

    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;
    public final static int DIAGONAL_LEFT = 2;
    public final static int DIAGONAL_RIGHT = 3;

    private int direction = HORIZONTAL;
    private boolean cyclic;
    private int maxLength;

    // --------------------------- CONSTRUCTORS ---------------------------

    public GradientPanel() {
        this(HORIZONTAL);
    }

    public GradientPanel(int direction) {
        super(new BorderLayout());
        setOpaque(false);
        this.direction = direction;
    }

    public GradientPanel(LayoutManager layoutManager) {
        super(layoutManager);
        setOpaque(false);
        this.direction = HORIZONTAL;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void paintComponent(Graphics g) {
        if (isOpaque()) {
            super.paintComponent(g);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // Create the gradient paint
        GradientPaint paint = null;

        Color sc = getForeground();
        Color ec = getBackground();

        switch (direction) {
            case HORIZONTAL: {
                paint = new GradientPaint(0, height / 2, sc, width, height / 2, ec, cyclic);
                break;
            }
            case VERTICAL: {
                paint = new GradientPaint(width / 2, 0, sc, width / 2, maxLength > 0 ? maxLength : height, ec, cyclic);
                break;
            }
            case DIAGONAL_LEFT: {
                paint = new GradientPaint(0, 0, sc, width, height, ec, cyclic);
                break;
            }
            case DIAGONAL_RIGHT: {
                paint = new GradientPaint(width, 0, sc, 0, height, ec, cyclic);
                break;
            }
        }

        if (paint == null) {
            throw new RuntimeException("Invalid direction specified in GradientPanel");
        }

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

        super.paintComponent(g);

    }
}
