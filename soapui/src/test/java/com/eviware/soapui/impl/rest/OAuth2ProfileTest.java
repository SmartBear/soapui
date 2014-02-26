package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the OAuth2Profile class.
 */
public class OAuth2ProfileTest
{

	private OAuth2Profile profile;

	@Before
	public void setUp() throws Exception
	{
		profile = OAuth2TestUtils.getOAuthProfileWithDefaultValues();

	}

	@Test
	public void waitsForAccessTokenStatusChange() throws Exception
	{
		final String accessToken = "mock token";
		profile.waitingForAuthorization();

		Runnable simulatedAccessTokenRetrieval = new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(50);
				}
				catch( InterruptedException ignore )
				{

				}
				profile.applyRetrievedAccessToken( accessToken );
			}
		};
		new Thread(simulatedAccessTokenRetrieval).start();
		profile.waitForAccessTokenStatus( OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER, 1000);

		assertThat(profile.getAccessToken(), is(accessToken));
	}

	@Test
	public void ignoresIntermediateAccessTokenStatusChanges() throws Exception
	{
		final String accessToken = "mock token";
		profile.waitingForAuthorization();

		Runnable simulatedAccessTokenRetrieval = new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(50);
					profile.waitingForAuthorization();
					Thread.sleep(10);
				}
				catch( InterruptedException ignore )
				{

				}
				profile.applyRetrievedAccessToken( accessToken );
			}
		};
		new Thread(simulatedAccessTokenRetrieval).start();
		profile.waitForAccessTokenStatus( OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER, 1000);

		assertThat(profile.getAccessToken(), is(accessToken));
	}

	@Test
	public void appliesTimeOutCorrectlyEvenOnMultipleStatusChanges() throws Exception
	{
		final String accessToken = "mock token";
		profile.waitingForAuthorization();

		Runnable simulatedAccessTokenRetrieval = new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(100);
					profile.waitingForAuthorization();
					Thread.sleep(100);
				}
				catch( InterruptedException ignore )
				{

				}
				profile.applyRetrievedAccessToken( accessToken );
			}
		};
		new Thread(simulatedAccessTokenRetrieval).start();
		profile.waitForAccessTokenStatus( OAuth2Profile.AccessTokenStatus.RETRIEVED_FROM_SERVER, 150);

		assertThat(profile.getAccessToken(), is(not((accessToken ))));
	}
}
