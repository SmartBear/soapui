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

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;

public class AutoscrollSupport implements Autoscroll {
    private static final int AUTOSCROLL_MARGIN = 12;

    Component comp;
    Insets insets;
    Insets scrollUnits;

    public AutoscrollSupport(Component comp, Insets insets) {
        this(comp, insets, insets);
    }

    public AutoscrollSupport(Component comp, Insets insets, Insets scrollUnits) {
        this.comp = comp;
        this.insets = insets;
        this.scrollUnits = scrollUnits;
    }

    public AutoscrollSupport(Component comp) {
        this(comp, new Insets(AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN, AUTOSCROLL_MARGIN));
    }

    public void autoscroll(Point cursorLoc) {
        JViewport viewport = getViewport();
        if (viewport == null) {
            return;
        }
        Point viewPos = viewport.getViewPosition();
        int viewHeight = viewport.getExtentSize().height;
        int viewWidth = viewport.getExtentSize().width;

        // resolve scrolling
        if ((cursorLoc.y - viewPos.y) < insets.top) { // scroll up
            viewport.setViewPosition(new Point(viewPos.x, Math.max(viewPos.y - scrollUnits.top, 0)));
        } else if ((viewPos.y + viewHeight - cursorLoc.y) < insets.bottom) { // scroll down
            viewport.setViewPosition(new Point(viewPos.x, Math.min(viewPos.y + scrollUnits.bottom, comp.getHeight()
                    - viewHeight)));
        } else if ((cursorLoc.x - viewPos.x) < insets.left) { // scroll left
            viewport.setViewPosition(new Point(Math.max(viewPos.x - scrollUnits.left, 0), viewPos.y));
        } else if ((viewPos.x + viewWidth - cursorLoc.x) < insets.right) { // scroll right
            viewport.setViewPosition(new Point(Math.min(viewPos.x + scrollUnits.right, comp.getWidth() - viewWidth),
                    viewPos.y));
        }
    }

    public Insets getAutoscrollInsets() {
        Rectangle raOuter = comp.getBounds();
        Rectangle raInner = comp.getParent().getBounds();
        return new Insets(raInner.y - raOuter.y + AUTOSCROLL_MARGIN, raInner.x - raOuter.x + comp.getWidth(),
                raOuter.height - raInner.height - raInner.y + raOuter.y + AUTOSCROLL_MARGIN, raOuter.width - raInner.width
                - raInner.x + raOuter.x + AUTOSCROLL_MARGIN);
    }

    JViewport getViewport() {
        return (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, comp);
    }
}
