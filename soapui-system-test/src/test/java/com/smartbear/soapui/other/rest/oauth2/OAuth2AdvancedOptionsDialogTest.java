package com.smartbear.soapui.other.rest.oauth2;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2AdvanceOptionsDialog;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import com.smartbear.soapui.utils.IntegrationTest;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.awt.Dialog;

import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition.HEADER;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition.QUERY;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_JSON;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_URL_ENCODED_FORM;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

// TODO This hangs when running it together with the other tests, find out why
@Ignore
@Category( IntegrationTest.class )
public class OAuth2AdvancedOptionsDialogTest
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
		Thread.sleep( 3000 );

		Dialog advanceOptionsDialog = ( Dialog )robot.finder().findByName( "OAuth2.0 Advanced options" );
		DialogFixture dialogFixture = new DialogFixture( robot, advanceOptionsDialog );

		dialogFixture.radioButton( QUERY.toString() ).click();
		dialogFixture.radioButton( BODY_URL_ENCODED_FORM.toString() ).click();
		dialogFixture.button( "OK" ).click();

		//Changed through dialog are saved to profile
		assertThat( profile.getAccessTokenPosition(), is( QUERY ) );
		assertThat( profile.getAccessTokenRetrievalLocation(), is( BODY_URL_ENCODED_FORM ) );
	}
}
