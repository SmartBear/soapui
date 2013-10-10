package com.eviware.soapui.impl.rest.panels.request;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
* Created with IntelliJ IDEA.
* User: manne
* Date: 10/9/13
* Time: 3:42 PM
* To change this template use File | Settings | File Templates.
*/
class ParameterFinder
{

	private String parametersString;

	private List<String> tokens;

	public ParameterFinder( String parametersString )
	{

		this.parametersString = parametersString;
		StringTokenizer parser = new StringTokenizer( parametersString, "?&=;", true );
		List<String> parsedTokens = new ArrayList<String>();
		while (parser.hasMoreTokens())
		{
			parsedTokens.add(parser.nextToken());
		}
		tokens = parsedTokens;
	}

	public String findParameterAt( int dot )
	{
		String token = getTokenForChar( dot );
		if (token.equals("&"))
		{
			return getTokenForChar( dot + 1 );
		}
		else if (token.equals("="))
		{
			return getTokenForChar( dot -1 );
		}
		return token;
	}

	private String getTokenForChar( int index )
	{
		int currentIndex = 0;
		for( String token : tokens )
		{
			if (index >= currentIndex && index < currentIndex + token.length())
			{
				return token;
			}
			currentIndex += token.length();
		}
		return "";
	}

}
