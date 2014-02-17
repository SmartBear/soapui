package com.eviware.soapui.impl.rest.actions.oauth;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 2/17/14
 * Time: 8:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class JavaScriptValidator
{
	public boolean validate( String script )
	{
		Context mozillaJavaScriptContext = Context.enter();
		try
		{
			mozillaJavaScriptContext.compileString( script, "scriptToValidate", 1, null );
			return true;
		}
		catch( EvaluatorException e )
		{
			return false;
		}
	}
}
