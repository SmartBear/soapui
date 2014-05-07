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
package com.smartbear.soapui.utils.fest;

import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.finder.FrameFinder;

import javax.swing.*;
import java.awt.*;

import static org.fest.swing.finder.WindowFinder.findDialog;
import static org.fest.swing.finder.WindowFinder.findFrame;

/**
 * @author Prakash
 */
public final class FestMatchers {
    private FestMatchers() {
        throw new AssertionError();
    }

    public static FrameFinder frameWithTitle(final String expectedTitle) {
        return findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            @Override
            protected boolean isMatching(Frame component) {
                return doesStringStartWith(component.getTitle(), expectedTitle);
            }
        });
    }

    public static DialogFinder dialogWithTitle(final String expectedTitle) {
        return findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog component) {
                return doesStringStartWith(component.getTitle(), expectedTitle);
            }
        });
    }

    public static GenericTypeMatcher<JButton> buttonWithText(final String expectedText) {
        return new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return doesStringStartWith(button.getText(), expectedText);
            }
        };
    }

    public static GenericTypeMatcher<JMenuItem> menuItemWithText(final String expectedText) {
        return new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return doesStringStartWith(menuItem.getText(), expectedText);
            }
        };
    }

    private static boolean doesStringStartWith(String mainString, String subString) {
        return mainString == subString || (mainString != null && subString != null &&
                mainString.startsWith(subString));
    }
}
