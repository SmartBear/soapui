/*
 * soapUI, copyright (C) 2004-2014 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
import com.eviware.soapui.utils.ContainerWalker;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import static com.eviware.soapui.utils.SwingMatchers.enabled;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class ExpirationTimeChooserTest
{

	private ExpirationTimeChooser chooser;
	private OAuth2Profile profile;
	private ContainerWalker walker;

	@Before
	public void setUp() throws Exception
	{
		profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();
		chooser = new ExpirationTimeChooser(profile);
		walker = new ContainerWalker( chooser );
	}

	@Test
	public void timeFieldsDisabledOnStart() throws Exception
	{
		assertThat(walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class), is(not( enabled() )));
		assertThat(walker.findComponent(ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class), is(not( enabled() )));

	}

	@Test
	public void timeFieldsEnabledWhenManualIsSelected() throws Exception
	{
		walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
		assertThat( walker.findComponent( ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class ), is( enabled() ) );
		assertThat(walker.findComponent( ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class ), is( enabled() ));

	}

	@Test
	public void getsEnteredSecondsCorrectly() throws Exception
	{
		walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
		walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class).setText("1234");

		assertThat( chooser.getAccessTokenExpirationTimeInSeconds(), is( 1234 ) );

	}
	
	@Test
	public void getsEnteredMinutesCorrectly() throws Exception
	{
		walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
		walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class).setText( "3" );
		walker.findComponent(ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class).setSelectedIndex( 1 ); //minutes

		assertThat( chooser.getAccessTokenExpirationTimeInSeconds(), is( 180 ) );
	}

	@Test
	public void getsEnteredHoursCorrectly() throws Exception
	{
		walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
		walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class).setText( "2" );
		walker.findComponent(ExpirationTimeChooser.TIME_UNIT_COMBO_NAME, JComboBox.class).setSelectedIndex( 2 ); //minutes

		assertThat( chooser.getAccessTokenExpirationTimeInSeconds(), is( 7200 ) );
	}

	@Test
	public void returnsDefaultValueWhenNonNumberEntered() throws Exception
	{
		walker.findComponent(ExpirationTimeChooser.MANUAL_EXPIRATION_RADIO_NAME, JRadioButton.class).doClick();
		walker.findComponent(ExpirationTimeChooser.TIME_FIELD_NAME, JTextField.class).setText( "smurf" );

		assertThat( chooser.getAccessTokenExpirationTimeInSeconds(), is( -1 ) );
	}

}
