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

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
import com.eviware.soapui.utils.ContainerWalker;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import static com.eviware.soapui.utils.SwingMatchers.enabled;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class ExpirationTimeChooserTest {

    private ExpirationTimeChooser chooser;
    private OAuth2Profile profileWithServerExpiration;
    private ContainerWalker walker;

    @Before
    public void setUp() throws Exception {
        profileWithServerExpiration = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        initializeChooserFromProfile(profileWithServerExpiration);
    }

    private void initializeChooserFromProfile(OAuth2Profile profile) {
        chooser = new ExpirationTimeChooser(profile);
        walker = new ContainerWalker(chooser);
    }

    @Test
    public void serverExpirationRadioButtonIsSelectedWhenManualIsNotSelectedInProfile() throws Exception {
        JRadioButton serverRadioButton = walker.findComponent(ExpirationTimeChooser.SERVER_EXPIRATION_RADIO_NAME, JRadioButton.class);
        assertThat(serverRadioButton, is(selected()));
    }

    @Test
    public void manualRadioButtonIsSelectedWhenManualIsSelectedInProfile() throws Exception {
        OAuth2Profile profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        profile.setUseManualAccessTokenExpirationTime(true);
        initializeChooserFromProfile(profile);

        JRadioButton manualRadioButton = walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class);
        assertThat(manualRadioButton, is(selected()));
    }

    @Test
    public void timeFieldsDisabledOnStart() throws Exception {
        assertThat(walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class), is(not(enabled())));
        assertThat(walker.findComponent(ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class), is(not(enabled())));

    }

    @Test
    public void timeFieldsEnabledWhenManualIsSelected() throws Exception {
        walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
        assertThat(walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class), is(enabled()));
        assertThat(walker.findComponent(ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class), is(enabled()));
    }

    @Test
    public void populatesTimeTextFieldFromProfile() throws Exception {
        OAuth2Profile profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
        profile.setManualAccessTokenExpirationTime("90");
        initializeChooserFromProfile(profile);

        JTextField timeField = walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class);
        assertThat(timeField.getText(), is("90"));
    }

    @Test
    public void timeFieldIsEmptyWhenNoManualTimeSpecifiedInProfile() throws Exception {
        JTextField timeField = walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class);
        assertThat(timeField.getText(), is(""));
    }

    private Matcher<AbstractButton> selected() {
        return new TypeSafeMatcher<AbstractButton>() {
            @Override
            protected boolean matchesSafely(AbstractButton item) {
                return item.isSelected();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a button in selected state");
            }
        };
    }

}
