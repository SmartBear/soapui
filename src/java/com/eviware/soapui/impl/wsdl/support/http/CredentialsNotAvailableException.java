package com.eviware.soapui.impl.wsdl.support.http;

/**
 * Authentication credentials required to respond to a authentication challenge
 * are not available
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public class CredentialsNotAvailableException extends AuthenticationException
{
	/**
	 * Creates a new CredentialsNotAvailableException with a <tt>null</tt> detail
	 * message.
	 */
	public CredentialsNotAvailableException()
	{
		super();
	}

	/**
	 * Creates a new CredentialsNotAvailableException with the specified message.
	 * 
	 * @param message
	 *           the exception detail message
	 */
	public CredentialsNotAvailableException( String message )
	{
		super( message );
	}

	/**
	 * Creates a new CredentialsNotAvailableException with the specified detail
	 * message and cause.
	 * 
	 * @param message
	 *           the exception detail message
	 * @param cause
	 *           the <tt>Throwable</tt> that caused this exception, or
	 *           <tt>null</tt> if the cause is unavailable, unknown, or not a
	 *           <tt>Throwable</tt>
	 */
	public CredentialsNotAvailableException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
