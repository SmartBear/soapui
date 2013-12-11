/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.oauth;

import com.eviware.soapui.config.OAuth2ProfileConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.awt.event.ActionEvent;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for RefreshOAuthAccessTokenAction
 */
public class RefreshOAuthAccessTokenActionTest
{

	public static final ActionEvent DUMMY_ACTION_EVENT = new ActionEvent( new Object(), 0, "click" );
	private XDialogs originalDialogs;
	private StubbedDialogs stubbedDialogs;
	private OAuth2Profile profile;
	private RefreshOAuthAccessTokenAction action;
	private OAuth2ClientFacade clientFacade;


	@Before
	public void setUp() throws Exception
	{
		originalDialogs = UISupport.getDialogs();
		stubbedDialogs = new StubbedDialogs();
		UISupport.setDialogs( stubbedDialogs );
		OAuth2ProfileConfig configuration = OAuth2ProfileConfig.Factory.newInstance();
		profile = new OAuth2Profile( ModelItemFactory.makeOAuth2ProfileContainer(), configuration );

		clientFacade = mock(OAuth2ClientFacade.class);
		action = new RefreshOAuthAccessTokenAction(profile){
			@Override
			protected OAuth2ClientFacade getOAuthClientFacade()
			{
				return clientFacade;
			}
		};
	}

	@After
	public void tearDown() throws Exception
	{
		UISupport.setDialogs( originalDialogs );
	}

	@Test
	public void savesAccessTokenInProfile() throws Exception
	{

		final String accessToken = "4/98789adfc8234278243987";
		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocationOnMock ) throws Throwable
			{
				profile.setAccessToken( accessToken );
				return null;
			}
		} ).when( clientFacade ).refreshAccessToken( profile );
		action.actionPerformed( DUMMY_ACTION_EVENT );
		assertThat( profile.getAccessToken(), is(accessToken) );
	}

	@Test
	public void showsAnErrorMessageWhenGetAccessTokenFails() throws Exception
	{
		Mockito.doThrow( new OAuth2Exception( new RuntimeException (  )) ).when(clientFacade).refreshAccessToken( profile );

		action.actionPerformed(DUMMY_ACTION_EVENT);
		assertThat( stubbedDialogs.getErrorMessages(), is( aCollectionWithSize( 1 ) ) );
	}

	@Test
	public void displaysValidationErrorWhenValidationFails() throws Exception
	{
		String theMessage = "Client ID is empty";
		Mockito.doThrow( new InvalidOAuth2ParametersException ( theMessage  )).when( clientFacade ).refreshAccessToken( profile );

		action.actionPerformed( DUMMY_ACTION_EVENT );
		assertThat( stubbedDialogs.getErrorMessages(), is( aCollectionWithSize( 1 ) ) );
		assertThat( stubbedDialogs.getErrorMessages().get(0), containsString(theMessage) );
	}

	@Ignore("Ignored until requirement is clarified - see comment in RefreshOAuthAccessTokenAction.isEnabled()")
	@Test
	public void disabledIfAndOnlyIfRefreshTokenIsEmpty() throws Exception
	{
		profile.setRefreshToken( "" );
		action = new RefreshOAuthAccessTokenAction( profile );
		assertThat(action.isEnabled(), is(false));
		profile.setRefreshToken( "some_refresh_token" );
		assertThat( action.isEnabled(), is(true));
	}
}
