package com.eviware.soapui.impl.rest.actions.oauth;

/**
* Created with IntelliJ IDEA.
* User: manne
* Date: 2/18/14
* Time: 9:36 AM
* To change this template use File | Settings | File Templates.
*/
public class JavaScriptValidationError
{
	private final String errorMessage;
	private final int lineNumber;

	public JavaScriptValidationError( String errorMessage, int lineNumber )
	{
		this.errorMessage = errorMessage;
		this.lineNumber = lineNumber;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}


	public int getLineNumber()
	{
		return lineNumber;
	}
}
