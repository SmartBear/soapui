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

package com.eviware.soapui.ui.desktop.standalone;

import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import java.beans.PropertyVetoException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A DesktopManager managing the internal frames in Desktop using a Most-Recently-Used order when changing
 * the active internal frame. A Deque (a stack-like data structure) is used to keep track of frames.
 * <ul>
 * <li><code>activateFrame(JInternalFrame)</code> puts the frame at top of stack : if frame was already present
 * in stack, remove it then add it at top, otherwise, add it at top.</li>
 * <li><code>deactivateFrame(JInternalFrame)</code> is a noop on the stack, delegate to superclass.</li>
 * <li><code>closeFrame(JInternalFrame)</code> removes frame from the stack and selects the frame at top of stack.</li>
 * <li><code>iconifyFrame(JInternalFrame></code> is like <code>closeFrame</code> as far as this manager is concerned,
 * but with iconifyFrame on superclass called.</li>
 * <li><code>deiconifyFrame</code> delegates to superclass to bring back the frame on desktop, puts it at top of stack
 * and makes sure it is selected.</li>
 * </ul>
 * At anytime, there is one frame selected (unless there are no (open) frames at all) and that frame is the top of
 * stack.
 */
public class MostRecentlyUsedOrderDesktopManager implements DesktopManager {
    // Keep desktop panel list (JInternalFrame) of existing internal frames in a most-recently-used order (i.e. a stack).
    Deque<JInternalFrame> mostRecentlyUsedFrames = new ArrayDeque<JInternalFrame>();

    private DesktopManager delegate;
    // this is used to prevent AquaInternalFrameManager from activating another pane when we are closing one on Mac
    private boolean isClosingFrame;

    public MostRecentlyUsedOrderDesktopManager(DesktopManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void activateFrame(JInternalFrame f) {
        if (f == null || isClosingFrame) {
            return;
        }
        delegate.activateFrame(f);
        if (!mostRecentlyUsedFrames.isEmpty() && f.equals(mostRecentlyUsedFrames.getFirst())) {
            selectTopFrame(null);
            return;
        } else if (!mostRecentlyUsedFrames.isEmpty() && mostRecentlyUsedFrames.contains(f)) {
            mostRecentlyUsedFrames.remove(f);
        }
        JInternalFrame previousTop = mostRecentlyUsedFrames.isEmpty() ? null : mostRecentlyUsedFrames.getFirst();
        mostRecentlyUsedFrames.addFirst(f);
        selectTopFrame(previousTop);
    }

    @Override
    public void beginDraggingFrame(JComponent f) {
        delegate.beginDraggingFrame(f);
    }

    @Override
    public void beginResizingFrame(JComponent f, int direction) {
        delegate.beginResizingFrame(f, direction);
    }

    @Override
    public void deactivateFrame(JInternalFrame f) {
        delegate.deactivateFrame(f);
    }

    @Override
    public void closeFrame(JInternalFrame f) {
        mostRecentlyUsedFrames.remove(f);
        try {
            isClosingFrame = true;
            delegate.closeFrame(f);
        } finally {
            isClosingFrame = false;
        }
        selectTopFrame(f);
    }

    @Override
    public void iconifyFrame(JInternalFrame f) {
        mostRecentlyUsedFrames.remove(f);
        selectTopFrame(f);
        delegate.iconifyFrame(f);
    }

    @Override
    public void maximizeFrame(JInternalFrame f) {
        delegate.maximizeFrame(f);
    }

    @Override
    public void minimizeFrame(JInternalFrame f) {
        delegate.minimizeFrame(f);
    }

    @Override
    public void openFrame(JInternalFrame f) {
        delegate.openFrame(f);
    }

    @Override
    public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
        delegate.resizeFrame(f, newX, newY, newWidth, newHeight);
    }

    @Override
    public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
        delegate.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
    }

    @Override
    public void deiconifyFrame(JInternalFrame f) {
        delegate.deiconifyFrame(f);
        activateFrame(f);
    }

    @Override
    public void dragFrame(JComponent f, int newX, int newY) {
        delegate.dragFrame(f, newX, newY);
    }

    @Override
    public void endDraggingFrame(JComponent f) {
        delegate.endDraggingFrame(f);
    }

    @Override
    public void endResizingFrame(JComponent f) {
        delegate.endResizingFrame(f);
    }

    protected void selectTopFrame(JInternalFrame previousTopFrame) {
        JInternalFrame topFrame;
        try {
            if (mostRecentlyUsedFrames.isEmpty()) {
                return;
            } else {
                topFrame = mostRecentlyUsedFrames.getFirst();
            }
            if (previousTopFrame != null && !previousTopFrame.equals(topFrame)) {
                if (previousTopFrame.isSelected()) {
                    previousTopFrame.setSelected(false);
                }
            }
            if (!topFrame.isSelected()) {
                topFrame.setSelected(true);
            }
        } catch (PropertyVetoException ignore) {
        }
    }

}
