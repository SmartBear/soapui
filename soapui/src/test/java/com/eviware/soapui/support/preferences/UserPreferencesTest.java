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

package com.eviware.soapui.support.preferences;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Rectangle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the UserPreferences class.
 */
public class UserPreferencesTest {

    private static Rectangle originalBounds;

    @BeforeClass
    public static void saveOriginalBounds() {
        originalBounds = new UserPreferences().getSoapUIWindowBounds();
    }

    @AfterClass
    public static void restoreOriginalBounds() throws BackingStoreException {
        new UserPreferences().setSoapUIWindowBounds(originalBounds);
    }

    private UserPreferences preferences;

    @Before
    public void setUp() throws Exception {
        preferences = new UserPreferences();
    }

    @Test
    public void savesAndRetrievesWindowBounds() throws Exception {
        Rectangle windowBounds = new Rectangle(150, 120, 1024, 768);
        preferences.setSoapUIWindowBounds(windowBounds);

        UserPreferences independentPreferences = new UserPreferences();
        assertThat(independentPreferences.getSoapUIWindowBounds(), is(windowBounds));

    }

    @Test
    public void clearsWindowBoundsWhenSettingNull() throws Exception {
        Rectangle windowBounds = new Rectangle(150, 120, 1024, 768);
        preferences.setSoapUIWindowBounds(windowBounds);

        UserPreferences independentPreferences = new UserPreferences();
        independentPreferences.setSoapUIWindowBounds(null);

        UserPreferences reader = new UserPreferences();
        assertThat(reader.getSoapUIWindowBounds(), is(nullValue()));

    }

    @Test
    public void returnsNullWhenPreferencesValuesAreBroken() throws Exception {
        preferences.setSoapUIWindowBounds(new Rectangle(0, 0, 200, 300));
        Preferences.userRoot().node(UserPreferences.ROOT_NODE_NAME).put(UserPreferences.WINDOW_X, "blablabla");

        assertThat(preferences.getSoapUIWindowBounds(), is(nullValue()));

    }
}
