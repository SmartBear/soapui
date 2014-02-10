package com.eviware.soapui.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.ConsoleDialogs;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.inspectors.auth.AuthInspectorFactory;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2AuthenticationInspector;
import com.eviware.soapui.utils.ApplicationUtils;
import com.eviware.soapui.utils.RestProjectUtils;
import com.eviware.soapui.utils.SoapProjectUtils;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.security.ExitCallHook;
import org.fest.swing.security.NoExitSecurityManagerInstaller;
import org.junit.*;

import java.awt.*;

import static com.eviware.soapui.utils.FestMatchers.frameWithTitle;
import static org.fest.swing.launcher.ApplicationLauncher.application;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Ignore
public class BasicOAuth2ConfigurationIT
{
	private static final String CLIENT_ID = "client-id";
	private static final String CLIENT_SECRET = "client-secret";
	private static final String AUTHORIZATION_URI = "authorization-uri";
	private static final String ACCESS_TOKEN_URI = "access-token-uri";
	private static final String REDIRECT_URI = "redirect-uri";
	private static final String SCOPE = "scope";
	private static final String ACCESS_TOKEN = "access-token";

	private static final String OAUTH_2_COMBOBOX_ITEM = CredentialsConfig.AuthType.O_AUTH_2.toString();
	private static final String GLOBAL_HTTP_SETTINGS_COMBOBOX_ITEM = CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString();
	private static final Dimension WINDOW_SIZE = new Dimension( 1024, 768 );

	private static NoExitSecurityManagerInstaller noExitSecurityManagerInstaller;

	private Robot robot;
	private FrameFixture rootWindow;

	@BeforeClass
	public static void setUpOnce()
	{
		noExitSecurityManagerInstaller = NoExitSecurityManagerInstaller.installNoExitSecurityManager( new ExitCallHook()
		{
			@Override
			public void exitCalled( int status )
			{
				System.out.print( "Exit status : " + status );
			}
		} );

		// Disabled due to SOAP-1271
		//FailOnThreadViolationRepaintManager.install();
	}

	@AfterClass
	public static void tearDown()
	{
		noExitSecurityManagerInstaller.uninstall();
	}

	@Before
	public void setup()
	{
		// TODO Get settings file

		System.setProperty( "soapui.jxbrowser.disable", "true" );

		robot = BasicRobot.robotWithNewAwtHierarchy();

		application( SoapUI.class ).start();

		rootWindow = frameWithTitle( "SoapUI" ).withTimeout( 3000 ).using( robot );
		rootWindow.show( WINDOW_SIZE );
	}

	@After
	public void after()
	{
		robot.cleanUp();
		UISupport.setDialogs( new ConsoleDialogs() );
	}

	@Test
	public void testUsingARequestNotSupportingOAuth()
	{
		SoapProjectUtils.createNewSoapProject(rootWindow, robot  );
		SoapProjectUtils.openRequestEditor( rootWindow );
		clickOnTheAuthTab( rootWindow );
		verifyThatTheOAuth2ItemIsNotPresent( rootWindow );
		ApplicationUtils.closeApplicationWithoutSaving( rootWindow, robot );
	}

	@Test
	public void testFillInBasicValues()
	{
		RestProjectUtils.createNewRestProject( rootWindow, robot );
		clickOnTheAuthTab( rootWindow );
		clickOnComboBoxItem( rootWindow, OAUTH_2_COMBOBOX_ITEM );
		fillInAllOAuth2Fields( rootWindow );
		clickOnComboBoxItem( rootWindow, GLOBAL_HTTP_SETTINGS_COMBOBOX_ITEM );
		clickOnComboBoxItem( rootWindow, OAUTH_2_COMBOBOX_ITEM );
		verifyAllOAuth2Fields( rootWindow );
		ApplicationUtils.closeApplicationWithoutSaving( rootWindow, robot);
	}

	private void clickOnTheAuthTab( FrameFixture rootWindow )
	{
		rootWindow.toggleButton( AuthInspectorFactory.INSPECTOR_ID ).click();
	}

	private void clickOnComboBoxItem( FrameFixture rootWindow, String itemName )
	{
		rootWindow.comboBox( OAuth2AuthenticationInspector.COMBO_BOX_LABEL ).selectItem( itemName );
	}

	private void fillInAllOAuth2Fields( FrameFixture rootWindow )
	{
		rootWindow.textBox( OAuth2Profile.CLIENT_ID_PROPERTY ).setText( CLIENT_ID );
		rootWindow.textBox( OAuth2Profile.CLIENT_SECRET_PROPERTY ).setText( CLIENT_SECRET );
		rootWindow.textBox( OAuth2Profile.AUTHORIZATION_URI_PROPERTY ).setText( AUTHORIZATION_URI );
		rootWindow.textBox( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY ).setText( ACCESS_TOKEN_URI );
		rootWindow.textBox( OAuth2Profile.REDIRECT_URI_PROPERTY ).setText( REDIRECT_URI );
		rootWindow.textBox( OAuth2Profile.SCOPE_PROPERTY ).setText( SCOPE );
		rootWindow.textBox( OAuth2Profile.ACCESS_TOKEN_PROPERTY ).setText( ACCESS_TOKEN );
	}

	private void verifyAllOAuth2Fields( FrameFixture rootWindow )
	{
		assertThat( rootWindow.textBox( OAuth2Profile.CLIENT_ID_PROPERTY ).text(), is( CLIENT_ID ) );
		assertThat( rootWindow.textBox( OAuth2Profile.CLIENT_SECRET_PROPERTY ).text(), is( CLIENT_SECRET ) );
		assertThat( rootWindow.textBox( OAuth2Profile.AUTHORIZATION_URI_PROPERTY ).text(), is( AUTHORIZATION_URI ) );
		assertThat( rootWindow.textBox( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY ).text(), is( ACCESS_TOKEN_URI ) );
		assertThat( rootWindow.textBox( OAuth2Profile.REDIRECT_URI_PROPERTY ).text(), is( REDIRECT_URI ) );
		assertThat( rootWindow.textBox( OAuth2Profile.SCOPE_PROPERTY ).text(), is( SCOPE ) );
		assertThat( rootWindow.textBox( OAuth2Profile.ACCESS_TOKEN_PROPERTY ).text(), is( ACCESS_TOKEN ) );
	}

	private void verifyThatTheOAuth2ItemIsNotPresent( FrameFixture rootWindow )
	{
		assertThat( rootWindow.comboBox( OAuth2AuthenticationInspector.COMBO_BOX_LABEL )
				.contents(), not( hasItemInArray( OAUTH_2_COMBOBOX_ITEM ) ) );
	}
}