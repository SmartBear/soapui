package com.eviware.soapui.impl.rest.actions.oauth;

/**
 * A no-op adapter for the BrowserListener interface.
 */
public class BrowserListenerAdapter implements BrowserListener
{
	@Override
	public void locationChanged( String newLocation )
	{

	}

	@Override
	public void contentChanged( String newContent )
	{

	}

	@Override
	public void javaScriptErrorOccurred( String script, String location, Exception error )
	{

	}

	@Override
	public void browserClosed()
	{

	}
}
