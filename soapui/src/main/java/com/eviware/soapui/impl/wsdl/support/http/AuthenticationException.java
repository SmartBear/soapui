package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.http.ProtocolException;

/**
 * Signals a failure in authentication process
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0
 */
public class AuthenticationException extends ProtocolException
{

	/**
	 * Creates a new AuthenticationException with a <tt>null</tt> detail message.
	 */
	public AuthenticationException()
	{
		super();
	}

	/**
	 * Creates a new AuthenticationException with the specified message.
	 * 
	 * @param message
	 *           the exception detail message
	 */
	public AuthenticationException( String message )
	{
		super( message );
	}

	/**
	 * Creates a new AuthenticationException with the specified detail message
	 * and cause.
	 * 
	 * @param message
	 *           the exception detail message
	 * @param cause
	 *           the <tt>Throwable</tt> that caused this exception, or
	 *           <tt>null</tt> if the cause is unavailable, unknown, or not a
	 *           <tt>Throwable</tt>
	 * 
	 * @since 3.0
	 */
	public AuthenticationException( String message, Throwable cause )
	{
		super( message, cause );
	}

}
