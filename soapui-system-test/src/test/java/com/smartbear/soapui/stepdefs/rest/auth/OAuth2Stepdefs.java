package com.smartbear.soapui.stepdefs.rest.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.editor.inspectors.auth.OAuth2AuthenticationInspector;
import com.smartbear.soapui.stepdefs.ScenarioRobot;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

import static com.smartbear.soapui.utils.fest.ApplicationUtils.getMainWindow;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OAuth2Stepdefs
{
	private static final String CLIENT_ID = "client-id";
	private static final String CLIENT_SECRET = "client-secret";
	private static final String AUTHORIZATION_URI = "authorization-uri";
	private static final String ACCESS_TOKEN_URI = "access-token-uri";
	private static final String REDIRECT_URI = "redirect-uri";
	private static final String SCOPE = "scope";
	private static final String ACCESS_TOKEN = "access-token";

	private static final String ADVANCE_OPTIONS_DIALOG_NAME = "OAuth2.0 Advanced options";

	private static final String OAUTH_2_COMBOBOX_ITEM = CredentialsConfig.AuthType.O_AUTH_2.toString();
	private static final String GLOBAL_HTTP_SETTINGS_COMBOBOX_ITEM = CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString();
	public static final String BUTTON_OK = "OK";

	private FrameFixture rootWindow;

	public OAuth2Stepdefs( ScenarioRobot runner )
	{
		Robot robot = runner.getRobot();
		rootWindow = getMainWindow( robot );
	}

	@When( "^clicks on the OAuth 2 Authorization Type$" )
	public void clicksOnTheOAuth2AuthorizationType()
	{
		clickOnComboBoxItem( rootWindow, OAUTH_2_COMBOBOX_ITEM );
	}

	@When( "^and fills out all fields$" )
	public void fillInAllOAuth2Fields()
	{
		fillInAllOAuth2Fields( rootWindow );
	}

	@When( "^switches to another Authorization type and then back again$" )
	public void switchToAnotherAuthorizationTypeAndThenBackAgain()
	{
		clickOnComboBoxItem( rootWindow, GLOBAL_HTTP_SETTINGS_COMBOBOX_ITEM );
		clickOnComboBoxItem( rootWindow, OAUTH_2_COMBOBOX_ITEM );
	}

	@When( "^user clicks on Advance options button$" )
	public void clickOnAdvanceOptionsButton()
	{
		rootWindow.button( OAuth2AuthenticationInspector.ADVANCED_OPTIONS ).click();
	}

	@When( "^user selects access token position (.+)$" )
	public void selectAccessTokenPosition(String accessTokenPosition)
	{
		getAdvanceDialogFixture().radioButton( accessTokenPosition ).click();
	}

	@When( "^user selects access token retrieval location (.+)$" )
	public void selectAccessTokenRetrievalLocation(String accessTokenRetrievalLocation)
	{
		getAdvanceDialogFixture().radioButton( accessTokenRetrievalLocation ).click();
	}


	@When( "^closes and reopens the advance options dialog" )
	public void closeAndReOpenAdvanceOptionsDialog()
	{
		closeAdvanceOptionsDialog();
		clickOnAdvanceOptionsButton();
	}

	@When( "^closes the advance options dialog" )
	public void closesAdvanceOptionsDialog()
	{
		closeAdvanceOptionsDialog();
	}


	@Then( "^the OAuth 2 option is not visible in the Authentication Type dropdown$" )
	public void verifyThatOAuth2OptionIsNotShownInAuthenticationDropdown()
	{
		assertThat( rootWindow.comboBox( OAuth2AuthenticationInspector.COMBO_BOX_LABEL )
				.contents(), not( hasItemInArray( OAUTH_2_COMBOBOX_ITEM ) ) );
	}

	@Then( "^the previously filled fields are still present$" )
	public void verifyThatThePreviouslyFilledFieldsAreStillPresent()
	{
		verifyAllOAuth2Fields( rootWindow );
	}

	@Then( "^access token position is (.+)$" )
	public void verifyAccessTokenPosition(String expectedAccessTokenPosition)
	{
		getAdvanceDialogFixture().radioButton( expectedAccessTokenPosition ).requireSelected();
	}

	@Then( "^access token retrieval location is (.+)$" )
	public void verifyAccessTokenRetrievalLocation(String expectedAccessTokenRetrievalLocation)
	{
		getAdvanceDialogFixture().radioButton( expectedAccessTokenRetrievalLocation ).requireSelected();
	}

	private void closeAdvanceOptionsDialog()
	{
		DialogFixture dialogFixture = getAdvanceDialogFixture();
		dialogFixture.button( BUTTON_OK ).click();
	}

	private DialogFixture getAdvanceDialogFixture()
	{
		return rootWindow.dialog( ADVANCE_OPTIONS_DIALOG_NAME );
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
}