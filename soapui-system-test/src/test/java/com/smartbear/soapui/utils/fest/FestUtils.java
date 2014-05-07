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

import org.fest.swing.core.Robot;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

import static org.junit.Assert.fail;

/**
 *
 */
public class FestUtils {
    public static void verifyDialogIsNotShowing(String dialogName, Robot robot) {
        try {
            findDialog(dialogName, robot);
            fail("Dialog: " + dialogName + " is still visible");
        } catch (ComponentLookupException e) {
        }
    }

    public static DialogFixture findDialog(String dialogName, Robot robot) {
        return new DialogFixture(robot, dialogName);
    }

    public static void verifyButtonIsNotShowing(FrameFixture rootWindow, String buttonName) {
        try {
            rootWindow.button(buttonName);
            fail("Button: " + buttonName + " is still visible");
        } catch (ComponentLookupException e) {
        }
    }

    public static void verifyTextFieldIsNotShowingInDialog(DialogFixture rootWindow, String fieldName) {
        try {
            rootWindow.textBox(fieldName);
            fail("Text field: " + fieldName + " is still visible");
        } catch (ComponentLookupException e) {
        }
    }
}
