package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.finder.DialogFinder;
import org.fest.swing.fixture.DialogFixture;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition.HEADER;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition.QUERY;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_JSON;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_URL_ENCODED_FORM;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class OAuth2AdvanceOptionsDialogTest
{
	private Robot robot;
	private OAuth2Profile profile;

	@Before
	public void setUp() throws SoapUIException
	{
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		XFormFactory.Factory.instance = new SwingFormFactory();
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		profile = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(), configuration );
	}

	@Test
	public void setsParameterToProfile() throws SoapUIException, InterruptedException
	{
		//before
		assertThat( profile.getAccessTokenPosition(), is( HEADER ) );
		assertThat( profile.getAccessTokenRetrievalLocation(), is( BODY_JSON ) );

		new Thread(
				new Runnable()
				{
					@Override
					public void run()
					{
						new OAuth2AdvanceOptionsDialog( profile );
					}
				} ).start();

		//wait for the dialog to show up
		Thread.sleep( 200 );

		Dialog advanceOptionsDialog = ( Dialog ) robot.finder().findByName( "OAuth2.0 Advanced options" );
		DialogFixture dialogFixture = new DialogFixture( robot, advanceOptionsDialog );

		dialogFixture.radioButton( QUERY.toString() ).click();
		dialogFixture.radioButton( BODY_URL_ENCODED_FORM.toString() ).click();
		dialogFixture.button( "OK" ).click();

		//Changed through dialog are saved to profile
		assertThat( profile.getAccessTokenPosition(), is( QUERY ) );
		assertThat( profile.getAccessTokenRetrievalLocation(), is( BODY_URL_ENCODED_FORM ) );

	}
}
