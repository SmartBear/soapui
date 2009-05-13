/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.iface;

import com.eviware.soapui.model.ModelItem;

/**
 * Request interface
 * 
 * @author Ole.Matzura
 */

public interface Request extends ModelItem
{
	public final static String REQUEST_PROPERTY = "request";
	public final static String ENDPOINT_PROPERTY = "endpoint";
	public final static String ENCODING_PROPERTY = "encoding";

	public String getRequestContent();

	public void setEndpoint( String string );

	public String getEndpoint();

	public String getEncoding();

	public void setEncoding( String string );

	public Operation getOperation();

	public void addSubmitListener( SubmitListener listener );

	public void removeSubmitListener( SubmitListener listener );

	public Submit submit( SubmitContext submitContext, boolean async ) throws SubmitException;

	public Attachment[] getAttachments();

	public MessagePart[] getRequestParts();

	public MessagePart[] getResponseParts();

	public static class SubmitException extends Exception
	{
		public SubmitException( String msg )
		{
			super( msg );
		}

		public SubmitException( String message, Throwable cause )
		{
			super( message, cause );
		}

		public SubmitException( Throwable cause )
		{
			super( cause );
		}
	}
}
