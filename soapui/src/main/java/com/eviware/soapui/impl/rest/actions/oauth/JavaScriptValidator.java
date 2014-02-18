package com.eviware.soapui.impl.rest.actions.oauth;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

/**
 * Simple Javascript syntax validator.
 */
public class JavaScriptValidator
{
	public JavaScriptValidationError validate( String script )
	{
		Context mozillaJavaScriptContext = Context.enter();
		try
		{
			mozillaJavaScriptContext.compileString( script, "scriptToValidate", 1, null );
			return null;
		}
		catch( EvaluatorException e )
		{
			return new JavaScriptValidationError( e.getMessage(), e.lineNumber());
		}
	}

}
